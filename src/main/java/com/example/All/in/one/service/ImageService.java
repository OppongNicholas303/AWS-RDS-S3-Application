package com.example.All.in.one.service;

import com.example.All.in.one.model.ImageItem;
import com.example.All.in.one.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    private final S3Client s3Client;
    private final ImageRepository imageRepository;
    private final ParameterStoreService parameterStoreService;
    private String bucketName; // Cached bucket name

    public ImageService(S3Client s3Client, ImageRepository imageRepository, ParameterStoreService parameterStoreService) {
        this.s3Client = s3Client;
        this.imageRepository = imageRepository;
        this.parameterStoreService = parameterStoreService;
    }

    private String getBucketName() {
        if (bucketName == null) {
            bucketName = parameterStoreService.getParameterValue("/s3-image-upload-app/s3/bucket-name");
            log.info("Fetched bucket name from Parameter Store: {}", bucketName);
        }
        return bucketName;
    }

    public ImageItem uploadImage(MultipartFile file, String description) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }

        String key = UUID.randomUUID().toString() + "-" + originalFilename;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(getBucketName()) // Fetch bucket name dynamically
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        ImageItem imageItem = new ImageItem();
        imageItem.setFilename(originalFilename);
        imageItem.setKey(key);
        imageItem.setUrl(getImageUrl(key));
        imageItem.setSize(file.getSize());
        imageItem.setLastModified(Instant.now());
        imageItem.setUploadDate(Instant.now());
        imageItem.setContentType(file.getContentType());
        imageItem.setDescription(description);
        return imageRepository.save(imageItem);
    }

    private String getImageUrl(String key) {
        return String.format("https://%s.s3.amazonaws.com/%s", getBucketName(), key);
    }

    @Transactional
    public void deleteImage(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(getBucketName())
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            imageRepository.deleteByKey(key);
            log.info("Deleted image with key: {}", key);
        } catch (Exception e) {
            log.error("Error deleting image with key {}: {}", key, e.getMessage());
            throw e;
        }
    }
}
