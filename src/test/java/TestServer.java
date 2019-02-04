import org.eclipse.jetty.server.Server;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestServer {

    @Test
    public void test() throws Exception {
        Server server = RunJettyServer.runServer();


        // Run lots of requests - 1,000 over 100 threads.
        //
        // What should we expect to happen?
        //
        // I guess ideally we should come up with a way to have Jetty accept all 1,000
        // very quickly and respond to them all 5 seconds later...And then once that works
        // we should find a way to make sure that each user can only have one
        // pending request each

        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0 ; i < 1000 ; i++) {
            executor.submit(new Runnable() {
                public void run() {
                    try {
                        String result = new String(new URL("http://127.0.0.1:8080/hello").openStream().readAllBytes(), StandardCharsets.UTF_8);

                        if (!result.equals("Hello, world!\n")) {
                            System.err.println("UNDELAYED: Unexpectedly got " + result);
                        } else {
                            System.out.println("UNDELAYED: Success");
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            if (i % 10 == 0) {
                executor.submit(new Runnable() {
                    public void run() {
                        try {
                            String result = new String(new URL("http://127.0.0.1:8080/delayed-hello").openStream().readAllBytes(),
                                StandardCharsets.UTF_8);

                            if (!result.equals("Hello, world!\n")) {
                                System.err.println("DELAYED: Unexpectedly got " + result);
                            } else {
                                System.out.println("DELAYED: Success");
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

        }

        executor.awaitTermination(30, TimeUnit.SECONDS);

        server.stop();
    }

}
