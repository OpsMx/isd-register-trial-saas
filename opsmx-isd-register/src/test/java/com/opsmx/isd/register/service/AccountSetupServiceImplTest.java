package com.opsmx.isd.register.service;

import com.opsmx.isd.register.dto.DatasourceRequestModel;
import com.opsmx.isd.register.dto.DatasourceResponseModel;
import com.opsmx.isd.register.entities.User;
import com.opsmx.isd.register.enums.CDType;
import com.opsmx.isd.register.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class AccountSetupServiceImplTest {

    @Spy
    @InjectMocks
    private AccountSetupServiceImpl accountSetupService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    private DatasourceRequestModel datasourceRequestModel = new DatasourceRequestModel();
    private DatasourceResponseModel responseModel = new DatasourceResponseModel();
    private String eventId = UUID.randomUUID().toString();

    @BeforeEach
    public void beforeTest(){

        datasourceRequestModel.setBusinessEmail("abc@example.com");
        datasourceRequestModel.setCompanyName("OpsMx");
        datasourceRequestModel.setContactNumber("1234567890");
        datasourceRequestModel.setFirstName("Pranav");
        datasourceRequestModel.setLastName("Bhaskaran");

        responseModel.setEventProcessed(true);
        responseModel.setEventId(eventId);

        ReflectionTestUtils.setField(accountSetupService, "spinnakerAutomationWebhookURL", "http://localhost:8084/webhook/start/spinnaker");
        ReflectionTestUtils.setField(accountSetupService, "argoAutomationWebhookURL", "http://localhost:8084/webhook/start/argo");
    }

    @Test
    @DisplayName("Should store the user details in DB for CD type isdArgo")
    void test1(){

        Mockito.when(userRepository.findByBusinessEmailAndCdType(datasourceRequestModel.getBusinessEmail(), CDType.isdArgo)).thenReturn(Optional.empty());
        accountSetupService.store(datasourceRequestModel, CDType.isdArgo);
        Mockito.verify(accountSetupService, Mockito.times(1)).store(datasourceRequestModel, CDType.isdArgo);
    }

    @Test
    @DisplayName("Should store the user details in DB for CD type isdSpinnaker")
    void test2(){

        Mockito.when(userRepository.findByBusinessEmailAndCdType(datasourceRequestModel.getBusinessEmail(), CDType.isdSpinnaker)).thenReturn(Optional.empty());
        accountSetupService.store(datasourceRequestModel, CDType.isdSpinnaker);
        Mockito.verify(accountSetupService, Mockito.times(1)).store(datasourceRequestModel, CDType.isdSpinnaker);
    }

    @Test
    @DisplayName("Should not fail even if the given user details is already present in the DB for cd type isdArgo")
    void test3(){

        List<User> users = new ArrayList<>();
        User user = new User();
        user.setCdType(CDType.isdArgo);
        user.setBusinessEmail("abc@example.com");
        user.setCompanyName("OpsMx");
        user.setContactNumber("1234567890");
        user.setFirstName("Pranav");
        user.setLastName("Bhaskaran");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        users.add(user);

        Mockito.when(userRepository.findByBusinessEmailAndCdType(datasourceRequestModel.getBusinessEmail(), CDType.isdArgo)).thenReturn(Optional.of(users));
        accountSetupService.store(datasourceRequestModel, CDType.isdArgo);
        Mockito.verify(accountSetupService, Mockito.times(1)).store(datasourceRequestModel, CDType.isdArgo);
    }

    @Test
    @DisplayName("Should not fail even if the given user details is already present in the DB for cd type isdSpinnaker")
    void test4(){

        List<User> users = new ArrayList<>();
        User user = new User();
        user.setCdType(CDType.isdSpinnaker);
        user.setBusinessEmail("abc@example.com");
        user.setCompanyName("OpsMx");
        user.setContactNumber("1234567890");
        user.setFirstName("Pranav");
        user.setLastName("Bhaskaran");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        users.add(user);

        Mockito.when(userRepository.findByBusinessEmailAndCdType(datasourceRequestModel.getBusinessEmail(), CDType.isdSpinnaker)).thenReturn(Optional.of(users));
        accountSetupService.store(datasourceRequestModel, CDType.isdSpinnaker);
        Mockito.verify(accountSetupService, Mockito.times(1)).store(datasourceRequestModel, CDType.isdSpinnaker);
    }

    @Test
    @DisplayName("Should trigger the argo webhook")
    void test5(){

        Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(DatasourceResponseModel.class), ArgumentMatchers.anyMap()))
                .thenReturn(ResponseEntity.ok(responseModel));

        DatasourceResponseModel datasourceResponseModel = accountSetupService.setup(datasourceRequestModel, CDType.isdArgo);

        assertEquals(responseModel.getEventId(), datasourceResponseModel.getEventId());
        assertEquals(responseModel.getEventProcessed(), datasourceResponseModel.getEventProcessed());
    }

    @Test
    @DisplayName("Should trigger the spinnaker webhook")
    void test6(){

        Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpEntity.class), ArgumentMatchers.eq(DatasourceResponseModel.class), ArgumentMatchers.anyMap()))
                .thenReturn(ResponseEntity.ok(responseModel));

        DatasourceResponseModel datasourceResponseModel = accountSetupService.setup(datasourceRequestModel, CDType.isdSpinnaker);

        assertEquals(responseModel.getEventId(), datasourceResponseModel.getEventId());
        assertEquals(responseModel.getEventProcessed(), datasourceResponseModel.getEventProcessed());
    }
}
