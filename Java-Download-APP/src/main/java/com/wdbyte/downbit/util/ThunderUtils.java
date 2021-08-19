package com.wdbyte.downbit.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Base64;

/**
 * @ClassName: ThunderUtils
 * @Description: 处理迅雷链接的工具类
 * @author: Hilda   Hilda_quan@163.com
 * @date: 2021/8/15 16:52
 */
public class ThunderUtils {

    private static final String THUNDER = "thunder://";

    /**
     * @Author Hilda
     * @Description //TODO 将传进来的链接进行处理，转换为httpurl的形式
     *                      实例：thunder://QUFmdHA6Ly9zb2Z0OjgwQHh6Lmk1MzAuY24vM0RzTUFY77yI5a6k5YaF5aSW6K6+6K6h······
     * @Param [url]
     * @returnValue java.lang.String
     **/
    /**
     * @Author Hilda
     * @Description //TODO 解密算法分析：
     *                      迅雷下载地址，其实就是将真实地址首尾分别添加AA和ZZ，
     *                      然后用base64编码，再在头部添加thunder://得到的地址
     *                      参考网址：https://blog.csdn.net/lixianlin/article/details/4419784
     * @Date 16:32 2021/8/16
     * @Param [url]
     * @returnValue java.lang.String
     **/
    public static String toHttpUrl(String url) {
        // 首先判断是不是迅雷链接
        if (!isThunderLink(url)) {
            // 不是，直接返回链接
            return url;
        }

        LogUtils.info("当前链接是迅雷链接，开始转换·····");

        // 转换逻辑
        url = url.replaceFirst(THUNDER, "");
        try {
            // base 64 转换
            url = new String(Base64.getDecoder().decode(url.getBytes()), "UTF-8");
            // url 解码
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 去头去尾
        if (url.startsWith("AA")) {
            url = url.substring(2);
        }
        if (url.endsWith("ZZ")) {
            url = url.substring(0, url.length() - 2);
        }

        return url;
    }


    /**
     * @Author Hilda
     * @Description //TODO 检查是否为迅雷链接
     * @Date 16:15 2021/8/16
     * @Param [url]
     * @returnValue boolean
     **/
    private static boolean isThunderLink(String url) {
        // 只要开头有 thunder:// 字样即可
        return url.startsWith(THUNDER);
    }
}
