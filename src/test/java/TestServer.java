import org.eclipse.jetty.server.Server;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TestServer {

    @Test
    public void test() throws Exception {
        Server server = RunJettyServer.runServer();


        // Run lots of requests over multiple threads.
        //
        // What should we expect to happen?
        //
        // I guess ideally we should come up with a way to have Jetty accept all requests
        // very quickly and respond to them all 5 seconds later...And then once that works
        // we should find a way to make sure that each user can only have one
        // pending request each

        for (int concurrentThreads = 1; concurrentThreads < 11; concurrentThreads++) {
            final AtomicInteger counter = new AtomicInteger(0);

            ExecutorService executor = Executors.newFixedThreadPool(concurrentThreads);

            for (int i = 0; i < 300; i++) {
                executor.submit(new Runnable() {
                    public void run() {
                        try {
                            long startTime = System.currentTimeMillis();
                            String result = new String(new URL("http://127.0.0.1:8080/delayed-hello").openStream().readAllBytes(),
                                    StandardCharsets.UTF_8);
                            long endTime = System.currentTimeMillis();

                            if (endTime - startTime < 1000) {
                                System.err.println("Got an undelayed response after " + (endTime - startTime) + "ms");
                            } else if (!result.equals("Hello, world!\n")) {
                                System.err.println("DELAYED: Unexpectedly got " + result);
                            } else {
//                                System.out.println("DELAYED: Success");
                                counter.incrementAndGet();
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

            executor.awaitTermination(10, TimeUnit.SECONDS);

            System.out.println("Client Threads: " + concurrentThreads + " - Got " + counter.get() + " responses in 10 secs.");
        }
        server.stop();
    }

}
