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

package pdl.common;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 3/6/12
 * Time: 2:24 PM
 */
public class ToolPool {

    public static boolean isDirectoryExist(String path) {
        File dir = new File(path);
        return dir.exists() && dir.isDirectory();
    }

    public static void createDirectoryIfNotExist(String path) {
        if(!isDirectoryExist(path)) {
            File dir = new File(path);
            dir.mkdir();
        }
    }

    public static boolean canReadFile(String path) {
        File file = new File(path);
        return file.exists() && file.canRead() && file.length()>0;
    }

    /**
     * build path with given parameters
     * @param path path to begin with
     * @param dirs series of directory names to append
     * @return new path with given dirs
     */
    public static String buildFilePath(String path, String... dirs) {
        String newPath = path.endsWith(File.separator)?path:path.concat(File.separator);
        if(dirs!=null) {
            for(String dir : dirs) {
                newPath += dir.concat(File.separator);
            }
        }
        return newPath;
    }

    public static Map<String, Object> jsonStringToMap(String value) throws IllegalArgumentException {
        Map<String, Object> rtnMap = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            if(value!=null && !value.isEmpty()) {
                TypeReference<TreeMap<String,Object>> typeRef = new TypeReference<TreeMap<String,Object>>() {};
                rtnMap = mapper.readValue(value, typeRef);
            }
        } catch (JsonParseException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        } catch (JsonMappingException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }

        return rtnMap;
    }

    public static String jsonMapToString(Map jsonMap) throws IllegalArgumentException {
        ObjectMapper mapper = new ObjectMapper();
        Writer writer = new StringWriter();
        try {
            mapper.writeValue(writer, jsonMap);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return writer.toString();
    }
}
