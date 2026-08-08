package com.evatech.bidplatform.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineDto {

    private Long proposalId;

    private String proposalName;

    private LocalDate dueDate;

    private Long daysRemaining;

    private Priority priority;
}