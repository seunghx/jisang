package com.jisang.persistence;

import java.util.List;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;

/**
 * 
 * 아마존 S3와 같은 스토리지 서비스에 멀티파트 연산 수행을 목적으로 정의한 인터페이스이다. S3와 같은 스토리지 서비스에는 멀티파트 파일
 * 외에도 어플리케이션 로그를 저장하는 것으로 알고 있다. 예를 들어 {@code CloudStorageDAO}와 같이 인터페이스를 정의하여
 * 멀티파트 파일, 로그 등에 대한 쓰기 연산을 모두 수행할 수 있게 메서드를 정의할까도 생각해 보았으나 멀티파트 파일은 S3에 로그는 다른
 * 클라우드 스토리지 서비스에 저장할 수도 있으므로 이 인터페이스를 멀티 파트에 대한 연산만 정의하였다.
 * 
 * @author leeseunghyun
 *
 */
public interface MultipartDAO {

    public List<String> upload(MultipartFile... files);

    /**
     * 이 메서드(삭제 연산) 실행 중 예외가 발생하였을 경우 최대 두번의 재시도를 수행한다.
     */
    @Retryable(value = { AmazonClientException.class }, maxAttempts = 2, backoff = @Backoff(delay = 1000))
    public void delete(String... fileNames);
}
