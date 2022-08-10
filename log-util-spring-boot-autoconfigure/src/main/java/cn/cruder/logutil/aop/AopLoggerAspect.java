package cn.cruder.logutil.aop;

import cn.cruder.logutil.annotation.AopLogger;
import cn.cruder.logutil.enums.LevelEnum;
import cn.cruder.logutil.utils.DateFormatUtil;
import cn.cruder.logutil.utils.NetworkUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
@NoArgsConstructor
public class AopLoggerAspect {

    /**
     * 说明：以引号开始，只包含字母数字或+-=的一串字，长度超过1024，最小匹配，以引号结束
     */
    public static final String PATTERN = "\"[\\w+-=]{1024,}?\"";
    public static final String REPLACE = "\"very long (more than 1024)\"";

    /**
     * 默认日志
     */
    private static final Logger defLog = org.slf4j.LoggerFactory.getLogger(AopLoggerAspect.class);

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
                AopLogger controllerLog = getAopLogger(point);
                Logger appointLog = appointLog(controllerLog);
                String describe = getAopLogDescribe(controllerLog);
                String declaringTypeName = point.getSignature().getDeclaringTypeName();
                String sigName = point.getSignature().getName();
                String method = request.getMethod();
                StringBuffer requestUrl = request.getRequestURL();
                Object[] pointArgs = point.getArgs();
                HashMap<Object, Object> requestParamMap = new HashMap<>();
                String[] parameterNames = ((MethodSignature) point.getSignature()).getParameterNames();
                if (pointArgs != null && parameterNames != null && pointArgs.length != 0 && pointArgs.length == parameterNames.length) {
                    // parameterNames是参数名
                    // pointArgs是参数值 一一对应
                    for (int i = 0; i < pointArgs.length; i++) {
                        requestParamMap.put(parameterNames[i], pointArgs[i]);
                    }
                }
                LogInfo logInfo = LogInfo.builder()
                        .describe(describe)
                        .requestParam(getObject(requestParamMap, ignoreLongText(controllerLog)))
                        .responseResult(getObject(result, ignoreLongText(controllerLog)))
                        .processingTime((endTime - startTime) + "ms")
                        .requestTime(DateFormatUtil.format(new Date(startTime)))
                        .url(requestUrl.toString())
                        .httpMethod(method)
                        .classMethod(declaringTypeName + "." + sigName)
                        .ip(NetworkUtil.getIpAddress(request))
                        .build();
                printLog(logInfo, aopLoggerLevel(controllerLog), appointLog, isFormat(controllerLog));
            } catch (Exception e) {
                if (log.isTraceEnabled()) {
                    log.trace("记录日志出错", e);
                }
            }
        }
    }

    private Logger appointLog(AopLogger controllerLog) {
        String appointLogName = appointLogName(controllerLog);
        Logger appointLog = defLog;
        if (!ObjectUtils.isEmpty(appointLogName)) {
            try {
                Logger logger = LoggerFactory.getLogger(appointLogName);
                if (logger != null) {
                    appointLog = logger;
                }
            } catch (Throwable e) {
                defLog.warn("获取指定Logger失败:{},采用默认Logger:{}", appointLogName, defLog);
            }
        }
        return appointLog;
    }

    private Object getObject(Object obj, Boolean ignoreLongText) {
        Object result = null;
        try {
            if (ignoreLongText) {
                result = JSON.toJSONString(obj).replaceAll(PATTERN, REPLACE);
            } else {
                result = JSON.toJSONString(obj);
            }
            result = JSON.parseObject(String.valueOf(result));
        } catch (Exception e) {
            if (log.isTraceEnabled()) {
                log.trace("参数转换异常:{}", String.valueOf(obj), e);
            }
        }
        return result;

    }

    /**
     * 获取注解中对方法的描述信息
     *
     * @param aopLogger {@link AopLogger}
     * @return describe
     */
    private String getAopLogDescribe(AopLogger aopLogger) {
        if (aopLogger == null) {
            return "";
        }
        return aopLogger.describe();
    }

    /**
     * 获取注解中对方法的描述信息
     *
     * @param aopLogger {@link AopLogger}
     * @return describe
     */
    private String appointLogName(AopLogger aopLogger) {
        if (aopLogger == null) {
            return "";
        }
        return aopLogger.appointLog();
    }

    private Boolean ignoreLongText(AopLogger aopLogger) {
        if (aopLogger == null) {
            return false;
        }
        return aopLogger.ignoreLongText();
    }

    private Boolean isFormat(AopLogger aopLogger) {
        if (aopLogger == null) {
            return false;
        }
        return aopLogger.isFormat();
    }

    /**
     * 获取注解中对方法的描述信息
     *
     * @param aopLogger {@link AopLogger}
     * @return describe
     */
    private LevelEnum aopLoggerLevel(AopLogger aopLogger) {
        if (aopLogger == null) {
            return LevelEnum.DEBUG;
        }
        return aopLogger.level();
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


    public void printLog(LogInfo logInfo, LevelEnum level, Logger log, Boolean isFormat) {
        switch (level) {
            case INFO:
                if (log.isInfoEnabled()) {
                    if (isFormat) {
                        log.info("\r\n{}", JSON.toJSONString(logInfo,
                                SerializerFeature.PrettyFormat,
                                SerializerFeature.WriteDateUseDateFormat,
                                SerializerFeature.WriteMapNullValue,
                                SerializerFeature.WriteNullListAsEmpty));
                    } else {
                        log.info("{}", JSON.toJSONString(logInfo));
                    }
                }
                break;
            case DEBUG:
            default:
                if (log.isDebugEnabled()) {
                    if (isFormat) {
                        log.debug("\r\n{}", JSON.toJSONString(logInfo,
                                SerializerFeature.PrettyFormat,
                                SerializerFeature.WriteDateUseDateFormat,
                                SerializerFeature.WriteMapNullValue,
                                SerializerFeature.WriteNullListAsEmpty));
                    } else {
                        log.debug("{}", JSON.toJSONString(logInfo));
                    }
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
        private Object requestParam;

        /**
         * 请求结果
         */
        private Object responseResult;

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