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

package pdl.web.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/6/12
 * Time: 8:51 AM
 */
public class RestAuthenticationFilter extends OncePerRequestFilter {
    private final static String USER_NAME_PARAM = "uname";
    private final static String USER_PASS_PARAM = "usig";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        System.err.println("Auth type: " + request.getAuthType() );
        List<String> headerKeys = Arrays.asList( USER_NAME_PARAM, USER_PASS_PARAM );
        Map<String, String> headerAndParms = new HashMap<String, String>();

        // load header values we care about
        Enumeration e = request.getHeaderNames();
        while ( e.hasMoreElements() ) {
            String key = (String)e.nextElement();

            System.err.println( "header:" + key + ", value=" + request.getHeader(key));
            if ( headerKeys.contains( key ) ) {
                headerAndParms.put( key, request.getHeader( key ) );
            }
        }

        // load parameters
        for( Object key : request.getParameterMap().keySet() ) {
            String[] o = (String[]) request.getParameterMap().get( key );
            headerAndParms.put( (String) key, o[0] );
        }

        String userName = headerAndParms.get( USER_NAME_PARAM );
        String userPasswd = headerAndParms.get( USER_PASS_PARAM );

        System.err.println( "User: " + userName + ", userPasswd: " + userPasswd );

        if ( userName == null || userPasswd == null ) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "REST signature failed validation.");
            return;
        }
        /*catch (Exception e) {
              response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "The REST Security Server experienced an internal error.");
              return;
          }*/

        filterChain.doFilter(request, response);
    }
}
