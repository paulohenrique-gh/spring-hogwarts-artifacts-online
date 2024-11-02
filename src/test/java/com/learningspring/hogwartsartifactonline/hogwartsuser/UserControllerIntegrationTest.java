package com.learningspring.hogwartsartifactonline.hogwartsuser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests for User API endpoints")
@Tag("integration")
public class UserControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    String adminToken;

    String userToken;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() throws Exception {
        ResultActions adminResultActions = this.mockMvc.perform(post(this.baseUrl = "/users/login").with(httpBasic("john", "123456")));
        MvcResult adminMvcResult = adminResultActions.andDo(print()).andReturn();
        String adminContentAsString = adminMvcResult.getResponse().getContentAsString();
        JSONObject adminJson = new JSONObject(adminContentAsString);
        this.adminToken = "Bearer " + adminJson.getJSONObject("data").getString("token");


        ResultActions userResultActions = this.mockMvc.perform(post(this.baseUrl = "/users/login").with(httpBasic("eric", "654321")));
        MvcResult userMvcResult = userResultActions.andDo(print()).andReturn();
        String userContentAsString = userMvcResult.getResponse().getContentAsString();
        JSONObject userJson = new JSONObject(userContentAsString);
        this.userToken = "Bearer " + userJson.getJSONObject("data").getString("token");
    }
}
