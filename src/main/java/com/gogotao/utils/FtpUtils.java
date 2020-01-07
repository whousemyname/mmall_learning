package com.gogotao.utils;

import org.apache.commons.net.ftp.FTPClient;

public class FtpUtils {
    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUsername = PropertiesUtil.getProperty("ftp.username");
    private static String ftpPassword = PropertiesUtil.getProperty("ftp.passpword");

    public FtpUtils(String ip, int port, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    private String ip;
    private int port;
    private String username;
    private String password;
    private FTPClient ftpClient;
}
