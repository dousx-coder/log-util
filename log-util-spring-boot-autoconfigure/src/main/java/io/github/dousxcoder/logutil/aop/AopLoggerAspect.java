package io.github.dousxcoder.logutil.aop;

import io.github.dousxcoder.logutil.annotation.AopLogger;
import io.github.dousxcoder.logutil.constant.LogConstant;
import io.github.dousxcoder.logutil.service.LogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 日志
 *
 * @author dousx
 * @date 2022-04-22 13:46
 */
@Slf4j
@Aspect
@AllArgsConstructor
public class AopLoggerAspect {

    public static final String NAME = "cruder_aopLoggerAspect";
    private final LogService logService;

    /**
     * <br/>
     * 方法用 {@link AopLogger}修饰,都打印log
     */
    @Pointcut("@annotation(io.github.dousxcoder.logutil.annotation.AopLogger)")
    public void recordLogAspect() {
    }


    /**
     * 记录日志
     *
     * @param point 切入点
     * @return result
     */
    @Around("recordLogAspect()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        Object result = null;
        HttpServletRequest request = null;
        long startTime = System.currentTimeMillis();
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            request = servletRequestAttributes.getRequest();
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            result = e.getMessage();
            // 抛出,交个业务处理
            throw e;
        } finally {
            try {
                long endTime = System.currentTimeMillis();
                logService.recordLog(point, result, request, startTime, endTime, MDC.get(LogConstant.TRACE_ID));
            } catch (Exception e) {
                if (log.isTraceEnabled()) {
                    log.trace("记录日志出错", e);
                }
            }
        }
    }


}