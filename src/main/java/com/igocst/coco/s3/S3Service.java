package com.igocst.coco.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.igocst.coco.security.MemberDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Service {
    static { System.setProperty("com.amazonaws.sdk.disableEc2Metadata", "true"); }

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;

    public String upload(MultipartFile multipartFile, String dirName, MemberDetails memberDetails) throws IOException {
        File uploadFile = convert(multipartFile)  // 파일 변환할 수 없으면 에러
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File로 전환을 실패했습니다"));


        try {
            return upload(uploadFile, dirName, memberDetails);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    private String upload(File uploadFile, String dirName, MemberDetails memberDetails) throws NoSuchAlgorithmException {
        //filename을 받고 -> uploadImageUrl을 반환 받음
        // 난수화를 위해 UUID 사용
//        String fileName = dirName + "/" + UUID.randomUUID() + uploadFile.getName();
        String fileName = memberDetails.getNickname();
        String cryptogram = encrypt(fileName);// S3에 저장된 파일 이름
        String uploadImageUrl = putS3(uploadFile, dirName+"/"+cryptogram); // s3로 업로드
        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    //파일명 암호화를 위한 세팅
    public String encrypt(String text) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(text.getBytes());

        return bytesToHex(md.digest());
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    // S3로 업로드
    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(
                new PutObjectRequest(bucket, fileName, uploadFile)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    private void delete(String fileKey) {
        amazonS3Client.deleteObject(bucket, fileKey);
    }
//   public String reupload(MultipartFile file, String currentFilePath, String imageKey) {
//        String fileName =
//    }

    // 로컬에 저장된 이미지 지우기
    // 임시로 생성된 new file을 삭제해준다!
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("파일이 삭제되었습니다");
            return;
        } else {
            log.info("파일 삭제에 실패했습니다");
        }
    }

    // 로컬에 파일 업로드 하기
    //multipartFile을 File타입으로 변환해줌 (변환된 파일을 가지고 put을 해주면 됨) -> ?왓..난 이미 put했는데..!
    private Optional<File> convert(MultipartFile file) throws IOException {
        File convertFile = new File(System.getProperty("user.dir") + "/" + file.getOriginalFilename());
        if (convertFile.createNewFile()) { // 바로 위에서 지정한 경로에 File이 생성됨 (경로가 잘못되었다면 생성 불가능)
            try (FileOutputStream fos = new FileOutputStream(convertFile)) { // FileOutputStream 데이터를 파일에 바이트 스트림으로 저장하기 위함
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }

        return Optional.empty();
    }
}
