package com.wdbyte.downbit.thread;

import com.wdbyte.downbit.DownloadMain;
import com.wdbyte.downbit.util.FileUtils;
import com.wdbyte.downbit.util.HttpUtils;
import com.wdbyte.downbit.util.LogUtils;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

/**
 * @ClassName: DownloadThread
 * @Description: 多线程下载工具类
 * @author: Hilda   Hilda_quan@163.com
 * @date: 2021/8/16 16:54
 */
public class DownloadThread implements Callable<Boolean> {

    // 每次读取的数据块大小
    private static final int BYTE_SIZE = 1024 * 100;

    // 下载链接
    private final String url;

    // 下载开始位置
    private final long startPos;

    // 要下载的文件区块大小
    private Long endPos;

    // 标识多线程下载切分的第几部分
        // 此处必须要使用 Integer，只有这样才可以执行 part != null
    private final Integer part;

    // 文件总大小
    private final Long contentLength;

    // 构造器
    public DownloadThread(String url, long startPos, long endPos, Integer part, long contentLength) {
        this.url = url;
        this.startPos = startPos;
        this.endPos = endPos;
        this.part = part;
        this.contentLength = contentLength;
    }


    /**
     * @Author Hilda
     * @Description //TODO 下载算法逻辑
     * @Date 17:01 2021/8/16
     * @Param []
     * @returnValue java.lang.Boolean
     * 参数说明：
     *      1. trim() 方法用于删除字符串的头尾空白符
     *      2. addAndGet(long delta)  以原子方式将delta与当前值相加，并返回相加后的值。相当于后置加加
     *
     **/
    @Override
    public Boolean call() throws Exception {
        // 判断 url 是否有效
        if (null == url || url.trim().equals("")) {
            throw new RuntimeException("下载路径不正确");
        }

        // 获取文件名。同时生成临时文件名
        String httpFileName = HttpUtils.getHttpFileName(url);
        if (part != null) {
            httpFileName = httpFileName + DownloadMain.FILE_TEMP_SUFFIX + part;
        }

        // 获取本地临时文件大小，以便于继续下载
        Long localFileContentLength = FileUtils.getFileContentLength(httpFileName);
        // 此处使用AtomicLong，为了实现原子操作
        // 此处意为：更新当前已下载文件的大小
        LogThread.LOCAL_FINISH_SIZE.addAndGet(localFileContentLength);

        // 进行文件的大小判断
        if (localFileContentLength >= endPos - startPos) {
            LogUtils.info("{} 已经下载完毕，无需重复下载", httpFileName);
            // 更新已完成线程数
            LogThread.DOWNLOAD_FINISH_THREAD.addAndGet(1);
            return true;
        }

        // 假如 endPos 等于文件总长度，则表示整个文件下载完全了
        if (endPos.equals(contentLength)) {
            endPos = null;
        }


        // 开始建立网络链接
        HttpURLConnection httpURLConnection = HttpUtils.getHttpUrlConnection(
                url, startPos + localFileContentLength, endPos
        );

        // 获取输入流
        try (InputStream input = httpURLConnection.getInputStream();
             BufferedInputStream bis = new BufferedInputStream(input);
             RandomAccessFile oSaveFile = new RandomAccessFile(httpFileName, "rw")) {
            // 下载文件逻辑
            oSaveFile.seek(localFileContentLength);   // 将文件指针指向已下载文件的末尾
            byte[] buffer = new byte[BYTE_SIZE];      // 存储下载下来的文件
            int len = -1;
            // 读到文件末尾会返回-1
            while ((len = bis.read(buffer)) != -1) {  // 读取下载信息，存到buffer中
                oSaveFile.write(buffer, 0, len);   // 将buffer的内容写到oSaveFile中
                // 更新已下载文件的大小
                LogThread.DOWNLOAD_SIZE.addAndGet(len);
            }
        } catch (FileNotFoundException e) {
            LogUtils.error("ERROR! 要下载的文件路径不存在 {}", url);
            return false;
        } catch (Exception e) {
            LogUtils.error("下载出现异常");
            e.printStackTrace();
            return false;
        } finally {
            // 无论下载是否成功，都最后都需要关闭链接
            httpURLConnection.disconnect();
            // 更新已完成线程数
            LogThread.DOWNLOAD_FINISH_THREAD.addAndGet(1);
        }

        return true;
    }
}
