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

package pdl.cloud.management;

import org.soyatec.windowsazure.table.ITableServiceEntity;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import pdl.cloud.model.User;
import pdl.cloud.storage.TableOperator;
import pdl.utils.Configuration;
import pdl.utils.StaticValues;
import pdl.utils.ToolPool;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 1/23/12
 * Time: 1:22 PM
 */
public class UserService {
    private Configuration conf;
    private TableOperator tableOperator;
    private PasswordEncoder passwordEncoder = new ShaPasswordEncoder();
    private String userTableName;

    public UserService() {
        conf = Configuration.getInstance();
        userTableName = ToolPool.buildTableName(StaticValues.TABLE_NAME_USER);
    }

    public UserService(Configuration conf) {
        this.conf = conf;
    }

    private void initializeTableOperator() {
        if (tableOperator == null)
            tableOperator = new TableOperator(conf);
    }

    /**
     * get user information by user id
     * this method adds admin user in fresh system where no user or user table exist
     * @param userId user id
     * @return user information
     * @throws Exception
     */
    public User getUserById(String userId) throws Exception {
        User rtnVal = null;

        try {
            initializeTableOperator();
            ITableServiceEntity rtnEntity = tableOperator.queryEntityBySearchKey(userTableName, StaticValues.COLUMN_USER_ID, userId, User.class);

            if(rtnEntity != null) {
                rtnVal = (User)rtnEntity;
            } else { //if admin does not exist(in case of fresh start), create one with default password ("pdlAdmin")
                if("admin".equals(userId)) {
                    User admin = new User();
                    admin.setFirstName("admin");
                    admin.setUserId("admin");
                    admin.setUserpass("pdlAdmin");
                    admin.setAdmin(1);

                    if (this.loadUser(admin))
                        rtnVal = admin;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

        return rtnVal;
    }

    /**
     * add new user
     * @param user user information
     * @return boolean of result
     * @throws Exception
     */
    public boolean loadUser(User user) throws Exception {
        boolean rtnVal = false;
        initializeTableOperator();

        user.setUserpass(passwordEncoder.encodePassword(user.getUserpass(), null));
        rtnVal = tableOperator.insertEntity(userTableName, user);
        if (!rtnVal)
            throw new Exception("Failed to load User.");

        return rtnVal;
    }

    /**
     * update user password
     * @param userId user id
     * @param oldPass old password
     * @param newPass new password
     * @param encrypted boolean of whether new password is encrypted
     * @return boolean of result
     * @throws Exception
     */
    public boolean updateUserPassword(String userId, String oldPass, String newPass, boolean encrypted) throws Exception {
        boolean rtnVal = false;

        try {
            User currUser = this.getUserById(userId);

            if (!encrypted) {
                oldPass = passwordEncoder.encodePassword(oldPass, null);
                newPass = passwordEncoder.encodePassword(newPass, null);
            }

            if (oldPass.equals(currUser.getUserpass())) {
                currUser.setUserpass(newPass);
                tableOperator.updateEntity(userTableName, currUser);
                rtnVal = true;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Filed to update User Credential.");
        }
        return rtnVal;
    }

    /**
     * check whether the user has admin priviliege
     * @param userId user id
     * @return boolean of if the user has admin privilege
     * @throws Exception
     */
    public boolean isAdmin(String userId) throws Exception {
        boolean rtnVal = false;
        try {
            User user = this.getUserById(userId);
            if (user != null && user.getUserId() != null) {
                rtnVal = user.getAdmin() == 1;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

        return rtnVal;
    }

}
