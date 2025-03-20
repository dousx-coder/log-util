package io.github.dousxcoder.logutil.hand;

import io.github.dousxcoder.logutil.constant.LogConstant;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.UUID;


public class TraceIdInterceptor implements HandlerInterceptor {
    public static final String NAME = "cruder_TraceIdInterceptor";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //如果有上层调用就用上层的ID
        String traceId = request.getHeader(LogConstant.TRACE_ID);
        if (Objects.isNull(traceId)) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put(LogConstant.TRACE_ID, traceId);
        request.setAttribute(LogConstant.TRACE_ID, traceId);
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        MDC.remove(LogConstant.TRACE_ID);
    }
}