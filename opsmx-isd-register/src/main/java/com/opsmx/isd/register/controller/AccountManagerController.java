package com.opsmx.isd.register.controller;

import com.opsmx.isd.register.dto.DatasourceRequestModel;
import com.opsmx.isd.register.dto.DatasourceResponseModel;
import com.opsmx.isd.register.dto.Message;
import com.opsmx.isd.register.dto.SaasTrialResponseModel;
import com.opsmx.isd.register.enums.CDType;
import com.opsmx.isd.register.service.AccountSetupService;
import com.opsmx.isd.register.service.SendMessage;
import com.opsmx.isd.register.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@Slf4j
public class AccountManagerController {

    @Autowired
    private AccountSetupService accountSetupService;

    @Autowired
    private SendMessage sendMessage;

    private final Long TIMEOUT_IN_SECONDS = 180L;

    @CrossOrigin(origins = "*")
    @PostMapping(value = "/webhookTrigger", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SaasTrialResponseModel> webhookTrigger(@Valid @RequestBody DatasourceRequestModel
                                                                             dataSourceRequestModel,
                                                                 HttpServletRequest request) {
        try {
            log.info(dataSourceRequestModel.toString());
            if(!Util.rateLimit(request)){
                return new ResponseEntity("You have exceeded the 10 requests in 1 minute limit!",
                        HttpStatus.TOO_MANY_REQUESTS);
            }
            accountSetupService.store(dataSourceRequestModel);
            log.info("User data saved ");
            AtomicReference<Boolean> isSpinnakerSetupComplete = new AtomicReference<>(false);
            AtomicReference<DatasourceResponseModel> atomicReference = new AtomicReference<>();
            CompletableFuture.supplyAsync(() -> {
                atomicReference.set(accountSetupService.setup(dataSourceRequestModel));
                return atomicReference;
            }).orTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS).whenComplete((result, exception) -> {
                if(exception != null) {
                    sendMessage.sendMessageObject(new Message(dataSourceRequestModel.getBusinessEmail(), exception.getMessage()));
                    return;
                }
                DatasourceResponseModel responseModel = atomicReference.get();
                if(responseModel != null && responseModel.getEventProcessed()){
                    log.info("Building spinnaker setup for user {} ", dataSourceRequestModel.getBusinessEmail());
                    isSpinnakerSetupComplete.set(true);
                    // send message to redirect to login page.
                    sendMessage.sendMessageObject(new Message(dataSourceRequestModel.getBusinessEmail(), "success"));
                }else {
                    log.info("Error building spinnaker for user {}", dataSourceRequestModel.getBusinessEmail());
                    // send message to redirect to error page.
                    sendMessage.sendMessageObject(new Message(dataSourceRequestModel.getBusinessEmail(), "failure"));
                }
            });
            SaasTrialResponseModel saasTrialResponseModel = new SaasTrialResponseModel();
            saasTrialResponseModel.setEventProcessed(true);
            saasTrialResponseModel.setEventId(UUID.randomUUID().toString());
            return new ResponseEntity<>(saasTrialResponseModel, HttpStatus.CREATED);
        }catch (Exception e){
            SaasTrialResponseModel saasTrialResponseModel = new SaasTrialResponseModel();
            saasTrialResponseModel.setEventProcessed(false);
            saasTrialResponseModel.setEventId(UUID.randomUUID().toString());
            return new ResponseEntity<>(saasTrialResponseModel, HttpStatus.CONFLICT);
        }
    }


    @CrossOrigin(origins = "*")
    @PostMapping(value = "/webhookTrigger/{cdType}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SaasTrialResponseModel> cdWebhookTrigger(@Valid @RequestBody DatasourceRequestModel dataSourceRequestModel,
                                                                   @PathVariable(name = "cdType") CDType cdType) {

        try {
            log.info("Request received to trigger the webhook for the cd type : {} {}", cdType.name(), dataSourceRequestModel.toString());

            SaasTrialResponseModel saasTrialResponseModel = new SaasTrialResponseModel();
            accountSetupService.store(dataSourceRequestModel, cdType);
            AtomicReference<DatasourceResponseModel> atomicReference = new AtomicReference<>();
            CompletableFuture.supplyAsync(() -> {
                atomicReference.set(accountSetupService.setup(dataSourceRequestModel, cdType));
                return atomicReference;
            }).orTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS).whenComplete((result, exception) -> {
                if(exception != null) {
                    log.error("User registration failed : {}", exception);
                    return;
                }
                log.info("Successfully completed the user registration process");
            });
            saasTrialResponseModel.setEventId(UUID.randomUUID().toString());
            saasTrialResponseModel.setEventProcessed(Boolean.TRUE);
            return new ResponseEntity<>(saasTrialResponseModel, HttpStatus.CREATED);
        }catch (Exception e){
            SaasTrialResponseModel saasTrialResponseModel = new SaasTrialResponseModel();
            saasTrialResponseModel.setEventProcessed(false);
            saasTrialResponseModel.setEventId(UUID.randomUUID().toString());
            return new ResponseEntity<>(saasTrialResponseModel, HttpStatus.CONFLICT);
        }
    }
}