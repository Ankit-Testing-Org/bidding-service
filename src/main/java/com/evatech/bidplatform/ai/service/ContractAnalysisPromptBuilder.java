package com.evatech.bidplatform.ai.service;

import com.evatech.bidplatform.contract.entity.ContractDocument;
import com.evatech.bidplatform.contract.entity.ContractPageText;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContractAnalysisPromptBuilder {

    public String buildPrompt(
            ContractDocument contractDocument,
            List<ContractPageText> pages
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an expert bid contract analyst.\n\n");

        prompt.append("Analyse the following bidding contract and identify important highlights.\n");
        prompt.append("Return only valid JSON.\n");
        prompt.append("Do not return markdown.\n");
        prompt.append("Do not invent missing information.\n");
        prompt.append("Every highlight must include the source page number.\n\n");

        prompt.append("Return JSON in this exact structure:\n");
        prompt.append("{\n");
        prompt.append("  \"highlights\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"category\": \"Deadline | Risk | Eligibility | Financial | Legal | Scope | Submission | Other\",\n");
        prompt.append("      \"title\": \"short title\",\n");
        prompt.append("      \"description\": \"clear explanation\",\n");
        prompt.append("      \"pageNumber\": 1,\n");
        prompt.append("      \"riskLevel\": \"LOW | MEDIUM | HIGH | CRITICAL\",\n");
        prompt.append("      \"recommendedAction\": \"recommended action for bid team\",\n");
        prompt.append("      \"confidenceScore\": 0.0\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");

        prompt.append("Contract file name: ");
        prompt.append(contractDocument.getOriginalFileName());
        prompt.append("\n\n");

        prompt.append("Contract text by page:\n\n");

        for (ContractPageText page : pages) {
            prompt.append("PAGE ");
            prompt.append(page.getPageNumber());
            prompt.append(":\n");
            prompt.append(page.getText());
            prompt.append("\n\n");
        }

        return prompt.toString();
    }
}
