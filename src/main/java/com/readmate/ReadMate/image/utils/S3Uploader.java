package com.readmate.ReadMate.image.utils;

import com.readmate.ReadMate.common.exception.CustomException;
import com.readmate.ReadMate.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
//import com.amazonaws.services.s3.AmazonS3Client;
//import com.amazonaws.services.s3.model.CannedAccessControlList;
//import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3Uploader {

//    private final AmazonS3Client amazonS3Client;
//
//    @Value("${cloud.aws.s3S.bucket}")
//    private String bucket;
//
//    public String uploadFiles(MultipartFile multipartFile, String dirName) throws IOException {
//        File uploadFile = convert(multipartFile)
//                .orElseThrow(() -> new CustomException(ErrorCode.FILE_CONVERT_FAIL)); // 에러 코드 적용
//        return upload(uploadFile, dirName);
//    }
//
//    public String upload(File uploadFile, String filePath) {
//        try {
//            String fileName = filePath + "/" + UUID.randomUUID() + uploadFile.getName();
//            String uploadImageUrl = putS3(uploadFile, fileName);
//            removeNewFile(uploadFile);
//            return uploadImageUrl;
//        } catch (Exception e) {
//            throw new CustomException(ErrorCode.FILE_UPLOAD_FAIL); // 에러 코드 적용
//        }
//    }
//
//    private String putS3(File uploadFile, String fileName) {
//        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile)
//                .withCannedAcl(CannedAccessControlList.PublicRead));
//        return amazonS3Client.getUrl(bucket, fileName).toString();
//    }
//
//    private void removeNewFile(File targetFile) {
//        if (targetFile.delete()) {
//            System.out.println("File delete success");
//        } else {
//            System.out.println("File delete fail");
//        }
//    }
//
//    private Optional<File> convert(MultipartFile file) throws IOException {
//        File convertFile = new File(System.getProperty("user.dir") + "/" + file.getOriginalFilename());
//        if (convertFile.createNewFile()) {
//            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
//                fos.write(file.getBytes());
//            }
//            return Optional.of(convertFile);
//        }
//        return Optional.empty();
//    }
}