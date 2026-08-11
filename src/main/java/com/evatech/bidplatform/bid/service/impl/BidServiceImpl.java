package com.evatech.bidplatform.bid.service.impl;

import com.evatech.bidplatform.bid.dto.ParsedTemplateFieldValue;
import com.evatech.bidplatform.bid.dto.request.AssignBidRequest;
import com.evatech.bidplatform.bid.dto.response.BidTemplateFieldUpdateResponse;
import com.evatech.bidplatform.bid.dto.response.DeleteDocumentResponse;
import com.evatech.bidplatform.bid.entity.*;
import com.evatech.bidplatform.bid.exception.BidNotFoundException;
import com.evatech.bidplatform.bid.exception.BidTemplateFieldNotFoundException;
import com.evatech.bidplatform.bid.exception.DocumentNotFoundException;
import com.evatech.bidplatform.bid.repository.BidDocumentRepository;
import com.evatech.bidplatform.bid.repository.BidRepository;
import com.evatech.bidplatform.bid.repository.BidTemplateFieldRepository;
import com.evatech.bidplatform.bid.service.BidService;
import com.evatech.bidplatform.bid.service.UploadedTemplateParser;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractDocumentFieldValue;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.document.service.FileStorageService;
import com.evatech.bidplatform.user.dto.RoleType;
import com.evatech.bidplatform.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final ContractService contractService;
    private final BidDocumentRepository bidDocumentRepository;
    private final FileStorageService fileStorageService;
    private final UploadedTemplateParser uploadedTemplateParser;
    private final BidTemplateFieldRepository bidTemplateFieldRepository;

    @Override
    @Transactional
    public Bid assignBid(Long contractId, AssignBidRequest request, User user, List<String> roles) {

        ContractDocument contract = contractService.getContract(contractId, user, roles);
        Bid bid = Bid.builder().contractDocument(contract).status(BidStatus.ASSIGNED).assignedTo(request.getAssignedTo()).assignedBy(user.getEmail()).assignedAt(LocalDateTime.now()).build();

        return bidRepository.save(bid);
    }

    @Override
    @Transactional
    public BidDocument uploadDocument(Long bidId, MultipartFile file, BidDocumentType documentType, User user) {

        Bid bid = bidRepository.findById(bidId).orElseThrow(() -> new BidNotFoundException(bidId + " not found"));
        String storagePath = fileStorageService.storeTemplate(file.getOriginalFilename(), file);
        BidDocument document = BidDocument.builder().bid(bid).fileName(file.getOriginalFilename()).storagePath(storagePath).documentType(documentType).uploadedBy(user.getEmail()).build();
        return bidDocumentRepository.save(document);
    }

    @Override
    @Transactional
    public BidDocument uploadCompletedBid(Long bidId, MultipartFile file, User user, List<String> roles) {

        Bid bid = bidRepository.findById(bidId).orElseThrow(() -> new BidNotFoundException("Bid not found: " + bidId));

        // Optional security checks
        validateBidAccess(bid, user, roles);
        String storagePath = fileStorageService.storeTemplate(file.getOriginalFilename(), file);

        BidDocument document = BidDocument.builder().fileName(file.getOriginalFilename()).storagePath(storagePath).documentType(BidDocumentType.COMPLETED_BID).uploadedBy(user.getEmail()).build();

        bid.getDocuments().add(document);
        bid.setStatus(BidStatus.USER_REVIEWED);
        bidRepository.save(bid);
        return bidDocumentRepository.save(document);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadDocument(Long documentId, User user, List<String> roles) {

        BidDocument document = bidDocumentRepository.findById(documentId).orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        return fileStorageService.loadTemplate(document.getStoragePath());
    }

    @Override
    public DeleteDocumentResponse deleteDocument(Long documentId, User user, List<String> roles) {

        BidDocument document = bidDocumentRepository.findById(documentId).orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        fileStorageService.deleteTemplate(document.getStoragePath());
        bidDocumentRepository.delete(document);
        return DeleteDocumentResponse.builder().documentId(documentId).fileName(document.getFileName()).message("Document deleted successfully").build();
    }

    @Override
    @Transactional
    public List<BidTemplateFieldUpdateResponse> uploadTemplateToContract(
            Long bidId,
            Long bidDocumentId,
            Long contractId,
            User user,
            List<String> roles) {

        BidDocument document = bidDocumentRepository.findById(bidDocumentId)
                        .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        Resource template = fileStorageService.loadTemplate(document.getStoragePath());

        contractService.getContract(contractId, user, roles);

        List<ParsedTemplateFieldValue> parsedValues = uploadedTemplateParser.parse(template);

        List<BidTemplateFieldUpdateResponse> responses = new ArrayList<>();

        for (ParsedTemplateFieldValue parsedValue : parsedValues) {
            BidTemplateField templateField =
                    bidTemplateFieldRepository.findByPlaceholder(parsedValue.placeholder())
                            .orElseThrow(() -> new BidTemplateFieldNotFoundException("Template field not found: " + parsedValue.placeholder()));

            ContractDocumentFieldValue fieldValue = contractService.fetchAndUpdateContractDocumentFieldValue(
                            contractId, templateField.getId(), parsedValue.value(), user, roles);

            responses.add(BidTemplateFieldUpdateResponse.builder()
                            .templateFieldId(templateField.getId())
                            .placeholder(templateField.getPlaceholder())
                            .fieldLabel(templateField.getFieldLabel())
                            .fieldType(templateField.getFieldType())
                            .oldValue(null)
                            .newValue(fieldValue.getValue())
                            .build());
        }

        return responses;
    }

    private void validateBidAccess(Bid bid, User user, List<String> roles) {
        if (roles.contains(RoleType.ADMIN.name())) {
            return;
        }
        if (roles.contains(RoleType.BID_MANAGER.name())) {
            return;
        }
        if (user.getEmail().equals(bid.getAssignedTo())) {
            return;
        }
        if (user.getEmail().equals(bid.getCreatedBy())) {
            return;
        }
        throw new AccessDeniedException("You are not authorized to access this bid");
    }
}