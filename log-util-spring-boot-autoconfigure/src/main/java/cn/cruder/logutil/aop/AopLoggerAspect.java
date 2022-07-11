package cn.cruder.logutil.aop;

import cn.cruder.logutil.annotation.AopLogger;
import cn.cruder.logutil.enums.DatePattern;
import cn.cruder.logutil.enums.LevelEnum;
import cn.cruder.logutil.properties.LogProperties;
import cn.cruder.logutil.utils.DateFormatUtil;
import cn.cruder.logutil.utils.NetworkUtil;
import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    /**
     * 说明：以引号开始，只包含字母数字或+-=的一串字，长度超过1024，最小匹配，以引号结束
     */
    public static final String PATTERN = "\"[\\w+-=]{1024,}?\"";
    public static final String REPLACE = "\"very long (more than 1024)\"";
    private final LogProperties logProperties;

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
                String describe = getAopLogDescribe(point);
                String declaringTypeName = point.getSignature().getDeclaringTypeName();
                String sigName = point.getSignature().getName();
                Object[] pointArgs = point.getArgs();
                String method = request.getMethod();
                StringBuffer requestUrl = request.getRequestURL();
                LogInfo logInfo = LogInfo.builder()
                        .describe(describe)
                        .requestParam(JSON.toJSONString(pointArgs).replaceAll(PATTERN, REPLACE))
                        .responseResult(JSON.toJSONString(result).replaceAll(PATTERN, REPLACE))
                        .processingTime((endTime - startTime) + "ms")
                        .requestTime(DateFormatUtil.format(new Date(startTime)))
                        .url(requestUrl.toString())
                        .httpMethod(method)
                        .classMethod(declaringTypeName + "." + sigName)
                        .ip(NetworkUtil.getIpAddress(request))
                        .build();
                printLog(logInfo);
            } catch (Exception e) {
                log.error("记录日志出错", e);
            }
        }
    }

    /**
     * 获取注解中对方法的描述信息
     *
     * @param joinPoint 切点
     * @return describe
     */
    private String getAopLogDescribe(JoinPoint joinPoint) {
        AopLogger controllerLog = getAopLogger(joinPoint);
        if (controllerLog == null) {
            return "";
        }
        return controllerLog.describe();
    }


    /**
     * 获取方法上aop注解
     *
     * @param joinPoint 切点
     * @return {@link AopLogger}
     */
    private AopLogger getAopLogger(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(AopLogger.class);
    }


    public void printLog(LogInfo logInfo) {
        LevelEnum levelEnum = LevelEnum.getLevelEnum(logProperties.getLevel());
        switch (levelEnum) {
            case INFO:
                if (log.isInfoEnabled()) {
                    log.info(logProperties.getPrefix() + logInfo.toString() + logProperties.getSuffix());
                }
                break;
            case DEBUG:
            default:
                if (log.isDebugEnabled()) {
                    log.debug(logProperties.getPrefix() + logInfo.toString() + logProperties.getSuffix());
                }
        }


    }

    @Builder
    @Data
    private static class LogInfo {

        /**
         * 描述
         */
        private String describe;

        /**
         * 请求参数
         */
        private String requestParam;

        /**
         * 请求结果
         */
        private String responseResult;

        /**
         * 处理时间 单位:ms
         */
        private String processingTime;

        /**
         * 请求时间
         */
        private String requestTime;

        /**
         * url
         */
        private String url;

        /**
         * 请求方式
         */
        private String httpMethod;

        /**
         * classMethod
         */
        private String classMethod;

        /**
         * ip
         */
        private String ip;

    }
}