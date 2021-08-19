package com.wdbyte.downbit;


/**
 * @Author Hilda
 * @Description //TODO 本项目将重写 download-bit-master 项目，作为自己Java练手项目
 * @Date 16:47 2021/8/15
 * @Param
 * @returnValue
 **/


import com.wdbyte.downbit.thread.DownloadThread;
import com.wdbyte.downbit.thread.LogThread;
import com.wdbyte.downbit.util.FileUtils;
import com.wdbyte.downbit.util.HttpUtils;
import com.wdbyte.downbit.util.LogUtils;
import com.wdbyte.downbit.util.ThunderUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.CRC32;

/**
 * @ClassName: DownloadMain
 * @Description: 下载器的总入口
 * @author: Hilda   Hilda_quan@163.com
 * @date: 2021/8/15 16:45
 */
public class DownloadMain {

    // 下载线程数量，可根据需求手动修改
    public static int DOWNLOAD_THREAD_NUM = 5;

    // 下载线程池
    private static ExecutorService executor = Executors.newFixedThreadPool(DOWNLOAD_THREAD_NUM + 1);

    // 临时文件后缀名
    public static String FILE_TEMP_SUFFIX = ".temp";


    /**
     * @Author Hilda
     * @Description //TODO main方法入口
     * @Date 16:49 2021/8/15
     * @Param
     * @returnValue
     **/
    public static void main(String[] args) throws Exception {
        System.out.println("请输入下载链接（支持 http/thunder 链接）：");
        Scanner in = new Scanner(System.in);
        String url = in.nextLine();

        // 处理迅雷链接
        url = ThunderUtils.toHttpUrl(url);
        DownloadMain fileDownload = new DownloadMain();

        // 开始下载文件处理
        fileDownload.download(url);
    }


