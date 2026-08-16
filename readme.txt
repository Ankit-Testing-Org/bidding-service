What each role does
----------------------------------------------------------------
ADMIN
Plain Text
1 - Access everything
2 - Assign users
3 - Configure workflows
4 - Manage users and roles

CONTRACT_OWNER
Plain Text
1 - Upload contract
2 - Edit own contract
3 - Qualify/unqualify lots
4 - Generate bids
5 - Submit contract for review
6 - View own contracts

LEGAL_REVIEWER
Plain Text
1 - View assigned contracts
2 - Add legal review comments
3 - Approve/reject legal review

FINANCE_REVIEWER
Plain Text
1 - View assigned contracts
2 - Review pricing/costing
3 - Approve/reject finance review

COMMERCIAL_REVIEWER
Plain Text
1 - Commercial assessment
2 - Market/pricing review

MANAGER
Plain Text
1 - Final approval authority
2 - Can approve bid submission
3 - Can approve contract award decisions
----------------------------------------------------------------

FLOW STEPS :
       1) -  uploadContract
                      (Controller : ContractController, @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE))
       	- // STEP 1) VALIDATE FILE
       	- // STEP 2) STORE FILE
       	- // STEP 3) UPDATE CONTRACT DOCUMENT DB
        - // STEP 4) Extract text and save pages //IGNORE
        - // STEP 5) ANALYSE THE CONTRACT. //STEP 5 & 6 Combined
        - // STEP 6) Extract lots and save them
        - // STEP 7) SEND EMAIL TO BIDDERS. // WE CAN DISCUSS FURTHER.

        // EMAIL WILL BE TRIGGERED TO BIDDERS.

        2) - It will be assigned to bidder.
                      (Controller : ContractAssignmentController, @PostMapping("/{contractId}/assign")

        3) - Once contract is openend it will opened Contract Analysed Screen , this service will be used both for analysis and
         renalaysis of contract. If analysis already done it will return contract.
                      (Controller : ContractAnalyserController, @GetMapping("/{contractId}/analyse/contract"))

        4) - Once highlights are shown on left user can mark highlighs correct / incorrect and comments if want reanalysis of points.
                      (Controller : ContractHighlightController,
                          @PostMapping("/contract/highlights/{highlightId}/approve"),
                          @PostMapping("/contract/highlights/{highlightId}/reject"),
                          @PostMapping("/contract/highlights/{highlightId}/reanalyse")

                          REPEAT STEP 4) TILL ITS QUALIFY OR REJECT

        5)- User can see lots in list, once click on lot comes lot details... Where user can mark it as qualified or unqualified.
                      (Controller : ContractLotController,
                      @GetMapping("/{contractId}/fetch/contract/lots"),
                      @GetMapping("/{contractId}/lots/{lotNumber}/qualify"),
                      @GetMapping("/{contractId}/lots/{lotNumber}/unqualify"),


        6)- If user marks it unqualified then lot will be red and Analysis button won't appear-

        7)- If user marks it qualified then lot will be green and Analysis button will appear

        8)- On click of analysis button it will analyse lot and show highlights of lot.
                      (Controller : ContractLotController,     @GetMapping("/api/contracts/lots/{contractLotId}/analyse")

        9)- User have option to mark analysis correct or renalayse based on comments.
                      (Controller : ContractLotController,
                       @PostMapping("/{lotId}/reanalyse")

        10)- After complete analysis user marks analysis complete
                      (Controller : ContractLotController,
                          @PostMapping("/{lotId}/analysis/approve"),
                          @PostMapping("/{lotId}/analysis/reject")

        11) - User will assign bid task.
              (Controller : BidController,     @PostMapping("/{contractId}/assign")
)
        12) - AI will generate a template based on AI lots.
            (Controller : BidTemplateController,
                          @PostMapping("/{contractId}/generate-template"))

        13) - User will download AI Template.
         (Controller : BidTemplateController,
                 @GetMapping("/contracts/{contractId}/documents/{documentId}/download")

        14)- User upload bid in template.
                      (Controller : BidController, @PostMapping("/{bidId}/upload-documents")
)

       15)- Update contract with document template
                             (Controller : BidController, @PostMapping("/{bidId}/template/{templateId}/update-contract")
)

       16)- Fetch updated contract
                                     (Controller : BidController, @PostMapping("/{bidId}/template/{templateId}/update-contract")
        )