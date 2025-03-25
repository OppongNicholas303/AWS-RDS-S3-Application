package com.example.All.in.one.service;

import com.example.All.in.one.model.ImageItem;
import com.example.All.in.one.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    private final S3Client s3Client;
    private final ImageRepository imageRepository;

//    private ParameterStoreService parameterStoreService;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public ImageService(S3Client s3Client, ImageRepository imageRepository, ParameterStoreService parameterStoreService) {
        this.s3Client = s3Client;
        this.imageRepository = imageRepository;
//        this.parameterStoreService = parameterStoreService;
    }


    public ImageItem uploadImage(MultipartFile file, String description) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }

        String key = UUID.randomUUID().toString() + "-" + originalFilename;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket("week5-lab-bucket-nicholas")
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        ImageItem imageItem = new ImageItem();
        imageItem.setFileName(originalFilename);
        imageItem.setFilename(originalFilename);
        imageItem.setKey(key);
        imageItem.setUrl(getImageUrl(key));
        imageItem.setFileSize(file.getSize());
        imageItem.setSize(file.getSize()); // Add this line to set the size
        imageItem.setLastModified(Instant.now());
        imageItem.setUploadDate(Instant.now());
        imageItem.setContentType(file.getContentType());
        imageItem.setDescription(description);
        return imageRepository.save(imageItem);
    }

    private String getImageUrl(String key) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, key);
    }

    public Page<ImageItem> getPaginatedImages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return imageRepository.findAll(pageable);
    }

    @Transactional
    public void deleteImage(String key) {
        try {
            // Delete from S3
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);

            // Delete from database
            imageRepository.deleteByKey(key);
            log.info("Deleted image with key: {}", key);
        } catch (Exception e) {
            log.error("Error deleting image with key {}: {}", key, e.getMessage());
            throw e;
        }
    }
}