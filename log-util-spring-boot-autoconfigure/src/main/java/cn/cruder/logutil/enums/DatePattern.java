package cn.cruder.logutil.enums;

/**
 * @author dousx
 * @date 2022-07-02 14:44
 */
public enum DatePattern {
    NORM_DATETIME_MS_PATTERN("yyyy-MM-dd HH:mm:ss.SSS"),;

    DatePattern(String pattern) {
        this.pattern = pattern;
    }

    private String pattern;

    public String pattern() {
        return pattern;
    }
}
