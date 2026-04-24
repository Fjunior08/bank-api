package com.bank.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bank.api.dto.CreateAccountDTO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateDepositWithdrawAndGetStatement() throws Exception {

        //Criar conta
        CreateAccountDTO dto = new CreateAccountDTO();
        dto.setOwner("Franciss");

        String response = mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        Long accountId = json.get("data").get("id").asLong();

        //Depositar
        mockMvc.perform(post("/accounts/" + accountId + "/deposit")
                .param("amount", "100"))
                .andExpect(status().isOk());

        //Sacar
        mockMvc.perform(post("/accounts/" + accountId + "/withdraw")
                .param("amount", "50"))
                .andExpect(status().isOk());

        //Extrato
        mockMvc.perform(get("/accounts/" + accountId + "/statement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}