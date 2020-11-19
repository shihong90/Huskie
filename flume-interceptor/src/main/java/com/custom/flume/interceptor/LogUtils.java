package com.custom.flume.interceptor;

import org.apache.commons.lang.math.NumberUtils;

/**
 * 做一个合法性效验
 */
class LogUtils {
    static boolean validateStart(String log) {
        if (log == null) return false;

        return log.trim().startsWith("{") || log.trim().endsWith("}");


    }

    static boolean validateEvent(String log) {
        if (log == null) return false;

        // 时间 | json串
        String[] split = log.split("\\|");
        if (split.length!=2)return false;

        //判断服务器时间

        return NumberUtils.isDigits(split[0]) || split[0].length() ==13 || split[1].trim().startsWith("{") || split[1].trim().endsWith("}");

    }
}
