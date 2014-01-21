package pdl.cloud.model;

import pdl.utils.StaticValues;

/**
 * User: movence
 * Date: 1/18/14
 * Time: 11:12 PM
 * pdl.cloud.model
 */
public class FileInfo extends AbstractModel {
    String type;
    String suuid; //file name
    String content;
    String userId;
    String container;
    String path;
    String originalName;
    int status; //reserved:1, committed:2

    public FileInfo() {
        super();
    }

    public FileInfo(String uuid) {
        super(uuid);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSuuid() {
        return suuid;
    }

    public void setSuuid(String suuid) {
        this.suuid = suuid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public int getStatus() {
        return status;
    }

    public String statusInString() {
        return status== StaticValues.FILE_STATUS_COMMITTED?"committed"
                :status==StaticValues.FILE_STATUS_RESERVED?"reserved"
                :"N/A";
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
