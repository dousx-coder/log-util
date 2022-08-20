package cn.cruder.logutil.aop;

import cn.cruder.logutil.annotation.AopLogger;
import cn.cruder.logutil.enums.LevelEnum;
import cn.cruder.logutil.pojo.LogInfo;
import cn.cruder.logutil.service.LogService;
import cn.cruder.logutil.utils.DateFormatUtil;
import cn.cruder.logutil.utils.NetworkUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;

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
    @Pointcut("@annotation(cn.cruder.logutil.annotation.AopLogger)")
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
                logService.recordLog(point, result, request, startTime, endTime);
            } catch (Exception e) {
                if (log.isTraceEnabled()) {
                    log.trace("记录日志出错", e);
                }
            }
        }
    }


}