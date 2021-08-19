package com.wdbyte.downbit.thread;

import com.wdbyte.downbit.DownloadMain;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @ClassName: LogThread
 * @Description: 多线程下载日志记录
 * @author: Hilda   Hilda_quan@163.com
 * @date: 2021/8/15 17:17
 */
public class LogThread implements Callable<Boolean> {

    public static AtomicLong LOCAL_FINISH_SIZE = new AtomicLong();
    public static AtomicLong DOWNLOAD_SIZE = new AtomicLong();
    public static AtomicLong DOWNLOAD_FINISH_THREAD = new AtomicLong();
    private final long httpFileContentLength;
    private final int DOWN_THREAD_NUM = DownloadMain.DOWNLOAD_THREAD_NUM;   // 此参数由DownloadMain中的下载线程数决定

    // 构造器
    public LogThread(long httpFileContentLength) {
        this.httpFileContentLength = httpFileContentLength;
    }

    /**
     * @Author Hilda
     * @Description //TODO 实时打印下载信息（速度、已下载文件大小等）
     * @Date 16:20 2021/8/17
     * @Param []
     * @returnValue java.lang.Boolean
     **/
    @Override
    public Boolean call() throws Exception {
        int[] downSizeArr = new int[DOWN_THREAD_NUM];   // 优化：直接将DownloadMain里面的参数移到这里，提升代码可读性
        int i = 0;
        double size = 0;
        double mb = 1024d * 1024d;

        // 获取文件总大小
        String httpFileSize = String.format("%.2f", httpFileContentLength / mb);

        // 实时获取下载信息
        while (DOWNLOAD_FINISH_THREAD.get() != DownloadMain.DOWNLOAD_THREAD_NUM) {   // 判断是否下载完全了
            double downloadSize = DOWNLOAD_SIZE.get();

            // 滚动数组，实时更新5个位置，以节省资源
            // 设定为 5 的原因是对应总线程数。因为总线程数为 5，而算平均速度需要总和除以 5
            // 源代码：downSizeArr[++i % 5] = Double.valueOf(downloadSize - size).intValue();
            downSizeArr[++i % DOWN_THREAD_NUM] = Double.valueOf(downloadSize - size).intValue();
            size = downloadSize;

            // 每秒速度
            double fiveSecDownloadSize = Arrays.stream(downSizeArr).sum();
//            int speed = (int)((fiveSecDownloadSize / 1024d) / (i < 5d ? i : 5d));
            int speed = (int)((fiveSecDownloadSize / 1024d) / (i < DOWN_THREAD_NUM ? i : DOWN_THREAD_NUM));

            // 剩余时间
            double surplusSize = httpFileContentLength - downloadSize - LOCAL_FINISH_SIZE.get();
            String surplusTime = String.format("%.1f", surplusSize / 1024d / speed);
            if (surplusTime.equals("Infinity")) {
                surplusTime = "-";
            }

            // 已下的文件大小
            String currentFileSize = String.format("%.2f", downloadSize / mb + LOCAL_FINISH_SIZE.get() / mb);
            String speedLog = String.format("> 已下载 %smb/%smb, 速度 %skb/s, 剩余时间 %ss", currentFileSize, httpFileSize, speed, surplusTime);
            System.out.print("\r");
            System.out.print(speedLog);

            Thread.sleep(1000);
        }

        System.out.println();
        return true;
    }
}
