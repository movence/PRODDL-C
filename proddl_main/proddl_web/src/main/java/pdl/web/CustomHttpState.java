/*
 * Copyright J. Craig Venter Institute, 2011
 *
 * The creation of this program was supported by the U.S. National
 * Science Foundation grant 1048199 and the Microsoft allocation
 * in the MS Azure cloud.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pdl.web;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.auth.AuthScope;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 9/28/12
 * Time: 2:54 PM
 */
public class CustomHttpState extends HttpState {

    /**
     * Set credentials property.
     *
     * @param credentials
     * @see #setCredentials(org.apache.commons.httpclient.auth.AuthScope, org.apache.commons.httpclient.Credentials)
     */
    public void setCredentials(final Credentials credentials) {
        super.setCredentials(AuthScope.ANY, credentials);
    }

}
