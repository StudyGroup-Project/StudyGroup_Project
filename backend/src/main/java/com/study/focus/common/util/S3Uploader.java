package com.study.focus.common.util;


import com.study.focus.common.dto.FileDetailDto;
import com.study.focus.common.exception.BusinessException;
import com.study.focus.common.exception.CommonErrorCode;
import com.study.focus.common.exception.UserErrorCode;
import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Uploader
{
    private  final S3Template s3Template;
    // s3 bucket name
    @Value("${spring.cloud.aws.s3.bucket}")
    private  String bucket;

    //파일당 최대 파일 size
    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxSizeByte;

    //전체 파일의 최대 size
    @Value("${spring.servlet.multipart.max-request-size}")
    private DataSize requestMaxByte;

    //업로드 가능한 파일 List
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "tiff", "heic",
            "ppt", "pptx",
            "xls", "xlsx", "csv",
            "hwp","txt"
    );

    /*

    파일 업로드 방식 순서
    1.  파일의 메타 데이터 생성
    2. 파일의 메타 데이터 DB 에 저장
    2.  메타 데이터를 이용해 파일 업로드
    이렇게 하면 DB 문제면 업로드 전 롤백
    만약 DB 성공 후  파일 업로드 과정에서 업로드 문제면 내부 자체 롤백으로 해결하고 예외 던지기
    하지만 속도 측면에서 커넥션 풀을 오랫동안 잡고 있는 문제는 있을 수 있음
     */

    /*
    파일 수정 방식 순서(이 부분은 참고하지마세요. 나중에 수정 필요)
    1. 새로운 파일에 대한 메타 데이터 생성
    2. 해당 파일을 aws s3에 업로드(임시 업로드)
    3. 만약 s3에 모두 업로드에 성공했다면 db에 해당 key를 원래 키로 대체
    -> 수정 파일의 개수가 더 많으면 db에 추가, 작다면 db에 기존 키 삭제
    4.0 db에도 성공적으로 반영했다면 기존 key들 aws에서 삭제(임시 키가 원본 키로 변신!)
    4.1 db에서 예외가 발생한다면 예외를 잡아서 임시로 추가한 키들을 삭제!
     */


    //파일에 메타 데이터 생성
    public FileDetailDto makeMetaData(MultipartFile file){
        validateFile(file);
        String originalFilename = file.getOriginalFilename();
        String key = getUniqueFileName(originalFilename);
        String contentType = file.getContentType();
        return FileDetailDto.builder()
                .originalFileName(originalFilename)
                .key(key)
                .contentType(contentType)
                .fileSize(file.getSize())
                .build();
    }

    //단일 파일 업로드
    public void uploadFile(String upLoadKey,MultipartFile file){
        validateFile(file);
        fileUpLoad(file, upLoadKey);
    }



    //여러 파일 업로드
    //하나라도 실패하면 기존에 업로드한 파일들 삭제
    public void uploadFiles(List<String> upLoadKeys, List<MultipartFile> files){
        List<String> successfulKeys = new ArrayList<>();
        long totalSize = getTotalSize(files);
        if(totalSize > requestMaxByte.toBytes()){
            throw  new BusinessException(UserErrorCode.FILE_TOO_LARGE);
        }
        try{
            IntStream.range(0,files.size())
                    .forEach(i -> {
                        uploadFile(upLoadKeys.get(i),files.get(i));
                        successfulKeys.add(upLoadKeys.get(i));
                    });
        }
        catch (BusinessException e){
            cleanUpS3File(successfulKeys);
            throw e;
        }
    }

    //단일 파일 수정
    public void correctFile(String upLoadKey, MultipartFile file){
        uploadFile(upLoadKey,file);
    }

    // 여러 파일 수정(파일의 개수가 다르면 추가 작업필요 -> 만약 수정한 파일이 더 많다면 파일 추가 작업, 적다면 파일 삭제 작업 필요)
    // DB를 사용해야하기 때문에 서비스 레이어에서  추가 작업 필요
    public void correctFiles(List<String> keys, List<MultipartFile> files) {
        IntStream.range(0, files.size()).forEach(i ->
                uploadFile(keys.get(i), files.get(i))
        );
    }



    //파일 하나에 대한 url 반환
    public String getUrlFile(String key){
        validateKey(key);
        try{
            return  s3Template.createSignedGetURL(bucket,key,Duration.ofMinutes(10)).toString();
        }catch (S3Exception e){
            log.error("s3 파일 가져오기에 실패했습니다. key: {}, Error:{}",key,e.getMessage());
            throw  new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR,e);
        }
    }

    //여러 파일에 대한 url 반환
    //실패하면 전부 실패
    public List<String> getUrlFiles(List<String> keys){
        if(keys == null || keys.isEmpty()) {
            throw  new BusinessException(UserErrorCode.FILE_NOT_FOUND);}
        return keys.parallelStream().map(this::getUrlFile).collect(Collectors.toList());
    }

    
    //개발자가 직접 파일을 삭제하는 메서드가 아님
    //스케줄러에 의해서 파일 자동 삭제 예정
    public void deleteFile(String key){
        validateKey(key);
        try{
            s3Template.deleteObject(bucket,key);
        }catch (S3Exception  e){
            log.error("s3_File_Delete Fail. key: {}, Error: {}",key ,e.getMessage());
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR,e);
        }
    }

    private void validateKey(String key) {
        if(!StringUtils.hasText(key)){
            log.error("File key Not found : {}", key);
            throw  new BusinessException(UserErrorCode.FILE_NOT_FOUND);
        }
    }
    //내부 파일 검증
    private  void validateFile(MultipartFile file){
        if(file == null || file.isEmpty()){
            throw  new BusinessException(UserErrorCode.FILE_EMPTY);
        }
        String originalFileName = file.getOriginalFilename();
        if(originalFileName == null || originalFileName.isBlank()){
            throw  new BusinessException(UserErrorCode.FILENAME_MISSING);
        }
        if(file.getSize() > maxSizeByte.toBytes()){
            throw  new BusinessException(UserErrorCode.FILE_TOO_LARGE);
        }

        String extension = getExtension(originalFileName);
        if(!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(UserErrorCode.INVALID_FILE_TYPE);
        }
    }

    //파일 이름에서 확장자만 가져오는 함수
    private String getExtension(String fileName) {
        if(fileName == null || fileName.isBlank())
            return "";
        int lastDotIndex = fileName.lastIndexOf(".");
        if(lastDotIndex == -1 || lastDotIndex == fileName.length()-1) return "";
        return fileName.substring(lastDotIndex+1).toLowerCase();
    }

    //전체 파일 사이즈 체크
    private long getTotalSize(List<MultipartFile> files) {
        return files.stream().
                filter(file -> file != null && !file.isEmpty())
                .mapToLong(MultipartFile::getSize)
                .sum();
    }

    //s3에 파일 업로드
    private void fileUpLoad(MultipartFile file, String key) {
        try{
            s3Template.upload(bucket, key, file.getInputStream());
        }catch ( IOException e){
            throw new BusinessException(UserErrorCode.FILE_UPLOAD_FAIL,e);
        }
        catch (S3Exception e){
            log.error("s3 파일 업로드에 실패했습니다. key: {}, Error: {}",key ,e.getMessage());
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR,e);
        }
    }


    //s3 업로드 도중 실패하면 기존에 성공한 파일을 s3에 제거 -> 트랜잭션의 롤백과 동일
    private void cleanUpS3File(List<String> keysToDelete){
        if(keysToDelete == null || keysToDelete.isEmpty()) return;
        keysToDelete.forEach(this::deleteFile);
    }

    //파일에대한 Key 생성
    private String getUniqueFileName(String originalFilename) {
        return UUID.randomUUID().toString() + "_" + originalFilename;
    }




}
