import org.eclipse.jetty.server.Server;
import org.junit.Test;

public class RunServerForManualTesting {

    @Test
    public void test() throws Exception {
        Server server = RunJettyServer.runServer();

        server.dumpStdErr();

        server.join();
    }
}
