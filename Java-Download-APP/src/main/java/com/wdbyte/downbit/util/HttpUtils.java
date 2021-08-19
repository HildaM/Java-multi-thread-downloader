package com.wdbyte.downbit.util;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: HttpUtils
 * @Description: 处理http链接的自定义工具类
 * @author: Hilda   Hilda_quan@163.com
 * @date: 2021/8/15 16:56
 */
public class HttpUtils {

    /**
     * @Author Hilda
     * @Description //TODO 传进url，返回下载文件的名字
     *                      以 https://dcdn.wostatic.cn/app/android/app-release-1.0.10.apk 为例：
     *                      此处意思就是获取 app-release-1.0.10.apk 字符串作为文件名。
     *                      使用了 substring 和 lastIndexOf。最后的 substring 获取从 indexOf + 1 开始到末尾的所有字符
     * @Date 17:01 2021/8/15
     * @Param [url]
     * @returnValue java.lang.String
     **/
    public static String getHttpFileName(String url) {
        // 从后面开始，找到 / 字符，获取它的下标
        int lastIndexOf = url.lastIndexOf("/");
        // 从 / 开始，剪切至末尾，获得名字
        return url.substring(lastIndexOf + 1);
    }


    /**
     * @Author Hilda
     * @Description //TODO 传进文件地址，返回该文件的总长度大小
     * @Date 17:05 2021/8/15
     * @Param [url]
     * @returnValue long
     **/
    public static long getHttpFileContentLength(String url) throws IOException {
        // 先建立网络链接，然后再获取文件大小
        HttpURLConnection httpURLConnection = getHttpUrlConnection(url);
        int contentLength = httpURLConnection.getContentLength();
        httpURLConnection.disconnect();

        return contentLength;
    }



    /**
     * @Author Hilda
     * @Description //TODO 获取 HTTP 链接
     * @Date 17:20 2021/8/16
     * @Param [url, l, endPos]
     * @returnValue java.net.HttpURLConnection
     **/
    public static HttpURLConnection getHttpUrlConnection(String url, long start, Long end) throws IOException {
        HttpURLConnection httpURLConnection = getHttpUrlConnection(url);
        LogUtils.debug("此线程下载内容区间 {}-{}", start, end);

        // 此处对应着项目理论知识 —— 了解详情须看！
        if (end != null) {
            httpURLConnection.setRequestProperty("RANGE", "bytes=" + start + "-" + end);
        }
        else {
            // 如果没有提供endPos，则从start一直下载到末尾
            httpURLConnection.setRequestProperty("RANGE", "bytes=" + start + "-");
        }

        // 获取信息头
        Map<String, List<String>> headerFields = httpURLConnection.getHeaderFields();

        for (String s : headerFields.keySet()) {
            LogUtils.debug("此线程相应头 {}：{}", s, headerFields.get(s));
        }

        return httpURLConnection;
    }

    /**
     * @Author Hilda
     * @Description //TODO 重载方法. 返回一个创建好的网络请求链接
     * @Date 17:21 2021/8/16
     * @Param [url]
     * @returnValue java.net.HttpURLConnection
     * 参数说明：
     *      1. 设置http请求头 HttpURLConnection.setRequestProperty（String key，String value）
     *
     **/
    private static HttpURLConnection getHttpUrlConnection(String url) throws IOException {
        // 引入 java.net 包下的 URL 类
        URL httpUrl = new URL(url);
        // 打开网络链接
        HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
        // 设置http请求头 —— 网络IO方面的知识
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36");
        return httpURLConnection;
    }


    
    /**
     * @Author Hilda
     * @Description //TODO 获取下载链接的 Etag。（但此功能暂时没用）
     * @Date 14:10 2021/8/19
     * @Param 
     * @returnValue 
     **/
    
//    public static String getHttpFileEtag(String url) throws IOException {
//        HttpURLConnection httpUrlConnection = getHttpUrlConnection(url);
//        Map<String, List<String>> headerFields = httpUrlConnection.getHeaderFields();
//        List<String> eTagList = headerFields.get("ETag");
//        httpUrlConnection.disconnect();
//        return eTagList.get(0);
//    }
}
