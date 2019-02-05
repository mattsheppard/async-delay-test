import org.eclipse.jetty.server.Server;
import org.junit.Test;

public class RunServerForManualTesting {

    @Test
    public void test() throws Exception {
        Server server = RunJettyServer.runServer();

        server.dumpStdErr();

        System.out.println("Try running\n    curl http://127.0.0.1:8080/delayed-hello");

        server.join();
    }
}
