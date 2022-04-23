package cn.cruder.logutil.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

/**
 * @author dousx
 * @date 2022-04-22 13:44
 */

@ConfigurationProperties(value = "cruder.log")
@PropertySource(value = "classpath:application.yml", encoding = "UTF-8")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LogProperties {
    /**
     * 日志开始前缀
     */
    private String prefix = "";
    /**
     * 日志结束前缀
     */
    private String suffix = "";

    /**
     * 打印级别
     * <p/>
     * 支持列表如下
     * <li/>debug (默认级别)
     * <li/>info
     */
    private String level = "debug";
}