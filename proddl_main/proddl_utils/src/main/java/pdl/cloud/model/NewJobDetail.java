package pdl.cloud.model;

import pdl.utils.StaticValues;

/**
 * User: movence
 * Date: 1/17/14
 * Time: 11:44 AM
 * pdl.cloud.model
 */
public class NewJobDetail extends AbstractModel {
    private String jobName;
    private String inputFileUUID;
    private String scriptFileUUID;
    private String userId;
    private int status;
    private int priority;
    private String input;
    private String result;
    private String log;
    private String jobDirectory;

    public NewJobDetail() {
        super();
    }

    public NewJobDetail(String uuid) {
        super(uuid);
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getInputFileUUID() {
        return inputFileUUID;
    }

    public void setInputFileUUID(String inputFileUUID) {
        this.inputFileUUID = inputFileUUID;
    }

    public String getScriptFileUUID() {
        return scriptFileUUID;
    }

    public void setScriptFileUUID(String scriptFileUUID) {
        this.scriptFileUUID = scriptFileUUID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    /**
     * @return String value of status
     *         0:Submitted, 1:Running, 2:Finished, 4:Failed
     */
    public String statusInString() {
        return status== StaticValues.JOB_STATUS_SUBMITTED?"submitted"
                :status==StaticValues.JOB_STATUS_PENDING?"pending"
                :status==StaticValues.JOB_STATUS_RUNNING?"running"
                :status==StaticValues.JOB_STATUS_COMPLETED?"finished"
                :"failed";
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getJobDirectory() {
        return jobDirectory;
    }

    public void setJobDirectory(String jobDirectory) {
        this.jobDirectory = jobDirectory;
    }
}
