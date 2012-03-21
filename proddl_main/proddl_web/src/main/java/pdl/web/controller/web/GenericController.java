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

package pdl.web.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import pdl.web.service.common.FileService;
import pdl.web.utils.AjaxUtils;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping(value = "w")
public class GenericController {

    @RequestMapping(value = "common", method = RequestMethod.GET)
    public String getCommonPage() {
        return "common/commonpage";
    }

    @RequestMapping(value = "main", method = RequestMethod.GET)
    public String getMainPage() {
        return "index";
    }

    @ModelAttribute
    public void ajaxAttribute(WebRequest request, Model model) {
        model.addAttribute("ajaxRequest", AjaxUtils.isAjaxRequest(request));
    }

    @RequestMapping(value = "fileupload", method = RequestMethod.GET)
    public String fileUploadForm() {
        return "fileupload";
    }

    @RequestMapping(value = "fileupload", method = RequestMethod.POST)
    public String processUpload(@RequestParam("file") MultipartFile file, @RequestParam("type") String type, Model model, Principal principal) {

        try {
            FileService fileService = new FileService();
            fileService.uploadFile(file, type, principal.getName());
        } catch (Exception ex) {

        }

        model.addAttribute("message", "File '" + file.getOriginalFilename() + "' uploaded successfully");

        return "fileupload";
    }

    @RequestMapping("test")
    public ModelAndView test() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        return new ModelAndView("test", "message", uuid1 + "::::" + uuid2);
    }
}
