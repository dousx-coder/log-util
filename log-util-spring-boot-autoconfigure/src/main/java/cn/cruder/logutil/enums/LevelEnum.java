package cn.cruder.logutil.enums;

import java.util.Locale;

/**
 * AOP日志级别
 *
 * @author dousx
 * @date 2022-04-22 15:04
 */
public enum LevelEnum {
    /**
     * debug
     */
    DEBUG("DEBUG"),
    /**
     * info
     */
    INFO("INFO"),
    ;

    LevelEnum(String level) {
        this.level = level;
    }

    public String level() {
        return level;
    }

    private final String level;

    public static LevelEnum getLevelEnum(String level) {
        LevelEnum[] values = values();
        for (LevelEnum value : values) {
            if (value.level().toLowerCase(Locale.ROOT).equals(level)) {
                return value;
            }
        }
        return DEBUG;
    }
}
