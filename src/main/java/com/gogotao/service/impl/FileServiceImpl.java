package com.gogotao.service.impl;

import com.gogotao.service.IFileService;
import org.apache.ibatis.annotations.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.util.UUID;

public class FileServiceImpl implements IFileService {
    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public String upload(MultipartFile file, String path){
        String fileName = file.getOriginalFilename();
        String fileExtendName = fileName.substring(fileName.lastIndexOf(".") + 1);
        String uploadFileName = UUID.randomUUID() + "." +fileExtendName;
        logger.info("开始上传文件,文件原名称:{},上传的路径:{},文件新名称:{}", fileName, path, uploadFileName);
        File fileDir = new File(path);
        if (!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(fileDir, uploadFileName);
        try {
            file.transferTo(targetFile);

            //todo 将targetFile上传到FTP服务器上

            //todo 上传完之后删除upload下的文件

        } catch (IOException e) {
            logger.error("上传文件失败", e);
            return null;
        }
        return targetFile.getName();
    }
}
