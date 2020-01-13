package com.gogotao.service.impl;

import com.gogotao.service.IFileService;
import com.gogotao.utils.FtpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service("iFileService")
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
            List<File> fileList = new ArrayList<>();
            fileList.add(targetFile);
            if (!FtpUtils.uploadFile(fileList)){
                return null;
            }
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件失败", e);
            return null;
        }
        return targetFile.getName();
    }
}
