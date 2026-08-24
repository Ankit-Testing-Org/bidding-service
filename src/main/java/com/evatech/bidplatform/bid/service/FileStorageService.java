package com.evatech.bidplatform.bid.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorageService {

    String storeContract(MultipartFile file);

    Resource loadContract(String fileName);

    Path resolvePath(String relativePath);

    Path getTempDirectory();

    String storeTemplate(String fileName, MultipartFile file);
    String storeTemplate(String fileName, XWPFDocument document);

    Resource loadTemplate(String fileName);
    void deleteTemplate(String fileName);
}