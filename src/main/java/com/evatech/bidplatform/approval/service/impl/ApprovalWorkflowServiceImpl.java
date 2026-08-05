package com.evatech.bidplatform.approval.service.impl;

import com.evatech.bidplatform.approval.entity.workflow.*;
import com.evatech.bidplatform.approval.repository.workflow.*;
import com.evatech.bidplatform.approval.service.ApprovalWorkflowService;
import com.evatech.bidplatform.approval.service.WorkflowNotificationService;
import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractStatus;
import com.evatech.bidplatform.contract.service.ContractService;
import com.evatech.bidplatform.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ApprovalWorkflowServiceImpl implements ApprovalWorkflowService {

    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowStepTransitionRepository workflowStepTransitionRepository;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final WorkflowNotificationService workflowNotificationService;
    private final ContractService contractService;

    @Transactional
    @Override
    public ContractDocument submitForReview(
            Long contractId,
            String userId,
            User user) {
        ContractDocument contractDocument = contractService.getContract(contractId, user);
        startWorkflow("CONTRACT_APPROVAL", "CONTRACT_DOCUMENT", contractId, userId);
        contractDocument.setStatus(ContractStatus.SUBMITTED_FOR_REVIEW);
        return contractService.saveContract(contractDocument);
    }

    @Override
    public WorkflowInstance startWorkflow(
            final String workflowKey,
            final String businessType,
            final Long businessId,
            final String startedBy
    ) {
        WorkflowDefinition workflowDefinition = workflowDefinitionRepository
                .findByWorkflowKeyAndActiveTrue(workflowKey)
                .orElseThrow(() -> new IllegalArgumentException("Active workflow not found: " + workflowKey));

        WorkflowStep firstStep = workflowStepRepository
                .findFirstByWorkflowDefinitionIdOrderByStepOrderAsc(workflowDefinition.getId())
                .orElseThrow(() -> new IllegalStateException("Workflow has no configured steps"));

        WorkflowInstance workflowInstance = new WorkflowInstance(
                workflowDefinition,
                firstStep,
                businessType,
                businessId,
                startedBy
        );

        WorkflowInstance savedInstance = workflowInstanceRepository.save(workflowInstance);

        WorkflowTask task = new WorkflowTask();
        task.setWorkflowInstance(savedInstance);
        task.setWorkflowStep(firstStep);
        task.setAssignmentType(firstStep.getAssignmentType());
        task.setAssigneeGroupKey(firstStep.getAssigneeGroupKey());

        WorkflowTask savedTask = workflowTaskRepository.save(task);

        if (firstStep.getAssignmentType() == AssignmentType.GROUP) {
            workflowNotificationService.notifyGroupForReview(savedTask);
        }

        return savedInstance;
    }

    @Override
    public WorkflowTask assignTaskToMe(Long taskId, String userId) {
        WorkflowTask task = workflowTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow task not found: " + taskId));

        task.assignToUser(userId, userId);

        return workflowTaskRepository.save(task);
    }

    @Override
    public WorkflowTask submitTask(Long taskId, WorkflowAction action, String userId, String comment) {
        WorkflowTask task = workflowTaskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Workflow task not found: " + taskId));

        validateTaskAssignee(task, userId);

        if (action == WorkflowAction.REJECT) {
            task.reject(comment);
        } else {
            task.approve(comment);
        }

        WorkflowTask completedTask = workflowTaskRepository.save(task);

        WorkflowInstance instance = task.getWorkflowInstance();

        WorkflowStepTransition transition = workflowStepTransitionRepository
                .findByFromStepIdAndAction(task.getWorkflowStep().getId(), action)
                .orElseThrow(() -> new IllegalStateException("No transition configured for action: " + action));

        if (action == WorkflowAction.REJECT) {
            instance.reject();
            workflowInstanceRepository.save(instance);
            workflowNotificationService.notifyRequesterWorkflowRejected(instance);
            return completedTask;
        }

        if (Boolean.TRUE.equals(transition.getCompletesWorkflow())) {
            instance.complete();
            workflowInstanceRepository.save(instance);
            workflowNotificationService.notifyRequesterWorkflowCompleted(instance);
            return completedTask;
        }

        WorkflowStep nextStep = transition.getToStep();
        instance.moveToStep(nextStep);
        workflowInstanceRepository.save(instance);

        WorkflowTask nextTask = new WorkflowTask();
        nextTask.setWorkflowInstance(instance);
        nextTask.setWorkflowStep(nextStep);
        nextTask.setAssignmentType(nextStep.getAssignmentType());
        nextTask.setAssigneeGroupKey(nextStep.getAssigneeGroupKey());

        WorkflowTask savedNextTask = workflowTaskRepository.save(nextTask);

        if (nextStep.getAssignmentType() == AssignmentType.GROUP) {
            workflowNotificationService.notifyGroupForReview(savedNextTask);
        }

        return completedTask;
    }

    private void validateTaskAssignee(WorkflowTask task, String userId) {
        if (task.getStatus() != WorkflowTaskStatus.ASSIGNED) {
            throw new IllegalStateException("Task must be assigned before submitting");
        }

        if (!userId.equals(task.getAssigneeUserId())) {
            throw new AccessDeniedException("Task is assigned to another user");
        }
    }
}