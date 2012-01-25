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

package pdl.services.management;

import pdl.common.Configuration;

import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 11/1/11
 * Time: 2:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScheduledInstanceMonitor extends TimerTask {
    private CloudInstanceManager instanceManager;

    public ScheduledInstanceMonitor( Configuration conf) {
        instanceManager = new CloudInstanceManager( conf );
    }

    public synchronized  void run() {
        instanceManager.executeScalingByProcessorTime();
    }
}
