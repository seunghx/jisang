package com.jisang.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**/
/**
 * 
 * 지상 어플리케이션은 쇼핑몰과 유사한 어플리케이션인 만큼 상품 이미지가 어플리케이션의 데이터의 대부분을 차지한다. 이를 관계형 데이터베이스에
 * 저장하는 것보다는 클라우드 스토리지에 저장하는 것이 낫다고 생각하여 클라우드 스토리지로 AWS S3를 선택하게 되었다. 이 클래스는 AWS
 * S3에 {@link MultipartFile} 데이터 연산을 수행한다.
 * 
 * @author leeseunghyun
 *
 */
@Repository
public class S3MultipartDAO implements MultipartDAO {

    // Instance Fields
    // ==========================================================================================================================

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AmazonS3 s3Client;
    @Autowired
    @Qualifier("imageTrashCan")
    private List<String> imageTrashCan;

    @Value("${aws.s3.bucket-name}")
    private String s3BucketName;

    // Methods
    // ==========================================================================================================================

    /**
     * 
     * 파일을 aws S3에 업로드한다.
     * 
     * 메서드가 많이 길고 try-catch 절이 중첩되어 복잡해 보이나 업로드 수행 중 예외가 발생할 경우에 대한 처리를 위해 꼭 필요한
     * 부분이라 복잡해 보이게 만들게 되었다. 만약 업로드 중 예외가 발생한다면 이미 S3에 업로드된 일부 이미지는 삭제되어야만 한다. 아래
     * 메서드를 이런 동작을 처리하나 만약 삭제 연산 중 에외가 발생할 경우 후에 스케줄러에 의해 일괄 삭제될 수 있기 위해
     * {@code imageTrashCan}에 삭제되어야 할 이미지의 url을 {@code add} 한다.
     * 
     * 
     * @throws IllegalArgumentException
     *             인자 {@code files}가 empty 배열일 때 발생
     * @throws DataAccessException
     *             {@code InputStream ins = file.getInputStream()}
     * @throws AmazonClientException
     *             {@link TransferManager#upload(String, String, InputStream, ObjectMetadata)
     * 
     */
    public List<String> upload(MultipartFile... files) {

        if (Objects.isNull(files) || files.length == 0) {
            throw new IllegalArgumentException("Argumet files is empty.");
        }

        List<String> uploaded = new ArrayList<>();

        try {
            Arrays.stream(files)
                  .forEach(file -> {
                      ObjectMetadata metaData = new ObjectMetadata();
                      metaData.setContentLength(file.getSize());
                      metaData.setContentType(file.getContentType());

                      String uploadedFilename = System.nanoTime() + "_" + file.getOriginalFilename();

                      PutObjectRequest putObjectRequest = null;

                      try (InputStream ins = file.getInputStream()) {

                          putObjectRequest = 
                                  new PutObjectRequest(s3BucketName, uploadedFilename, ins, metaData);
                          putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);

                          s3Client.putObject(putObjectRequest);

                          uploaded.add(s3Client.getUrl(s3BucketName, uploadedFilename).toString());

                      } catch (IOException e) {
                          logger.info("Converting {} to RuntimeExceptoin.", e.toString());

                          throw new RuntimeException(e);
                      }
                  });
         } catch (RuntimeException e) {

             if (logger.isInfoEnabled()) {
                logger.info("Uploading images failed due to occurrence of {}", e.toString());
             }

             if (!uploaded.isEmpty()) {
                logger.info("Starting to delete aleady uploaded images.");

                try {
                    deleteAll(uploaded.toArray(new String[uploaded.size()]));
                } catch (AmazonClientException ex) {
                    logger.error("Deleting files from amazon S3 failed due to", ex);
 
                    logger.info(
                           "Putting image urls into image trash can. Images in amazon S3 will be deleted by scheduler.");

                    imageTrashCan.addAll(uploaded);

                    e.addSuppressed(ex);
                }
            }
            throw e;
        }

        return uploaded;
    }

    /**
     * S3 버켓내의 파일 삭제 연산. 실제 연산은 {@link #deleteOne(String), #deleteAll(String...)}
     * 메서드를 호출하여 수행한다.
     * 
     * @throws AmazonClientException
     *             {@link #deleteOne(String), #deleteAll(String...)}
     */
    public void delete(String... fileNames) {
        if (fileNames == null || fileNames.length == 0) {
            throw new IllegalArgumentException("Arguments fileNames is empty.");
        }

        if (fileNames.length == 1)
            deleteOne(fileNames[0]);
        else
            deleteAll(fileNames);
    }

    /**
     * 
     * 한 개의 파일에 대한 삭제 연산을 수행.
     * 
     * @throws AmazonClientException
     *             S3 삭제 연산 중 오류가 발생할 경우 이 예외가 던져진다.
     * 
     */
    private void deleteOne(String fileName) {

        logger.debug("Deteting single file object from S3. Deleting file name : {}", fileName);

        s3Client.deleteObject(s3BucketName, fileName);
    }

    /**
     * 
     * 여러 개의 파일에 대한 삭제 연산을 수행.
     * 
     * @throws AmazonClientException
     *             S3 삭제 연산 중 오류가 발생할 경우 이 예외가 던져진다.
     * 
     */
    private void deleteAll(String... fileNames) {

        logger.debug("Deteting multiple file objects from S3.");

        Arrays.asList(fileNames).forEach(file -> logger.debug("Deleting file name : {}", file));

        DeleteObjectsRequest dor = new DeleteObjectsRequest(s3BucketName).withKeys(fileNames);
        s3Client.deleteObjects(dor);
    }

}
