package com.opsmx.isd.register.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsmx.isd.register.dto.DatasourceRequestModel;
import com.opsmx.isd.register.dto.DatasourceResponseModel;
import com.opsmx.isd.register.enums.CDType;
import com.opsmx.isd.register.service.AccountSetupService;
import com.opsmx.isd.register.service.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.UUID;


@Slf4j
@WebMvcTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {AccountManagerController.class})
class AccountManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountSetupService accountSetupService;

    @MockBean
    private SendMessage sendMessage;

    private static ObjectMapper mapper = new ObjectMapper();

    private DatasourceRequestModel datasourceRequestModel = new DatasourceRequestModel();
    private DatasourceResponseModel responseModel = new DatasourceResponseModel();

    @BeforeEach
    public void beforeTest(){
        datasourceRequestModel.setBusinessEmail("abc@example.com");
        datasourceRequestModel.setCompanyName("OpsMx");
        datasourceRequestModel.setContactNumber("1234567890");
        datasourceRequestModel.setFirstName("Pranav");
        datasourceRequestModel.setLastName("Bhaskaran");

        responseModel.setEventProcessed(true);
        responseModel.setEventId(UUID.randomUUID().toString());
    }

    @Test
    @DisplayName("User registration API")
    void test1() throws Exception {

        Mockito.when(accountSetupService.setup(ArgumentMatchers.any())).thenReturn(responseModel);
        String json = mapper.writeValueAsString(datasourceRequestModel);
        log.info("request json : {}", json);
        mockMvc.perform(post("/webhookTrigger").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .content(json).characterEncoding(StandardCharsets.UTF_8.name())).andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventProcessed", Matchers.equalTo(Boolean.TRUE)));
    }

    @Test
    @DisplayName("User Registration API based on CD Type isdArgo")
    void test2() throws Exception{

        Mockito.when(accountSetupService.setup(datasourceRequestModel, CDType.isdArgo)).thenReturn(responseModel);
        String json = mapper.writeValueAsString(datasourceRequestModel);
        mockMvc.perform(post("/webhookTrigger/"+CDType.isdArgo.name()).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(json).characterEncoding(StandardCharsets.UTF_8.name())).andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventProcessed", Matchers.equalTo(Boolean.TRUE)))
                .andExpect(jsonPath("$.eventId", Matchers.notNullValue()));
    }

    @Test
    @DisplayName("User Registration API based on CD Type isdSpinnaker")
    void test3() throws Exception{

        Mockito.when(accountSetupService.setup(datasourceRequestModel, CDType.isdSpinnaker)).thenReturn(responseModel);
        String json = mapper.writeValueAsString(datasourceRequestModel);
        mockMvc.perform(post("/webhookTrigger/"+CDType.isdSpinnaker.name()).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(json).characterEncoding(StandardCharsets.UTF_8.name())).andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventProcessed", Matchers.equalTo(Boolean.TRUE)))
                .andExpect(jsonPath("$.eventId", Matchers.notNullValue()));
    }

    @Test
    @DisplayName("Trigger the webhook based on the cd type isdArgo , FAILURE scenario")
    void test4() throws Exception{

        Mockito.doThrow(RuntimeException.class).doNothing().when(accountSetupService).store(datasourceRequestModel, CDType.isdArgo);
        String json = mapper.writeValueAsString(datasourceRequestModel);
        mockMvc.perform(post("/webhookTrigger/"+CDType.isdArgo.name()).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(json).characterEncoding(StandardCharsets.UTF_8.name())).andExpect(status().isConflict())
                .andExpect(jsonPath("$.eventProcessed", Matchers.equalTo(Boolean.FALSE)))
                .andExpect(jsonPath("$.eventId", Matchers.notNullValue()));
    }

    @Test
    @DisplayName("Trigger the webhook based on the cd type isdSpinnaker , FAILURE scenario")
    void test5() throws Exception{

        Mockito.doThrow(RuntimeException.class).doNothing().when(accountSetupService).store(datasourceRequestModel, CDType.isdSpinnaker);
        String json = mapper.writeValueAsString(datasourceRequestModel);
        mockMvc.perform(post("/webhookTrigger/"+CDType.isdSpinnaker.name()).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(json).characterEncoding(StandardCharsets.UTF_8.name())).andExpect(status().isConflict())
                .andExpect(jsonPath("$.eventProcessed", Matchers.equalTo(Boolean.FALSE)))
                .andExpect(jsonPath("$.eventId", Matchers.notNullValue()));
    }

    @Test
    @DisplayName("Should throw bad request when wrong CD type is passed")
    void test6() throws Exception{

        Mockito.when(accountSetupService.setup(datasourceRequestModel, CDType.isdArgo)).thenReturn(responseModel);
        String json = mapper.writeValueAsString(datasourceRequestModel);
        mockMvc.perform(post("/webhookTrigger/someCDType").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(json).characterEncoding(StandardCharsets.UTF_8.name())).andExpect(status().isBadRequest());
    }
}