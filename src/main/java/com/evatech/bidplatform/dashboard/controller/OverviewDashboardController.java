package com.evatech.bidplatform.dashboard.controller;

import com.evatech.bidplatform.ApiResponse;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.dashboard.dto.OverviewDashboardResponse;
import com.evatech.bidplatform.dashboard.dto.ProcessingQueueItemResponse;
import com.evatech.bidplatform.dashboard.service.OverviewDashboardService;
import com.evatech.bidplatform.user.entity.User;
import com.evatech.bidplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class OverviewDashboardController extends AbstractController {

    private final OverviewDashboardService dashboardService;
    private final UserRepository userRepository;
    private final ContractService contractService;

    @GetMapping("/overview")
    public OverviewDashboardResponse getOverview(
            Authentication authentication
            ) {
        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);
        return dashboardService.getOverview(user, roles);
    }

    @GetMapping("/processing-queue")
    public ApiResponse<List<ProcessingQueueItemResponse>> getProcessingQueue(
            Authentication authentication
    ) {
        User user = authenticateAndFetchUser(userRepository, authentication);
        List<String> roles = fetchRolesForUser(authentication);
        return ApiResponse.success(
                "Success", contractService.getProcessingQueue(user, roles));
    }


}