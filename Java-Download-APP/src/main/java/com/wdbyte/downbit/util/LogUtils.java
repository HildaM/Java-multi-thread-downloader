package com.wdbyte.downbit.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @ClassName: LogUtils
 * @Description: 多线程下载器日志工具类
 * @author: Hilda   Hilda_quan@163.com
 * @date: 2021/8/15 17:05
 */
public class LogUtils {

    private static final boolean DEBUG = false;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");

    /**
     * @Author Hilda
     * @Description //TODO 输出日志信息
     * @Date 17:06 2021/8/15
     * @Param [msg, arg]  ---> 第二个参数使用Object数组，因为不知道传进的参数有多少
     * @returnValue void
     **/
    public static void info(String msg, Object... arg) {
        print(msg, " -INFO-", arg);
    }


    /**
     * @Author Hilda
     * @Description //TODO 生成 debug 信息
     * @Date 17:29 2021/8/16
     * @Param [s, arg]
     * @returnValue void
     **/
    public static void debug(String msg, Object... arg) {
        if (DEBUG) {
            print(msg, " -DEBUG-", arg);
        }
    }


    /**
     * @Author Hilda
     * @Description //TODO 打印信息工具类  ---> 统一所有信息的输出格式
     * @Date 17:31 2021/8/16
     * @Param [msg, level, arg]
     * @returnValue void
     **/
    private static void print(String msg, String level, Object... arg) {
        if (arg != null && arg.length > 0) {
            // 这里就解释了为什么之前要用 {} ，是为了在之后统一设置字符串输出
            // 使用 {} 方便将原始信息与通俗语言进行分离，方便参数的传递与最后的处理
            msg = String.format(msg.replace("{}", "%s"), arg);
        }
        String thread = Thread.currentThread().getName();

        System.out.println(LocalDateTime.now().format(dateTimeFormatter) + " " + thread + level + msg);
    }


    /**
     * @Author Hilda
     * @Description //TODO 生成错误报告
     * @Date 17:44 2021/8/16
     * @Param [msg, arg]
     * @returnValue void
     **/
    public static void error(String msg, Object... arg) {
        print(msg, " -ERROR-", arg);
    }
}
