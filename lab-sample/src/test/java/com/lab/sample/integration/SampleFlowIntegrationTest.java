package com.lab.sample.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.sample.dto.CustomerRequest;
import com.lab.sample.dto.SampleCreateRequest;
import com.lab.sample.dto.SampleRejectRequest;
import com.lab.sample.dto.TestDefinitionRequest;
import com.lab.sample.dto.TestResultRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Ucdan uca senaryo: musteri + test tanimi + numune olustur, sonuc gir, tamamla.
 * Ayni sekilde: reddedilen numuneye sonuc girme denemesi reddedilir (is kurali 2).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class SampleFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private Long createCustomer(String email) throws Exception {
        String body = mockMvc.perform(post("/api/customers")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new CustomerRequest("Test Klinik " + email, email, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private Long createTestDefinition(String code) throws Exception {
        String body = mockMvc.perform(post("/api/test-definitions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new TestDefinitionRequest(code, "Glukoz", "mg/dL",
                                        new BigDecimal("70"), new BigDecimal("100")))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    void fullHappyPath_receiveEnterResultComplete() throws Exception {
        Long customerId = createCustomer("klinik-" + System.nanoTime() + "@test.com");
        Long definitionId = createTestDefinition("GLU-" + System.nanoTime());
        String barcode = "BC-" + System.nanoTime();

        String sampleBody = mockMvc.perform(post("/api/samples")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new SampleCreateRequest(barcode, customerId, java.util.Set.of(definitionId)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("RECEIVED")))
                .andReturn().getResponse().getContentAsString();

        var sampleJson = objectMapper.readTree(sampleBody);
        Long sampleId = sampleJson.get("id").asLong();
        Long sampleTestId = sampleJson.get("tests").get(0).get("id").asLong();

        mockMvc.perform(post("/api/samples/{sampleId}/tests/{testId}/result", sampleId, sampleTestId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new TestResultRequest(new BigDecimal("92.5"), "Teknisyen Ayşe"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.flag", is("NORMAL")));

        mockMvc.perform(get("/api/samples/{id}", sampleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        mockMvc.perform(post("/api/samples/{id}/complete", sampleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    void rejectedSample_cannotReceiveResult() throws Exception {
        Long customerId = createCustomer("klinik-" + System.nanoTime() + "@test.com");
        Long definitionId = createTestDefinition("HGB-" + System.nanoTime());
        String barcode = "BC-" + System.nanoTime();

        String sampleBody = mockMvc.perform(post("/api/samples")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new SampleCreateRequest(barcode, customerId, java.util.Set.of(definitionId)))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var sampleJson = objectMapper.readTree(sampleBody);
        Long sampleId = sampleJson.get("id").asLong();
        Long sampleTestId = sampleJson.get("tests").get(0).get("id").asLong();

        mockMvc.perform(post("/api/samples/{id}/reject", sampleId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SampleRejectRequest("Numune hemolizli"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")));

        mockMvc.perform(post("/api/samples/{sampleId}/tests/{testId}/result", sampleId, sampleTestId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                new TestResultRequest(new BigDecimal("1"), "Teknisyen"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", is("SAMPLE_REJECTED")));
    }

    @Test
    void duplicateBarcode_returnsConflict() throws Exception {
        Long customerId = createCustomer("klinik-" + System.nanoTime() + "@test.com");
        Long definitionId = createTestDefinition("CRE-" + System.nanoTime());
        String barcode = "BC-DUP-" + System.nanoTime();

        SampleCreateRequest request = new SampleCreateRequest(barcode, customerId, java.util.Set.of(definitionId));
        mockMvc.perform(post("/api/samples").contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/samples").contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("DUPLICATE_BARCODE")));
    }
}
