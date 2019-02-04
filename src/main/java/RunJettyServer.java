import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jetty.server.ConnectionLimit;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;

public class RunJettyServer {

    public static Server runServer() throws Exception
    {
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(4);
        QueuedThreadPool threadPool = new QueuedThreadPool(4, 4, 100, queue);

        final Server server = new Server(threadPool);

        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory());
        connector.setPort(8080);
        server.setConnectors(new Connector[]{connector});

        // TODO - Not sure if this is actually the same as threads
        server.addBean(new ConnectionLimit(1,server));

        final WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setResourceBase(new File("src/main/webapp").getAbsolutePath());
        context.setDescriptor(new File("src/main/webapp/WEB-INF/web.xml").getAbsolutePath());
        context.setParentLoaderPriority(true);
        context.setServer(server);

        server.setHandler(context);

        server.start();

        return server;
    }

}
