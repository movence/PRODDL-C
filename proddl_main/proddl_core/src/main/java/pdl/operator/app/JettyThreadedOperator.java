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

package pdl.operator.app;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.security.ProtectionDomain;

/**
 * Created by IntelliJ IDEA.
 * User: hkim
 * Date: 12/19/11
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class JettyThreadedOperator extends Thread {
    private String jettyPort;

    public JettyThreadedOperator(String port) {
        jettyPort = port;
    }

    public void run() {
        try {
            System.out.println("JettyThreadedOperator: START");

            Server jettyServer = new Server();
            jettyServer.setStopAtShutdown(true);
            jettyServer.setGracefulShutdown(1000);

            QueuedThreadPool threadPool = new QueuedThreadPool();
            threadPool.setMaxThreads(100);
            jettyServer.setThreadPool(threadPool);

            Connector connector = new SelectChannelConnector();
            connector.setPort(Integer.parseInt(jettyPort));
            jettyServer.setConnectors(new Connector[]{connector});

            ProtectionDomain protectionDomain = JettyOperator.class.getProtectionDomain();
            String warFile = protectionDomain.getCodeSource().getLocation().toExternalForm();
            String currentDir = new File(protectionDomain.getCodeSource().getLocation().getPath()).getParent();

            WebAppContext context = new WebAppContext(warFile, "/");
            context.setServer(jettyServer);

            HandlerList handlers = new HandlerList();
            handlers.addHandler(context);
            jettyServer.setHandler(handlers);

            jettyServer.start();
            jettyServer.join();

            System.out.println("JettyThreadedOperator: start DONE");
        } catch (Exception ex) {
            System.out.println("JettyThreadedOperator.start threw : " + ex.toString());
            ex.printStackTrace();
        }
    }
}
