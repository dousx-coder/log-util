package cn.cruder.logutil.annotation;

import cn.cruder.logutil.enums.LevelEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author dousx
 * @date 2022-04-22 13:46
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AopLogger {
    /**
     * 描述
     *
     * @return 描述
     */
    String describe() default "";

    /**
     * 每个接口的级别都可能不一样
     *
     * @return {@link LevelEnum}
     */
    LevelEnum level() default LevelEnum.DEBUG;

    /**
     * 指定日志名称
     * <br/>
     * 默认 "controllerLog" 需要提供
     * <pre/>
     * {@code
     *     <!--打印指定业务日志到单独文件-->
     *     <appender name="controllerLogAppender" class="ch.qos.logback.core.rolling.RollingFileAppender">
     *         <append>true</append>
     *         <filter class="ch.qos.logback.classic.filter.LevelFilter">
     *             <level>INFO</level>
     *         </filter>
     *         <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
     *             <fileNamePattern>
     *                 ${LOG_HOME}/${APP_NAME}/%d{yyyy,aux}/%d{MM,aux}/%d{dd,aux}/${APP_NAME}-%d{yyyy-MM-dd-HH}.controller.log.zip
     *             </fileNamePattern>
     *             <!-- 如果当前是按小时保存，则保存 2400 小时（= 100 天）内的日志 -->
     *             <MaxHistory>2400</MaxHistory>
     *             <!-- 日志文件保留的总的最大大小-->
     *             <totalSizeCap>30GB</totalSizeCap>
     *         </rollingPolicy>
     *         <encoder charset="UTF-8">
     *             <charset>UTF-8</charset> <!-- 此处设置字符集，防止中文乱码 -->
     *             <!--格式化输出：%d表示日期，%thread表示线程名，%-5level：级别从左显示5个字符宽度%msg：日志消息，%n是换行符 -->
     *             <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{20}.%M [%line] - %msg%n</pattern>
     *         </encoder>
     *     </appender>
     *
     *     <!--
     *     打印指定业务日志到单独文件
     *         private static final Logger controllerLog = LoggerFactory.getLogger("controllerLog");
     *         controllerLog.info("xxxxx");
     *     -->
     *     <logger name="controllerLog" level="DEBUG" additivity="false">
     *         <appender-ref ref="controllerLogAppender"/>
     *         <appender-ref ref="CONSOLE"/>
     *     </logger>
     * }
     * </pre>
     *
     * @return 日志名称
     */
    String appointLog() default "controllerLog";

    /**
     * 忽略长文本
     *
     * @return false 不忽略
     */
    boolean ignoreLongText() default false;

    /**
     * 是否格式化输出
     *
     * @return false 不忽略
     */
    boolean isFormat() default false;

}
