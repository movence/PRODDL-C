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

/**
 *
 */
package pdl.web.controller.web;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles and retrieves the login or denied page depending on the URI template
 */
@Controller
@RequestMapping(value = "w")
public class AuthenticationController {
    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String getLoginPage(@RequestParam(value = "error", required = false) boolean error, ModelMap model) {
        if (error == true) {
            model.put("error", "true");
        } else {
            model.put("error", "");
        }
        return "common/login";
    }

    @RequestMapping(value = "auth", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, String> processLoginInJson(
            @RequestParam(value = "userId", required = true) String userId,
            @RequestParam(value = "userPass", required = true) String userPass, Model model) {
        Map<String, String> resultMap = new HashMap<String, String>();
        return resultMap;
    }

    @RequestMapping(value = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "common/denied";
    }
}