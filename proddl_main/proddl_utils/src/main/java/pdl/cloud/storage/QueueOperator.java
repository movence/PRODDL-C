/*
 * Copyright J. Craig Venter Institute, 2014
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package pdl.cloud.storage;

import org.soyatec.windowsazure.queue.IMessage;
import org.soyatec.windowsazure.queue.IQueue;
import org.soyatec.windowsazure.queue.QueueStorageClient;
import org.soyatec.windowsazure.queue.internal.Message;
import pdl.cloud.storage.listener.QueueMessageReceivedListener;
import pdl.utils.Configuration;
import pdl.utils.StaticValues;

import java.net.URI;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 11/8/11
 * Time: 8:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class QueueOperator {
    private Configuration conf;

    private QueueStorageClient queueStorageClient;

    public QueueOperator() {
        try {
            conf = Configuration.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public QueueOperator(Configuration conf) {
        this.conf = conf;
    }

    private void initQueueClient() {
        try {
            queueStorageClient = QueueStorageClient.create(
                    URI.create(StaticValues.AZURE_QUEUE_HOST_NAME),
                    false,
                    conf.getStringProperty(StaticValues.CONFIG_KEY_CSTORAGE_NAME),
                    conf.getStringProperty(StaticValues.CONFIG_KEY_CSTORAGE_PKEY));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private IQueue getQueue(String queueName) {
        IQueue queue = null;
        try {
            if (queueStorageClient == null)
                initQueueClient();

            queue = queueStorageClient.getQueue(queueName);

            if (!queue.isQueueExist() && !queue.createQueue())
                throw new Exception("Queue Creation Failed");

            addQueueListner(queue);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return queue;
    }

    private void addQueueListner(IQueue queue) {
        try {
            QueueMessageReceivedListener queueMRL = new QueueMessageReceivedListener();
            queue.addMessageReceivedListener(queueMRL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean enqueue(String queueName, String msg) {
        boolean rtnVal = false;
        try {
            IQueue theQueue = null;
            if (theQueue == null || !theQueue.isQueueExist())
                theQueue = getQueue(queueName);

            IMessage queueMsg = new Message(msg);
            theQueue.putMessage(queueMsg);

            rtnVal = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rtnVal;
    }

    public String dequeue(String queueName, boolean isPop) {
        String msg = null;
        try {
            IQueue theQueue = getQueue(queueName);
            if (theQueue != null && theQueue.isQueueExist()) {

                IMessage imsg;
                if (isPop) {
                    imsg = theQueue.getMessage();
                    if (imsg != null)
                        theQueue.deleteMessage(imsg);
                } else {
                    imsg = theQueue.peekMessage();
                }

                if (imsg != null) {
                    msg = imsg.getContentAsString();
                } else
                    System.err.println("Queue returns NULL IMessage");

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return msg;
    }
}
