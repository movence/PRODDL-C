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

package pdl.web.service;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pdl.cloud.management.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 12/23/11
 * Time: 9:34 AM
 */
public class UserManagementService implements UserDetailsService {
    protected static Logger logger = Logger.getLogger("UserManagementService");

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException, DataAccessException {
        PasswordEncoder passwordEncoder = new ShaPasswordEncoder();
        UserDetails user = null;
        try {
            UserService pdlUserService = new UserService();
            pdl.cloud.model.User pdlUser = pdlUserService.getUserById(userId);

            user = new User(
                    pdlUser.getUserid(),
                    pdlUser.getUserpass(),
                    true,
                    true,
                    true,
                    true,
                    getAuthorities(pdlUser.getAdmin())
            );

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in retrieving user");
        }
        return user;
    }

    /**
     * Retrieves the correct ROLE type depending on the access level, where access level is an Integer.
     * Basically, this interprets the access value whether it's for a regular user or admin.
     *
     * @param access an integer value representing the access of the user
     * @return collection of granted authorities
     */
    public Collection<GrantedAuthority> getAuthorities(int access) {
        List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>(2);
        authList.add(new GrantedAuthorityImpl("ROLE_USER"));

        if (access == 1) {
            authList.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
        }

        return authList;
    }
}
