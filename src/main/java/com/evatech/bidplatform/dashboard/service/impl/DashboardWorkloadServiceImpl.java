package com.evatech.bidplatform.dashboard.service.impl;

import com.evatech.bidplatform.approval.entity.ApprovalStatus;
import com.evatech.bidplatform.approval.repository.ApprovalTaskRepository;
import com.evatech.bidplatform.dashboard.dto.ReviewWorkloadDto;
import com.evatech.bidplatform.dashboard.service.DashboardWorkloadService;
import com.evatech.bidplatform.user.dto.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardWorkloadServiceImpl
        implements DashboardWorkloadService {

    private final ApprovalTaskRepository approvalTaskRepository;

    @Override
    public ReviewWorkloadDto getWorkload() {

        return ReviewWorkloadDto.builder()
                .legalReviews(
                        approvalTaskRepository
                                .countByStatusAndAssignedRole(
                                        ApprovalStatus.PENDING,
                                        RoleType.LEGAL_REVIEWER))
                .financeReviews(
                        approvalTaskRepository
                                .countByStatusAndAssignedRole(
                                        ApprovalStatus.PENDING,
                                        RoleType.FINANCE_REVIEWER))
                .commercialReviews(
                        approvalTaskRepository
                                .countByStatusAndAssignedRole(
                                        ApprovalStatus.PENDING,
                                        RoleType.COMMERCIAL_REVIEWER))
                .managerApprovals(
                        approvalTaskRepository
                                .countByStatusAndAssignedRole(
                                        ApprovalStatus.PENDING,
                                        RoleType.MANAGEMENT_REVIEWER))
                .build();
    }
}
