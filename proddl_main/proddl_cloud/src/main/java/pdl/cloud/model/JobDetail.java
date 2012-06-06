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

package pdl.cloud.model;

import org.soyatec.windowsazure.table.AbstractTableServiceEntity;
import org.soyatec.windowsazure.table.Guid;
import pdl.common.StaticValues;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 8/12/11
 * Time: 2:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobDetail extends AbstractTableServiceEntity {
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

    public JobDetail(String partitionKey, String rowKey) {
        super(partitionKey, rowKey);
    }

    public JobDetail(String partitionKey) {
        this(partitionKey + "_job", new Guid().getValue());
    }

    public JobDetail() {
        //this( UUID.randomUUID().toString() );
        this("generic");
    }

    public String getJobUUID() {
        return getRowKey();
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

    /**
     * @return String value of status
     *         0:Submitted, 1:Running, 2:Finished, 4:Failed
     */
    public String getStatusInString() {
        return status==StaticValues.JOB_STATUS_SUBMITTED?"submitted"
                :status==StaticValues.JOB_STATUS_PENDING?"pending"
                :status==StaticValues.JOB_STATUS_RUNNING?"running"
                :status==StaticValues.JOB_STATUS_COMPLETED?"finished"
                :"failed";
    }

    public int getStatus() {
        return status;
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

    public String getJobDirectory() {
        return jobDirectory;
    }

    public void setJobDirectory(String jobDirectory) {
        this.jobDirectory = jobDirectory;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
