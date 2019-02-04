import java.io.IOException;
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

        delayResponeWithAListener(httpRequest, response, chain);
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

    private void delayResponeWithAListener(final HttpServletRequest request, final ServletResponse response,
        FilterChain chain) throws IOException, ServletException {

        if (request.getAttribute("delayComplete") == null) {
            final AsyncContext asyncContext = getAsyncContext(request, response);
            asyncContext.addListener(new DispatchOnTimeoutListener());
            asyncContext.setTimeout(5000);
//            asyncContext.dispatch();
        } else {
            chain.doFilter(request, response);
        }
    }

    private class DispatchOnTimeoutListener implements AsyncListener
    {
        public void onStartAsync(AsyncEvent event) throws IOException
        {
            System.out.println("onStartAsync");
        }
        public void onComplete(AsyncEvent event) throws IOException
        {
            System.out.println("onComplete");
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
            }, 5, TimeUnit.SECONDS);
        } else {
            chain.doFilter(request, response);
        }
    }


    public void destroy() {
        //we can close resources here
    }

}