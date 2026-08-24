package com.evatech.bidplatform.bid.service.impl;

import com.evatech.bidplatform.approval.service.ApprovalService;
import com.evatech.bidplatform.bid.dto.request.GenerateBidDocumentRequest;
import com.evatech.bidplatform.bid.dto.request.SubmitBidForApprovalRequest;
import com.evatech.bidplatform.bid.dto.response.*;
import com.evatech.bidplatform.bid.entity.*;
import com.evatech.bidplatform.bid.repository.BidDocumentRepository;
import com.evatech.bidplatform.bid.repository.BidRepository;
import com.evatech.bidplatform.bid.repository.GeneratedDocumentRepository;
import com.evatech.bidplatform.bid.service.BidTemplateService;
import com.evatech.bidplatform.bid.service.FileStorageService;
import com.evatech.bidplatform.bid.mapper.BidDocumentMapper;
import com.evatech.bidplatform.dashboard.service.BidPreparationService;
import com.evatech.bidplatform.user.entity.User;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BidPreparationServiceImpl implements BidPreparationService {

    private final BidRepository bidRepository;
    private final GeneratedDocumentRepository generatedDocumentRepository;
    private final BidTemplateService bidTemplateService;
    private final BidDocumentMapper bidDocumentMapper;
    private final FileStorageService fileStorageService;
    private final BidDocumentRepository bidDocumentRepository;
    private final ApprovalService approvalService;

    @Override
    public GeneratedBidDocumentResponse generateBidDocument(Long contractId, GenerateBidDocumentRequest request, User user, List<String> roles) {
        GeneratedDocument generatedDocument = bidTemplateService.generateBidTemplate(contractId, user, roles);
        Bid bid = generatedDocument.getBid();
        return new GeneratedBidDocumentResponse(contractId, bid.getId(), bidDocumentMapper.toDocumentResponse(generatedDocument), "COMPLETED", "Bid template generated successfully");
    }

    @Override
    @Transactional
    public LatestBidDocumentResponse getLatestBidDocument(Long contractId, User user, List<String> roles) {

        GeneratedDocument document = generatedDocumentRepository.findFirstByBidContractDocumentIdOrderByGeneratedAtDesc(contractId).orElse(null);
        if (document == null) {
            return new LatestBidDocumentResponse(contractId,
                    false, null,
                    true,"No generated document found");
        }
        return new LatestBidDocumentResponse(contractId,true,
                bidDocumentMapper.toDocumentResponse(document),
                true,"Success");
    }

    @Override
    @Transactional
    public BidDocumentDetailsResponse getDocumentDetails(Long contractId, Long documentId, User user, List<String> roles) {

        GeneratedDocument document = generatedDocumentRepository.findByIdAndBidContractDocumentId(documentId, contractId).orElseThrow(() -> new NotFoundException("Document not found"));

        return bidDocumentMapper.toDocumentResponse(document);
    }

    @Override
    @Transactional
    public Resource downloadDocument(Long contractId, Long documentId, User user, List<String> roles) {

        GeneratedDocument document = generatedDocumentRepository.findByIdAndBidContractDocumentId(documentId, contractId).orElseThrow(() -> new NotFoundException("Document not found"));
        return fileStorageService.loadTemplate(document.getStoragePath());
    }

    @Override
    @Transactional
    public Resource previewDocument(Long contractId, Long documentId, User user, List<String> roles) {

        GeneratedDocument document = generatedDocumentRepository.findByIdAndBidContractDocumentId(documentId, contractId).orElseThrow(() -> new NotFoundException("Document not found"));

        return fileStorageService.loadTemplate(document.getStoragePath());
    }

    @Override
    public UploadedBidDocumentResponse uploadCompletedBid(Long contractId, Long sourceGeneratedDocumentId, BidDocumentType documentType, MultipartFile file, User user, List<String> roles) {

        GeneratedDocument sourceTemplate = generatedDocumentRepository.findById(sourceGeneratedDocumentId).orElseThrow(() -> new NotFoundException("Generated template not found"));

        Bid bid = sourceTemplate.getBid();

        String storagePath = fileStorageService.storeTemplate(file.getOriginalFilename(), file);
        BidDocument document = BidDocument.builder().bid(bid).documentType(documentType).fileName(file.getOriginalFilename()).storagePath(storagePath).contentType(file.getContentType()).fileSize(file.getSize()).uploadedBy(user.getEmail()).build();
        document = bidDocumentRepository.save(document);
        bid.setStatus(BidStatus.IN_PROGRESS);
        bidRepository.save(bid);

        return new UploadedBidDocumentResponse(bid.getId(), document.getId(), "SUCCESS", "Document uploaded successfully");
    }

    @Override
    public SubmitBidForApprovalResponse submitForApproval(Long contractId, SubmitBidForApprovalRequest request, User user, List<String> roles) {
        Bid bid = bidRepository.findById(request.bidId()).orElseThrow(() -> new NotFoundException("Bid not found"));
        bid.setStatus(BidStatus.SUBMITTED);
        bid.setSubmittedBy(user.getEmail());
        bid.setSubmittedAt(LocalDateTime.now());
        bidRepository.save(bid);
        approvalService.createApprovalTasks(bid, request.reviewers());
        return new SubmitBidForApprovalResponse(bid.getId(), "SUBMITTED", "Bid submitted for approval successfully");
    }

    @Override
    @Transactional
    public BidPreparationStatusResponse getPreparationStatus(Long contractId, User user, List<String> roles) {
        Bid bid = bidRepository.findFirstByContractDocumentIdOrderByCreatedAtDesc(contractId).
                orElseThrow(() -> new NotFoundException("No bid found"));
        GeneratedDocument latestDocument = bid.getGeneratedDocuments().
                stream().max(Comparator.comparing(GeneratedDocument::getGeneratedAt)).
                orElse(null);
        long totalFields = 0;
        long completedFields = 0;
        if (latestDocument != null) {
            totalFields = latestDocument.getTemplateFields().size();
            completedFields = latestDocument.getTemplateFields().stream().
                    filter(BidTemplateField::isCompleted).count();
        }
        double completionPercentage = totalFields == 0 ? 0 : (completedFields * 100.0) / totalFields;
        return new BidPreparationStatusResponse(
                bid.getId(),
                bid.getStatus().name(),
                totalFields,
                completedFields,
                completionPercentage,
                bid.getDocuments().size());
    }
}

