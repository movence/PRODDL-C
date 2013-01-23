/**
 * Copyright  2006-2010 Soyatec
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *
 * $Id$
 */
package org.soyatec.windowsazure.blob.internal;

import java.util.Random;
import java.util.concurrent.Callable;

import org.soyatec.windowsazure.blob.IRetryPolicy;
import org.soyatec.windowsazure.error.StorageException;
import org.soyatec.windowsazure.internal.util.HttpUtilities;
import org.soyatec.windowsazure.internal.util.TimeSpan;

/**
 * The <code>RetryPolicies</code> class definitions for some standard retry
 * policies.
 * 
 */
public class RetryPolicies {
	public static final TimeSpan StandardMinBackoff = TimeSpan
			.fromMilliseconds(100);
	public static final TimeSpan StandardMaxBackoff = TimeSpan.fromSeconds(30);
	private static final Random Random = new Random();

	/**
	 * Policy that does no retries i.e., it just invokes
	 * <paramrefname="action"/> exactly once
	 */
	public static IRetryPolicy noRetry() {
		return new NoRetry();
	}

	/**
	 * Policy that retries a specified number of times with a specified fixed
	 * time interval between retries.
	 * 
	 * @param numberOfRetries
	 *            The number of times to retry. Should be a non-negative number.
	 * @param intervalBetweenRetries
	 *            The time interval between retries.Use TimeSpan.Zero to specify
	 *            immediate. When numberOfRetries is 0 and
	 *            intervalBetweenRetries is TimeSpan.Zero this policy is
	 *            equivalent to the NoRetry policy
	 */
	public static IRetryPolicy retryN(int numberOfRetries,
			TimeSpan intervalBetweenRetries) {
		return new RetryN(numberOfRetries, intervalBetweenRetries);
	}

	/**
	 * Policy that retries a specified number of times with a randomized
	 * exponential backoff scheme. For this retry policy, the minimum amount of
	 * milliseconds between retries is given by the StandardMinBackoff constant,
	 * and the maximum backoff is predefined by the StandardMaxBackoff constant.
	 * Otherwise, the backoff is calculated as random(2^currentRetry) *
	 * deltaBackoff.
	 * 
	 * @param numberOfRetries
	 *            The number of times to retry. Should be a non-negative number.
	 * @param deltaBackoff
	 *            The multiplier in the exponential backoff scheme.
	 * @return Policy that retries a specified number of times with a randomized
	 *         exponential backoff scheme.
	 */
	public static IRetryPolicy retryExponentialN(int numberOfRetries,
			TimeSpan deltaBackoff) {
		return new RetryExponentialN(numberOfRetries, StandardMinBackoff,
				StandardMaxBackoff, deltaBackoff);
	}

	/**
	 * Policy that retries a specified number of times with a randomized
	 * exponential backoff scheme. For this retry policy, the minimum amount of
	 * milliseconds between retries is given by the minBackoff parameter, and
	 * the maximum backoff is predefined by the maxBackoff parameter. Otherwise,
	 * the backoff is calculated as random(2^currentRetry) * deltaBackoff.
	 * 
	 * @param numberOfRetries
	 *            The number of times to retry. Should be a non-negative number.
	 * @param minBackoff
	 *            The minimum backoff interval.
	 * @param maxBackoff
	 *            The maximum backoff interval.
	 * @param deltaBackoff
	 *            The multiplier in the exponential backoff scheme.
	 * @return Policy that retries a specified number of times with a randomized
	 *         exponential backoff scheme.
	 */
	public static IRetryPolicy retryExponentialN(int numberOfRetries,
			TimeSpan minBackoff, TimeSpan maxBackoff, TimeSpan deltaBackoff) {
		if (minBackoff.compareTo(maxBackoff) > 0) {
			throw new IllegalArgumentException(
					"The minimum backoff must not be larger than the maximum backoff period.");
		}
		if (minBackoff.compareTo(TimeSpan.ZERO) < 0) {
			throw new IllegalArgumentException(
					"The minimum backoff period must not be negative.");
		}
		return new RetryExponentialN(numberOfRetries, minBackoff, maxBackoff,
				deltaBackoff);
	}

	/**
	 * Policy that does no retries i.e., it just invokes action exactly once.
	 */
	static class NoRetry implements IRetryPolicy {

		public Object execute(Callable action) throws StorageException {
			try {
				return action.call();
			} catch (Exception e) {
				throw HttpUtilities.translateWebException(e);
			}
		}
	}

	static class RetryN implements IRetryPolicy {
		private int numberOfRetries;
		private TimeSpan intervalBetweenRetries;

