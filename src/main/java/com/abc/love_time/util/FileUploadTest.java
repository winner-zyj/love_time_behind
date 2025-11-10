package com.abc.love_time.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUploadTest {
    
    public static void main(String[] args) {
        // 测试目录创建
        String uploadDir = "d:\\exercise\\love_time_behind\\src\\main\\webapp\\uploads\\heartwall";
        File dir = new File(uploadDir);
        
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("Directory created: " + created);
        } else {
            System.out.println("Directory already exists");
        }
        
        System.out.println("Directory path: " + dir.getAbsolutePath());
    }
}