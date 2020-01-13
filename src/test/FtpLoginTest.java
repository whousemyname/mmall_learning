import org.apache.commons.net.ftp.FTPClient;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FtpLoginTest {

    @Test
    public void test(){
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect("192.168.43.200");
            if (ftpClient.login("ftpuser", "123456")){
                System.out.println("yes");
            }else{
                System.out.println("no");
            }
            File targetFile = new File("D:\\MyProject\\a新区域\\mmall\\target\\mmall\\upload\\448c9c7b-b218-4eb2-940b-6fb8f6042f3b.jpg");
            targetFile.setWritable(true);
            FileInputStream fileInputStream = new FileInputStream(targetFile);
            ftpClient.storeFile(targetFile.getName(), fileInputStream);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setControlEncoding("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
