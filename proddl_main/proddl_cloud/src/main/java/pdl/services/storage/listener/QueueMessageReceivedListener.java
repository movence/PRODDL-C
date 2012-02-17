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

package pdl.services.storage.listener;

import org.soyatec.windowsazure.queue.IMessageReceivedListener;
import org.soyatec.windowsazure.queue.MessageReceivedEvent;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 10/21/11
 * Time: 3:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueueMessageReceivedListener implements IMessageReceivedListener {

    public void handle(MessageReceivedEvent event) {
        try {
            System.err.println( event.getMessage() );
            System.err.println( event.getSource() );
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }
}
