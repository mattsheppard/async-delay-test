import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class DelayFilter implements Filter {

    private ServletContext context;

    public void init(FilterConfig fConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = ((HttpServletRequest)request);

        delayAsInStackOverflowAnswer(httpRequest, response, chain);
    }

    private void noDelay(final HttpServletRequest request, final ServletResponse response,
                                       FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
    }

    // Like https://github.com/yourarj/spring-security-prevent-brute-force/blob/master/src/main/java/mr/awesome/spring/springsecuritydemoone/filter/AttemptFilter.java
    private void delayAsInStackOverflowAnswer(ServletRequest request, ServletResponse response, FilterChain chain) {
        AsyncContext asyncContext = request.startAsync();
        asyncContext.setTimeout(1000);
    }

    public static AsyncContext getAsyncContext(ServletRequest request, ServletResponse response) {
        AsyncContext asyncContext = null;
        if (request.isAsyncStarted()) {
            asyncContext = request.getAsyncContext();
        }
        else {
            asyncContext = request.startAsync(request, response);
        }
        return asyncContext;
    }

    private void delayWithASimpleSleep(final HttpServletRequest request, final ServletResponse response,
                                          FilterChain chain) throws IOException, ServletException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        chain.doFilter(request, response);
    }

    private void delayWithASeparateThread(final HttpServletRequest request, final ServletResponse response,
                                      FilterChain chain) throws IOException, ServletException {
        final AsyncContext asyncContext = request.startAsync();

        asyncContext.start(new Runnable() {
            @ Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                try {
                    chain.doFilter(request, response);
                    asyncContext.complete();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ServletException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void delayResponseWithAListener(final HttpServletRequest request, final ServletResponse response,
                                            FilterChain chain) throws IOException, ServletException {

        if (request.getAttribute("delayComplete") == null) {
            final AsyncContext asyncContext = getAsyncContext(request, response);
            asyncContext.addListener(new DispatchOnTimeoutListener());
            asyncContext.setTimeout(1000);
//            asyncContext.dispatch();
        } else {
            chain.doFilter(request, response);
        }
    }

    private class DispatchOnTimeoutListener implements AsyncListener
    {
        public void onStartAsync(AsyncEvent event) throws IOException
        {
//            System.out.println("onStartAsync");
        }
        public void onComplete(AsyncEvent event) throws IOException
        {
//            System.out.println("onComplete");
        }
        public void onTimeout(AsyncEvent event) throws IOException
        {
            AsyncContext asyncContext = event.getAsyncContext();
            asyncContext.getRequest().setAttribute("delayComplete", Boolean.TRUE);
            asyncContext.dispatch();
        }
        public void onError(AsyncEvent event) throws IOException
        {
            System.out.println("onError");
        }
    }


    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private void delayResponeWithAQueue(final HttpServletRequest request, final ServletResponse response,
        FilterChain chain) throws IOException, ServletException {

        if (request.getAttribute("delayComplete") == null) {
            final AsyncContext asyncContext = getAsyncContext(request, response);

            scheduler.schedule(new Runnable() {
                public void run() {
                    asyncContext.getRequest().setAttribute("delayComplete", Boolean.TRUE);
                    asyncContext.dispatch();
                }
            }, 1, TimeUnit.SECONDS);
        } else {
            chain.doFilter(request, response);
        }
    }


    public void destroy() {
        //we can close resources here
    }

}