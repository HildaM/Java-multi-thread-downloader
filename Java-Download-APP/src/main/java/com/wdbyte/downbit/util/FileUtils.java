package com.wdbyte.downbit.util;

import java.io.File;

/**
 * @ClassName: FileUtils
 * @Description: 处理与文件相关的工具类
 * @author: Hilda   Hilda_quan@163.com
 * @date: 2021/8/15 16:57
 */
public class FileUtils {

    /**
     * @Author Hilda
     * @Description //TODO 传进文件名，返回该文件的文件总长度
     * @Date 17:05 2021/8/15
     * @Param [fileName]
     * @returnValue long
     **/
    public static long getFileContentLength(String fileName) {
        File file = new File(fileName);
        // 先判断是否存在，再判断是否是一个文件
        return file.exists() && file.isFile() ? file.length() : 0;
    }
}
