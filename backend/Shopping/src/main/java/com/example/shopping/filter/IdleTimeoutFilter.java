package com.example.shopping.filter;

import com.example.shopping.scheduler.IdleTimeoutMonitor;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(3)
public class IdleTimeoutFilter implements Filter {

    private final IdleTimeoutMonitor monitor;

    public IdleTimeoutFilter(IdleTimeoutMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        monitor.updateAccessTime();
        chain.doFilter(request, response);
    }
}
