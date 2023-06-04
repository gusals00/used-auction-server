package com.auction.usedauction.util.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3BackUpManager {

    private ThreadLocal<BackUpData> backUpHolder = new ThreadLocal<>();
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public void insertBackUpData(String backUpCommand, String path) {
        backUpHolder.get().insert(backUpCommand, path);
    }

    public boolean isBackUpState() {
        return backUpHolder.get() != null;
    }

    public int getInsertSize() {
        return backUpHolder.get().getInsertSize();
    }

    public int getDeleteSize() {
        return backUpHolder.get().getDeleteSize();
    }

    public void backUp() {
        if (backUpHolder.get().isFirstLevel()) {
            // backUp 처리
            // S3에 저장된 객체들 backup되어 삭제
            log.info("[S3 backup rollback] backup insert count = {}", getInsertSize());
            List<BackUpDTO> backUp = backUpHolder.get().getBackUp(BackUpCommand.INSERT);
            for (BackUpDTO backUpDTO : backUp) {
                try {
                    amazonS3.deleteObject(new DeleteObjectRequest(bucket, backUpDTO.getPath()));
                    log.info("[S3 backup file, command = {}, path = {}", backUpDTO.getCommand(), backUpDTO.getPath());
                } catch (AmazonS3Exception ex) {
                    log.error("[S3 backUp transfer error] inserted object delete exception", ex);
                }
            }
        }
        complete();
    }

    public void begin() {
        if (backUpHolder.get() == null) {
            backUpHolder.set(new BackUpData());
        } else {
            backUpHolder.get().nextLevel();
        }
    }

    public void end() {
        if (backUpHolder.get().isFirstLevel()) {
            // S3에 저장된 객체들 삭제
            List<BackUpDTO> backUp = backUpHolder.get().getBackUp(BackUpCommand.DELETE);
            log.info("[S3 commit] delete count = {}", getDeleteSize());
            for (BackUpDTO backUpDTO : backUp) {
                try {
                    amazonS3.deleteObject(new DeleteObjectRequest(bucket, backUpDTO.getPath()));
                    log.info("[s3 delete] command = {}, path = {}", backUpDTO.getCommand(), backUpDTO.getPath());
                } catch (AmazonS3Exception ex) {
                    log.error("[S3 파일 삭제 전송 에러] delete exception", ex);
                }
            }
            backUpHolder.get().clean();
        }
        complete();
    }

    private void complete() {
        if (backUpHolder.get().isFirstLevel()) {
            backUpHolder.remove();
            if (backUpHolder.get() == null) {
                log.info("[s3 backup data clear]");
            }
            log.info("[S3 backup end]");
        } else {
            backUpHolder.get().prevLevel();
        }
    }

}
