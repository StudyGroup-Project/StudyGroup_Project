package com.study.focus.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;



//통합 테스트 코드 작성시 가장 위에 어노테이션 삽입 필요
//@ActiveProfiles("test")
//@Import(S3TestConfig.class)

//test 코드의 버킷 생성 방법
@TestConfiguration
public class S3TestConfig {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;
    
    private final S3Client s3Client;

    public S3TestConfig(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    //버킷 생성
    @PostConstruct
    public void createBucketIfNotExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            System.out.println(" S3 test bucket created: " + bucket);
        }
    }
}