package com.evatech.bidplatform.ai.service;

import com.evatech.bidplatform.ai.entity.AiEntity;
import com.evatech.bidplatform.ai.repository.AiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRequestLoggerService {

    private final AiRepository aiRepository;

    @Transactional
    public AiEntity logRequest(Long contractId,
                               String requestedBy) {
        AiEntity ai = new AiEntity();
        ai.setRequestedAt(LocalDateTime.now());
        ai.setRequestedBy(requestedBy);
        ai.setContractId(contractId);
        return aiRepository.save(ai);
    }
}
