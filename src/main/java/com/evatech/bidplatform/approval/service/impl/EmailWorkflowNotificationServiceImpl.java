package com.evatech.bidplatform.approval.service.impl;

import com.evatech.bidplatform.approval.entity.workflow.WorkflowInstance;
import com.evatech.bidplatform.approval.entity.workflow.WorkflowTask;
import com.evatech.bidplatform.approval.repository.workflow.WorkflowGroupMemberRepository;
import com.evatech.bidplatform.approval.service.WorkflowNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailWorkflowNotificationServiceImpl implements WorkflowNotificationService {

    private final JavaMailSender javaMailSender;
    private final WorkflowGroupMemberRepository workflowGroupMemberRepository;


    @Override
    public void notifyGroupForReview(WorkflowTask task) {
        List<String> emails = workflowGroupMemberRepository
                .findEmailAddressesByGroupKey(task.getAssigneeGroupKey());

        if (emails.isEmpty()) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emails.toArray(new String[0]));
        message.setSubject("Review task assigned: " + task.getWorkflowStep().getName());
        message.setText("A new review task is available. Please open the application and assign it to yourself.");

        javaMailSender.send(message);
    }

    @Override
    public void notifyRequesterWorkflowCompleted(WorkflowInstance instance) {
    }

    @Override
    public void notifyRequesterWorkflowRejected(WorkflowInstance instance) {
    }
}