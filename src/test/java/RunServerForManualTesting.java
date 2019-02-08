import org.eclipse.jetty.server.Server;
import org.junit.Test;

public class RunServerForManualTesting {

    @Test
    public void test() throws Exception {
        // When you run this you might like to set
        //     -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog
        //     -Dorg.eclipse.jetty.LEVEL=DEBUG
        // as VM arguments to get debug logging from Jetty.

        Server server = RunJettyServer.runServer();

        server.dumpStdErr();

        System.out.println("Try running\n    curl http://127.0.0.1:8080/delayed-hello");

        server.join();
    }
}
