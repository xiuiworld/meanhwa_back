package com.example.meanhwa_back.storage.service;

import com.example.meanhwa_back.storage.config.StorageProperties;
import com.example.meanhwa_back.storage.dto.ImageUploadResponse;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile({"local", "test"})
public class FakeStorageService implements StorageService {
    private final StorageProperties storageProperties;
    private final ImageFileValidator imageFileValidator;
    private final StorageKeyGenerator storageKeyGenerator;

    public FakeStorageService(
            StorageProperties storageProperties,
            ImageFileValidator imageFileValidator,
            StorageKeyGenerator storageKeyGenerator
    ) {
        this.storageProperties = storageProperties;
        this.imageFileValidator = imageFileValidator;
        this.storageKeyGenerator = storageKeyGenerator;
    }

    @Override
    public ImageUploadResponse uploadImage(MultipartFile file) {
        imageFileValidator.validate(file);
        String key = storageKeyGenerator.generateImageKey(file);
        String imageUrl = storageProperties.getFakeBaseUrl().replaceAll("/+$", "") + "/" + key;
        return new ImageUploadResponse(imageUrl, file.getOriginalFilename(), file.getContentType(), file.getSize());
    }
}
