package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.dashboard.dto.DeadlineDto;
import com.evatech.bidplatform.dashboard.dto.Priority;
import com.evatech.bidplatform.dashboard.entity.Proposal;
import com.evatech.bidplatform.dashboard.repository.ProposalRepository;
import com.evatech.bidplatform.dashboard.service.DashboardDeadlineService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DashboardDeadlineServiceImpl
        implements DashboardDeadlineService {

    private final ProposalRepository proposalRepository;

    @Override
    public List<DeadlineDto> getDeadlines() {

        return proposalRepository
                .findUpcomingDeadlines(
                        LocalDate.now(),
                        PageRequest.of(0, 5))
                .stream()
                .map(this::toDeadlineDto)
                .toList();
    }

    private DeadlineDto toDeadlineDto(
            Proposal proposal) {

        long daysRemaining = ChronoUnit.DAYS.between(
                LocalDate.now(),
                proposal.getSubmissionDate());

        return DeadlineDto.builder()
                .proposalId(proposal.getId())
                .proposalName(proposal.getTitle())
                .dueDate(proposal.getSubmissionDate())
                .daysRemaining(daysRemaining)
                .priority(determinePriority(daysRemaining))
                .build();
    }

    private Priority determinePriority(
            long daysRemaining) {

        if (daysRemaining <= 1) {
            return Priority.CRITICAL;
        }

        if (daysRemaining <= 3) {
            return Priority.HIGH;
        }

        if (daysRemaining <= 7) {
            return Priority.MEDIUM;
        }

        return Priority.LOW;
    }
}