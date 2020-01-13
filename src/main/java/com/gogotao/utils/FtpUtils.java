package com.gogotao.utils;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class FtpUtils {
    private static final Logger logger = LoggerFactory.getLogger(FtpUtils.class);
    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUsername = PropertiesUtil.getProperty("ftp.username");
    private static String ftpPassword= PropertiesUtil.getProperty("ftp.password");
        private static FTPClient ftpClient;

    public static boolean uploadFile(List<File> fileList) throws IOException{
        logger.info("开始连接ftp服务器");
        boolean result = uploadFile("image", fileList);
        logger.info("开始连接ftp服务器, 结束上传, 上传结果{}", result);
        return result;
    }

    private static boolean uploadFile(String remotePath, List<File> fileList) throws IOException{
        boolean uploaded = false;
        ftpClient = new FTPClient();
        FileInputStream fileInputStream = null;
        try {
            ftpClient.connect(ftpIp);
            if (!ftpClient.login(ftpUsername, ftpPassword)){
                logger.error("登录Ftp服务器失败");
                return false;
            }
            ftpClient.changeWorkingDirectory(remotePath);
            ftpClient.setBufferSize(1024);
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            for (File fileItem : fileList){
                fileInputStream = new FileInputStream(fileItem);
                ftpClient.storeFile(fileItem.getName(), fileInputStream);
            }
            uploaded = true;
        } catch (IOException e) {
            logger.error("登录Ftp服务器期间出现异常", e);
            uploaded = false;
        }finally {
            if (fileInputStream != null){
                fileInputStream.close();
            }
            ftpClient.disconnect();
        }
        return uploaded;
    }
}
