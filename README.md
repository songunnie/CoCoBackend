# 꿈꿔왔던 사이드 프로젝트가 시작되는 곳, Coco!
![CocoBanner](https://user-images.githubusercontent.com/103922744/183601123-611da9c2-7cf1-4320-bff1-c2d5b0ca7206.jpg)
<br>
<br>
<br>
<br>

## 1. 프로젝트 개요
- 개발기간: 2022.06.24 ~ 2022.07.29
- 참여인원: 3명
- 소개: 사이드 프로젝트 기획/구인 서비스
- 주소: <a href='https://www.cocoding.xy' target='_blank'>Coco 방문하기</a>
<br>
<br>

## 2. 사용기술
- Backend: `Java 11` `Spring Boot` `Spring Data JPA`
- Database: `AWS RDS` `MySQL` `H2`
- Security: `Spring Security`
- Cloud: `AWS S3` `AWS EC2`
- CI/CD: `Github Actions`
<br>
<br>

## 3. ERD 설계
<p align="center">
 <img width="652" alt="스크린샷 2022-08-09 오후 5 15 17" src="https://user-images.githubusercontent.com/103922744/183599831-272abb85-f55e-4e1f-8d7f-16daffcc523f.png">
</p>
<br>
<br>

## 4. 프로젝트 
#### Architecture Version 1
<img width="1549" alt="스크린샷 2022-08-09 오후 2 20 22" src="https://user-images.githubusercontent.com/103922744/183616139-859ea582-38e9-4cdb-b712-982170e8b7fe.png">
<br>
<br>


## 5. 맡은 기능
대표 기능: `댓글` / `프로필 페이지`
- 게시글 상세페이지 댓글 CRUD
- 프로필 페이지를 위한 회원정보 획득
- 프로필 페이지를 위한 회원정보 수정
- 닉네임 중복 방지
- 프로필 이미지 업로드 - S3 연동
- 이미지 파일 확장자 필터
- XSS 보안
<br>
<br>

## 6. 어려움을 선물 받은 기능
### 🎞 프로필 이미지 S3 연동  
> #### 6-1 기존 방식
- 처음에는 프로필 기능을 맡게 되었을 때 '내가 과연 할 수 있을까?'라며 기대 반, 우려 반을 가지고 몇번이나 자문해야했던 기능이었습니다.  
- 먼저, 업로드 기능은 이런 생각을 가지고 구현됐습니다.  
  - 유저 A가 프로필 '꽃'라는 파일명의 이미지를 저장하고, 유저 B 또한 '꽃'이라는 파일명 이미지를 저장했을 때, 각자 다른 이미지임에도 둘 중 한 이미지만 프론트쪽으로 보내준다는 단점이 있다는 것을 알게 되었습니다.
     -  따라서 UUID를 통해 파일명을 난수화 시켜, 유저가 선택한 파일명이 같아도 겹치지 않게 구현했습니다.
<details>
<summary>UUID 이용한 기존 코드</summary>
<div markdown="1">

```java
private String upload(File uploadFile, String dirName) {
    String fileName = dirName + "/" + UUID.randomUUID() + uploadFile.getName();
    String uploadImageUrl = putS3(uploadFile, fileName); 
    removeNewFile(uploadFile);
    return uploadImageUrl;
}
    
private String putS3(File uploadFile, String fileName) {
    amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
    return amazonS3Client.getUrl(bucket, fileName).toString();
}
```
</div>
</details>  
<br>  

> #### 6-2 개선한 방식
- 여기서 한가지 고민이 생겼던게, 유저 A가 n번 이미지를 변경했을 때, n 이전의 모든 이미지 파일이 아직 S3에 쌓여있다는 점이 었습니다.  
- 파일명을 '유저 고유값인 email 이나, nickname으로 하는 건 어떨까?'  
  - 그럼 해결되는게 있었는데, 바로 유저가 프로필 이미지를 재변경 했을 때 기존 파일에 덮어씌워지는 구조라 따로 수정-삭제로직이 필요없고, 파일명이 겹치지 않는다는 것이었습니다.  
- 위에 로직은 꽤나 합리적이지만, 반대로 유저의 고유값이 노출될 수 있다는 점을 우려해서 SHA-256을 이용해 파일명을 암호화하는 로직으로 구현해봤습니다.  
  - 파일명에는 nickname이 아닌 email을 사용한 이유는, nickname은 중복은 안되지만, 추후 변경할 수 있는 부분이기에 회원가입 후 변하지 않는 값인 email이 더 안전하다 생각했기 때문입니다.
<details>
<summary>개선된 코드</summary>
<div markdown="1">

```java
private String upload(File uploadFile, String dirName, MemberDetails memberDetails) throws NoSuchAlgorithmException {
    String fileName = memberDetails.getUsername();
    String cryptogram = encrypt(fileName);
    String uploadImageUrl = putS3(uploadFile, dirName+"/"+cryptogram);
    removeNewFile(uploadFile);
    return uploadImageUrl;
}

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


private String putS3(File uploadFile, String fileName) {
    amazonS3Client.putObject(
            new PutObjectRequest(bucket, fileName, uploadFile)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
    return amazonS3Client.getUrl(bucket, fileName).toString();
}

```
</div>
</details>  
<br>
<br>  

## 7. 회고 / 느낀점
> 프로젝트 개발 회고 글: https://velog.io/@songunnie/WIL-07312022
