package cn.cruder.logutil.service;

import cn.cruder.logutil.annotation.AopLogger;
import cn.cruder.logutil.autoconfiguration.TaskExecutorConfigurer;
import cn.cruder.logutil.constant.LogConstant;
import cn.cruder.logutil.enums.LevelEnum;
import cn.cruder.logutil.pojo.LogInfo;
import cn.cruder.logutil.utils.DateFormatUtil;
import cn.cruder.logutil.utils.NetworkUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;

/**
 * @author dousx
 */
public class LogService {
    public static final String NAME = "cruder_logService";
    private static final Logger defLog = org.slf4j.LoggerFactory.getLogger(LogService.class);
    /**
     * 说明：以引号开始，只包含字母数字或+-=的一串字，长度超过1024，最小匹配，以引号结束
     */
    public static final String PATTERN = "\"[\\w+-=]{1024,}?\"";
    public static final String REPLACE = "\"very long (more than 1024)\"";

    /**
     * 异步记录日志,是避免拼接参数影响响应时间
     *
     * @param point     {@link ProceedingJoinPoint}
     * @param result    result
     * @param request   {@link HttpServletRequest}
     * @param startTime 请求时间
     * @param endTime   完成时间
     * @param traceId   traceId需要传递当前线程
     */
    @Async(TaskExecutorConfigurer.LOG_POOL)
    public void recordLog(ProceedingJoinPoint point, Object result, HttpServletRequest request, long startTime, long endTime, String traceId) {
        try {
            MDC.put(LogConstant.TRACE_ID, traceId);
            AopLogger controllerLog = getAopLogger(point);
            LevelEnum level = aopLoggerLevel(controllerLog);
            Logger appointLog = appointLog(controllerLog);
            if (!checkPrintLog(level, appointLog)) {
                return;
            }
            String describe = getAopLogDescribe(controllerLog);
            String declaringTypeName = point.getSignature().getDeclaringTypeName();
            String sigName = point.getSignature().getName();
            String method = request.getMethod();
            Object[] pointArgs = point.getArgs();
            HashMap<Object, Object> requestParamMap = new HashMap<>();
            String[] parameterNames = ((MethodSignature) point.getSignature()).getParameterNames();
            if (pointArgs != null && parameterNames != null && pointArgs.length != 0 && pointArgs.length == parameterNames.length) {
                // parameterNames是参数名
                // pointArgs是参数值 一一对应
                for (int i = 0; i < pointArgs.length; i++) {
                    if (pointArgs[i] instanceof MultipartFile) {
                        MultipartFile mf = (MultipartFile) pointArgs[i];
                        HashMap<String, Object> hashMap = new HashMap<>(4);
                        hashMap.put("size", mf.getSize());
                        hashMap.put("originalFilename", mf.getOriginalFilename());
                        hashMap.put("contentType", mf.getContentType());
                        hashMap.put("name", mf.getName());
                        requestParamMap.put(parameterNames[i], hashMap);
                        continue;
                    }
                    requestParamMap.put(parameterNames[i], pointArgs[i]);
                }
            }

            Boolean ignoreLongText = ignoreLongText(controllerLog);
            LogInfo logInfo = LogInfo.builder()
                    .describe(describe)
                    .requestParam(getObject(requestParamMap, ignoreLongText, appointLog))
                    .responseResult(getObject(result, ignoreLongText, appointLog))
                    .processingTime((endTime - startTime) + "ms")
                    .requestTime(DateFormatUtil.format(new Date(startTime)))
                    .finishTime(DateFormatUtil.format(new Date(endTime)))
                    .uri(request.getRequestURI())
                    .httpMethod(method)
                    .classMethod(declaringTypeName + "." + sigName)
                    .ip(NetworkUtil.getIpAddress(request))
                    .build();
            printLog(logInfo, level, appointLog, isFormat(controllerLog));
        } finally {
            MDC.remove(LogConstant.TRACE_ID);
        }

    }

    private Boolean checkPrintLog(LevelEnum aopLogLevel, Logger log) {
        if (log.isDebugEnabled() && LevelEnum.DEBUG.equals(aopLogLevel)) {
            return true;
        }
        if (log.isInfoEnabled() && LevelEnum.INFO.equals(aopLogLevel)) {
            return true;
        }
        return false;
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

    private Object getObject(Object obj, Boolean ignoreLongText, Logger appointLog) {
        Object result = null;
        try {
            if (ignoreLongText) {
                result = JSON.toJSONString(obj).replaceAll(PATTERN, REPLACE);
            } else {
                result = JSON.toJSONString(obj);
            }
            result = JSON.parseObject(String.valueOf(result));
        } catch (Exception e) {
            if (appointLog.isTraceEnabled()) {
                appointLog.trace("参数转换异常:{}", String.valueOf(obj), e);
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


    private void printLog(LogInfo logInfo, LevelEnum level, Logger log, Boolean isFormat) {
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


}