		/**
		 * Construct policy that retries a specified number of times with a
		 * specified fixed time interval between retries.
		 * 
		 * @param numberOfRetries
		 *            The number of times to retry. Should be a non-negative
		 *            number.
		 * @param intervalBetweenRetries
		 *            The time interval between retries. Use TimeSpan.Zero to
		 *            specify immediate retries
		 */
		public RetryN(int numberOfRetries, TimeSpan intervalBetweenRetries) {
			this.numberOfRetries = numberOfRetries;
			this.intervalBetweenRetries = intervalBetweenRetries;
		}

		public Object execute(Callable action) throws StorageException {
			int retries = numberOfRetries;
			do {
				try {
					return action.call();
				} catch (Exception e) {
					if (retries == 0) {
						throw HttpUtilities.translateWebException(e);
					}
					if (intervalBetweenRetries.compareTo(TimeSpan.ZERO) > 0) {
						try {
							Thread.sleep(intervalBetweenRetries
									.toMilliseconds());
						} catch (InterruptedException e1) {
						}
					}
				}
			} while (retries-- > 0);
			return null;
		}

	}

	/**
	 * Policy that retries a specified number of times with a randomized
	 * exponential backoff scheme
	 * 
	 */
	static class RetryExponentialN implements IRetryPolicy {

		private int numberOfRetries;
		private TimeSpan minBackoff;
		private TimeSpan maxBackoff;
		private TimeSpan deltaBackoff;

		/**
		 * Construct policy that retries a specified number of times with a
		 * randomized exponential backoff scheme.
		 * 
		 * @param numberOfRetries
		 * 			The number of times to retry. Should be a non-negative number.
		 * @param minBackoff
		 * 			The minimum amount of milliseconds between retries.
		 * @param maxBackoff
		 * 			The maximum backoff.
		 * @param deltaBackoff
		 * 			The multiplier in the exponential backoff scheme.
		 */
		public RetryExponentialN(int numberOfRetries, TimeSpan minBackoff,
				TimeSpan maxBackoff, TimeSpan deltaBackoff) {
			this.numberOfRetries = numberOfRetries;
			this.minBackoff = minBackoff;
			this.maxBackoff = maxBackoff;
			this.deltaBackoff = deltaBackoff;
		}

		public Object execute(Callable action) throws StorageException {
			int totalNumberOfRetries = numberOfRetries;
			int retries = numberOfRetries;
			TimeSpan backoff;
			// sanity check
			// this is already checked when creating the retry policy in case
			// other than the standard settings are used
			// because this library is available in source code, the standard
			// settings can be changed and thus we
			// check again at this point
			if (minBackoff.compareTo(maxBackoff) > 0) {
				throw new IllegalArgumentException(
						"The minimum backoff must not be larger than the maximum backoff period.");
			}
			if (minBackoff.compareTo(TimeSpan.ZERO) < 0) {
				throw new IllegalArgumentException(
						"The minimum backoff period must not be negative.");
			}
			do {
				try {
					return action.call();
				} catch (Exception e) {
					if (retries == 0) {
						throw HttpUtilities.translateWebException(e);
					}
					backoff = calculateCurrentBackoff(minBackoff, maxBackoff,
							deltaBackoff, totalNumberOfRetries
									- retries);
					if (backoff.compareTo(TimeSpan.ZERO) > 0) {
						try {
							Thread.sleep(backoff.toMilliseconds());
						} catch (InterruptedException e1) {
						}
					}
				}
			} while (retries-- > 0);
			return null;
		}

		/**
		 * Calculate current backoff timespan
		 * @param minBackoff
		 *           The minimum backoff interval.
		 * @param maxBackoff
		 *           The maximum backoff interval.
		 * @param deltaBackoff
		 *           The multiplier in the exponential backoff scheme.
		 * @param curRetry 
		 * 			 The number of times to retry. Should be a non-negative number.
		 * @return current backoff
		 */
		private TimeSpan calculateCurrentBackoff(TimeSpan minBackoff,
				TimeSpan maxBackoff, TimeSpan deltaBackoff, int curRetry) {
			TimeSpan backoff;
			if (curRetry > 30) {
				backoff = maxBackoff;
			} else {
				try {
					// only randomize the multiplier here
					// it would be as correct to randomize the whole backoff
					// result
					long delay = Random.nextInt((1 << curRetry) + 1);
					delay *= deltaBackoff.toMilliseconds();
					delay += minBackoff.toMilliseconds();
					backoff = TimeSpan.fromMilliseconds(delay);
				} catch (ArithmeticException e) {
					backoff = maxBackoff;
				}
				if (backoff.compareTo(maxBackoff) > 0) {
					backoff = maxBackoff;
				}
			}
			return backoff;
		}

	}

}