    /**
     * @Author Hilda
     * @Description //TODO 文件下载的主要逻辑
     * @Date 16:53 2021/8/15
     * @Param [url]
     * @returnValue void
     **/
    private void download(String url) throws Exception {
        // 用字符串substring和lastIndexOf，获取url里面的文件名
            // 此处的 HttpUtils 需要自己定义，不能导入已经存在的包
        String fileName = HttpUtils.getHttpFileName(url);

        // 获取文件长度，假如文件未下载，就返回 0. 如果已下载，获取后可方便之后判断文件是否下载完全
        long localFileSize = FileUtils.getFileContentLength(fileName);

        // 获取网络文件的具体大小
        long httpFileContentLength = HttpUtils.getHttpFileContentLength(url);

        // 判断是否已下载完全
        if (localFileSize >= httpFileContentLength) {
            LogUtils.info("{}已经下载完毕，无需重新下载", fileName);
            return;
        }

        // 创建一个装载着 Future接口 的List, 作为存放多线程任务的容器
        List<Future<Boolean>> futureList = new ArrayList<>();

        // 开始准备下载操作
        if (localFileSize > 0) {
            // 大于零：说明之前已经下载过了
            LogUtils.info("开始断点续传 {}", fileName);
        }
        else {
            // 等于零：说明这次是第一次下载
            LogUtils.info("开始下载文件 {}", fileName);
        }
        LogUtils.info("开始下载时间 {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")));

        // 获取开始下载的时间
        long startTime = System.currentTimeMillis();

        // 任务切分，并执行下载
        splitDownload(url, futureList);

        // 获取下载日志，得到下载内容的详情（速度、已下载大小等等）
        LogThread logThread = new LogThread(httpFileContentLength);

        // 将记录子线程下载详情的logThread类，挂载到线程池中运行
        Future<Boolean> future = executor.submit(logThread);
        futureList.add(future);

        // 开始下载任务
        for (Future<Boolean> booleanFuture : futureList) {
            booleanFuture.get();
        }
        LogUtils.info("文件下载完毕 {}，本次下载耗时：{}", fileName, (System.currentTimeMillis() - startTime) / 1000 + "s");
        LogUtils.info("结束下载时间 {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")));


        // 文件合并
        boolean merge = merge(fileName);
        boolean isDelete = false;
        if (merge) {
            // 合并完成，清理分段文件
           isDelete = clearTemp(fileName);
        }

        if (!isDelete) {
            LogUtils.info("临时文件删除失败！");
        }

        // 结束下载
        LogUtils.info("本次文件下载结束");
        System.exit(0);

    }



    /**
     * @Author Hilda
     * @Description //TODO 思路分析：
     *                      这个类的目的在于将整个下载进程拆分为几个子进程
     *                      首先获取整个网络文件的大小。然后除以划分数，得到每个子进程需要下载的文件大小。此处必须单独处理最后一个文件
     *                      进入遍历循环，创建每一个子进程。子进程需要的参数为：起始点、下载区间长度、结束点。
     *                      这里将创建子进程的操作单独封装成一个类 DownloadThread，便于管理。
     *                      创建完成后，添加到futureList中，等待开始运行
     * @Param [url, futureList]
     * @returnValue void
     **/
    private void splitDownload(String url, List<Future<Boolean>> futureList) throws IOException {
        // 获取网络文件的大小
        long httpFileContentLength = HttpUtils.getHttpFileContentLength(url);

        // 任务切分 —— 等分切分。存在整数除法取整，最后一个子文件大小需要单独处理
        long size = httpFileContentLength / DOWNLOAD_THREAD_NUM;

        // 单独处理最后一份文件的大小
        long lastSize = httpFileContentLength - ((httpFileContentLength / DOWNLOAD_THREAD_NUM) * (DOWNLOAD_THREAD_NUM - 1));

        // 获取必要的下载信息
        for (int i = 0; i < DOWNLOAD_THREAD_NUM; i++) {
            // start：下载的起始点
            long start = i * size;
            // downloadWindow：需要下载的区间长度
            long downloadWindow = (i == DOWNLOAD_THREAD_NUM - 1) ? lastSize : size;
            // 结尾点
            long end = start + downloadWindow;

            // 如果当前第一个包的下载起始点不是开头，则要自增
            // 不自增的话，下一个下载起始点就会覆盖掉上一个下载包的末尾了
            // 经尝试，如果没有这句逻辑的话，下载下来的文件有错误
            if (start != 0) {
                start++;
            }

            // 创建下载子进程
            DownloadThread downloadThread = new DownloadThread(url, start, end, i, httpFileContentLength);
            Future<Boolean> future = executor.submit(downloadThread);
            futureList.add(future);
        }
    }


    /**
     * @Author Hilda
     * @Description //TODO 将子线程下载得到的文件合并为一个文件
     * @Date 17:23 2021/8/15
     * @Param [fileName]
     * @returnValue boolean
     **/
    private boolean merge(String fileName) throws IOException {
        LogUtils.info("开始合并文件 {}", fileName);

        byte[] buffer = new byte[1024 * 10];

        int len = -1;  // 文件读取标志
        /**
         * @Author Hilda
         * @Description //TODO 文件合并逻辑
         *                 思路分析：
         *                 先创建一个可以读写文件的类。这个类读写的文件作为真正的下载文件
         *                 然后循环遍历子线程下载的内容。先创建一个可以读写子进程下载文件的的类，然后读性里面的内容，用buffer存储
         *                 最后将buffer里面的内容写进最初创建的读写文件类，合在一起，就成为一个真正的下载文件了
         *                 ----文件合并的核心在于：将子进程下载内容按顺序读写，存在一起，就完成合并了----
         * @Date 14:53 2021/8/17
         * @Param [fileName]
         * @returnValue boolean
         **/
        try (RandomAccessFile oSaveFile = new RandomAccessFile(fileName, "rw")) {
            for (int i = 0; i < DOWNLOAD_THREAD_NUM; i++) {
                // 创建一个读取子线程下载文件的类，然后按下载顺序读取里面的内容，并合并成一个文件
                try (BufferedInputStream bis = new BufferedInputStream(
                        new FileInputStream(fileName + FILE_TEMP_SUFFIX + i)
                )) {
                    while ((len = bis.read(buffer)) != -1) {
                        oSaveFile.write(buffer, 0, len);
                    }
                }
            }
            LogUtils.info("文件合并完毕 {}", fileName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    /**
     * @Author Hilda
     * @Description //TODO 清理临时文件
     * @Date 17:25 2021/8/15
     * @Param [fileName]
     * @returnValue void
     **/
    private boolean clearTemp(String fileName) {
        LogUtils.info("开始清理临时文件 {}-{}0-{}", fileName, FILE_TEMP_SUFFIX, (DOWNLOAD_THREAD_NUM - 1));

        // 删除对应的临时文件
        for (int i = 0; i < DOWNLOAD_THREAD_NUM; i++) {
            File file = new File(fileName + FILE_TEMP_SUFFIX + i);
            file.delete();
        }

        LogUtils.info("临时文件清理完毕 {}-{}0-{}", fileName, FILE_TEMP_SUFFIX, (DOWNLOAD_THREAD_NUM - 1));

        return true;
    }


    public static Long getCRC32(String filepath) throws IOException {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(filepath));
        CRC32 crc = new CRC32();
        byte[] bytes = new byte[1024];
        int cnt;
        while ((cnt = inputStream.read(bytes)) != -1) {
            crc.update(bytes, 0, cnt);
        }
        inputStream.close();
        return crc.getValue();
    }

}
