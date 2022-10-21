package cn.cruder.logutil.pojo;

import lombok.Builder;
import lombok.Data;

/**
 * @author dousx
 */
@Builder
@Data
public final class LogInfo {

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
     * 完成时间
     */
    private String finishTime;

    /**
     * uri
     */
    private String uri;

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
