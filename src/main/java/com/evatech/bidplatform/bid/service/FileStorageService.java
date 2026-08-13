package com.evatech.bidplatform.bid.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {

    String storeContract(MultipartFile file);

    Path resolvePath(String relativePath);

    Path getTempDirectory();

    String storeTemplate(String fileName, MultipartFile file);

    Resource loadTemplate(String fileName);
    void deleteTemplate(String fileName);
}