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

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 10/28/11
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class PerformanceData extends AbstractTableServiceEntity {
    private Long EventTickCount;
    private String DeploymentId;
    private String Role;
    private String RoleInstance;
    private String CounterName;
    private double CounterValue;

    public PerformanceData(String partitionKey, String rowKey) {
        super(partitionKey, rowKey);
    }

    public PerformanceData(String partitionKey) {
        this(partitionKey, new Guid().getValue());
    }

    public PerformanceData() {
        this("generic");
    }

    public Long getEventTickCount() {
        return EventTickCount;
    }

    public void setEventTickCount(Long eventTickCount) {
        EventTickCount = eventTickCount;
    }

    public String getDeploymentId() {
        return DeploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        DeploymentId = deploymentId;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public String getRoleInstance() {
        return RoleInstance;
    }

    public void setRoleInstance(String roleInstance) {
        RoleInstance = roleInstance;
    }

    public String getCounterName() {
        return CounterName;
    }

    public void setCounterName(String counterName) {
        CounterName = counterName;
    }

    public double getCounterValue() {
        return CounterValue;
    }

    public void setCounterValue(double counterValue) {
        CounterValue = counterValue;
    }
}
