import org.eclipse.jetty.server.Server;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
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
            final AtomicInteger
                validDelayedResponses = new AtomicInteger(0),
                validInstantResponses = new AtomicInteger(0),
                invalidDelayedResponses = new AtomicInteger(0),
                invalidInstantResponses = new AtomicInteger(0);

            ExecutorService executor = Executors.newFixedThreadPool(concurrentThreads);

            for (int i = 0; i < 300; i++) {
                executor.submit(() -> {
                    try {
                        // Try to spread the requests out a little
                        // Oddly if I enable this I see many more valid-instant responses.
                        //
                        //Thread.sleep(ThreadLocalRandom.current().nextInt(0, 50));

                        long startTime = System.currentTimeMillis();
                        String result = new String(new URL("http://127.0.0.1:8080/delayed-hello").openStream().readAllBytes(),
                                StandardCharsets.UTF_8);
                        long respTime = System.currentTimeMillis() - startTime;

                        if (respTime < 1000 && result.equals("Hello, world!\n")) {
                            validInstantResponses.incrementAndGet();
                        } else if (respTime >= 1000 && result.equals("Hello, world!\n")) {
                            validDelayedResponses.incrementAndGet();
                        } else if (respTime < 1000) {
                            // Could warn about the invalid value of result here
                            invalidInstantResponses.incrementAndGet();
                        } else {
                            // Could warn about the invalid value of result here
                            invalidDelayedResponses.incrementAndGet();
                        }
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            executor.awaitTermination(10, TimeUnit.SECONDS);

            System.out.printf("Client Threads: %d \n"
                + "\tvalid-instant: %d \n"
                + "\tvalid-delayed: %d \n"
                + "\tinvalid-instant: %d\n"
                + "\tinvalid-delayed: %d\n"
                + "\n", concurrentThreads,
                validInstantResponses.get(), validDelayedResponses.get(),
                invalidInstantResponses.get(), invalidDelayedResponses.get());

            // Give the server a chance to clear any pending requests we aborted.
            Thread.sleep(2000);
        }
        server.stop();
    }

}
