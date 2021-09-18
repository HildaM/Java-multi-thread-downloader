# Java-multi-thread-downloader

# 项目简介
java多线程实现断点续传下载器。支持迅雷链接及其他下载链接

# 项目特性
- 支持断点续传。未下载完成的任务，再下一次运行的时候，只需要重新输入相同的下载地址，即可从上一次下载结束的地方开始继续下载
- 支持迅雷链接
- 支持多线程加速下载（默认为 5 个线程，可以在 DownloadMain 类中修改私有属性 DOWNLOAD_THREAD_NUM 自定义线程数量

# 使用方法
1. 初次使用，需要使用maven将相关的依赖配置好
2. 打开DownloadMain类，找到main方法启动。然后输入下载地址，按回车即可开始下载
3. 文件默认存储到项目所在的目录
4. 若文件没有一次性下载完成，再次下载时，只需要输入当初的下载链接，就可以接着上次的结束点继续下载。


