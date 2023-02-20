package com.opsmx.isd.register.service;

import com.opsmx.isd.register.dto.DatasourceRequestModel;
import com.opsmx.isd.register.dto.DatasourceResponseModel;
import com.opsmx.isd.register.entities.User;
import com.opsmx.isd.register.enums.CDType;
import com.opsmx.isd.register.repositories.UserRepository;
import com.opsmx.isd.register.util.Util;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service("AccountSetupService-v1")
@Slf4j
public class AccountSetupServiceImpl implements AccountSetupService {

    @Value("${automation.webhook.spinnaker.url:#{null}}")
    private String spinnakerAutomationWebhookURL;

    @Value("${automation.webhook.argo.url:#{null}}")
    private String argoAutomationWebhookURL;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void store(DatasourceRequestModel datasourceRequestModel){
        userRepository.save(Util.toUser(datasourceRequestModel));
    }

    @Override
    public void store(DatasourceRequestModel datasourceRequestModel, CDType cdType) {

        User user = Util.toUser(datasourceRequestModel);
        user.setCdType(cdType);
        userRepository.save(user);
    }

    @Override
    public DatasourceResponseModel setup(DatasourceRequestModel datasourceRequestModel){
        return triggerWebhook(datasourceRequestModel, spinnakerAutomationWebhookURL);
    }

    private DatasourceResponseModel triggerWebhook(DatasourceRequestModel datasourceRequestModel, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String,String> uriVariables = new HashMap<>();
        uriVariables.put("user", datasourceRequestModel.getBusinessEmail());
        JSONObject user = new JSONObject();
        user.put("user", datasourceRequestModel.getBusinessEmail());
        String data = user.toString();
        HttpEntity<Object> httpEntity= new HttpEntity<>(data, headers);
        DatasourceResponseModel datasourceResponseModel = new DatasourceResponseModel();
        datasourceResponseModel.setEventProcessed(false);
        datasourceResponseModel.setEventId(UUID.randomUUID().toString());
        try {
            ResponseEntity<DatasourceResponseModel> responseEntity = this.restTemplate.postForEntity(url,
                    httpEntity, DatasourceResponseModel.class, uriVariables);
            if(responseEntity.getStatusCode().is2xxSuccessful()) {
                return getDatasourceResponseModel(datasourceResponseModel, responseEntity);
            }else {
                log.error("Trigger ISD register event failed : {}", responseEntity.toString());
            }
        }catch (Exception e){
            log.error("Exception in ISD register event triggering : {}", e);
        }
        return datasourceResponseModel;
    }

    private DatasourceResponseModel getDatasourceResponseModel(DatasourceResponseModel datasourceResponseModel, ResponseEntity<DatasourceResponseModel> responseEntity) {
        try {
            DatasourceResponseModel responseModel = responseEntity.getBody();
            if(responseModel != null && responseModel.getEventProcessed()){
                log.info("Event trigger ISD register success, event ID : {}", responseModel.getEventId());
                return responseModel;
            }
        } catch (Exception e) {
            log.error("Exception in triggering ISD register event {}", e);
        }
        return datasourceResponseModel;
    }

    @Override
    public DatasourceResponseModel setup(DatasourceRequestModel datasourceRequestModel, CDType cdType) {

        if (cdType.equals(CDType.isdSpinnaker))
            return triggerWebhook(datasourceRequestModel, spinnakerAutomationWebhookURL);
        else if(cdType.equals(CDType.isdArgo))
            return triggerWebhook(datasourceRequestModel, argoAutomationWebhookURL);

        return null;
    }
}

