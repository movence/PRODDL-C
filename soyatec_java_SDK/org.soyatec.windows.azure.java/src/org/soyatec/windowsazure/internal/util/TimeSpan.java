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
package org.soyatec.windowsazure.internal.util;

import java.util.concurrent.TimeUnit;

public class TimeSpan {
	private TimeUnit unit;
	private long duration;

	/**
	 * TimeSpan ZERO.
	 */
	public static final TimeSpan ZERO = new TimeSpan(0, TimeUnit.MILLISECONDS);

	TimeSpan(long value, TimeUnit unit) {
		this.unit = unit;
		this.duration = value;
	}

	/**
	 * Get the from seconds TimeSpan
	 * @param value
	 * @return TimeSpan
	 */
	public static TimeSpan fromSeconds(long value) {
		return new TimeSpan(value, TimeUnit.SECONDS);
	}

	/**
	 * Get the from milliseconds TimeSpan
	 * @param value
	 * @return TimeSpan
	 */
	public static TimeSpan fromMilliseconds(long value) {
		return new TimeSpan(value, TimeUnit.MILLISECONDS);
	}

	/**
	 * Change TimeSpan to milliseconds.
	 * @return milliseconds
	 */
	public long toMilliseconds() {
		return unit.toMillis(duration);
	}

	/**
	 * Change TimeSpan to Seconds
	 * @return seconds
	 */
	public long toSeconds() {
		return unit.toSeconds(duration);
	}

	/**
	 * Compare the two timeStamp's size
	 * @param o
	 *        the timeStamp to be compare 
	 * @return if the parameter timeStamp equal this timeStamp,it will be return 0,
	 *         if the parameter timeStamp less-than this timeStamp,it will be return a number that is greater than 0,
	 *         if the parameter timeStamp greater than this timeStamp,it will be return a number that is less-than 0
	 */
	public long compareTo(TimeSpan o) {
		return unit.toMillis(duration) - o.unit.toMillis(o.duration);
	}
}
