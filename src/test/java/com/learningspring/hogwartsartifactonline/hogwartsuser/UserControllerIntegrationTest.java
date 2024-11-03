package com.learningspring.hogwartsartifactonline.hogwartsuser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningspring.hogwartsartifactonline.hogwartsuser.dto.UserDto;
import com.learningspring.hogwartsartifactonline.system.StatusCode;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
        ResultActions adminResultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("john", "123456")));
        MvcResult adminMvcResult = adminResultActions.andDo(print()).andReturn();
        String adminContentAsString = adminMvcResult.getResponse().getContentAsString();
        JSONObject adminJson = new JSONObject(adminContentAsString);
        this.adminToken = "Bearer " + adminJson.getJSONObject("data").getString("token");


        ResultActions userResultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("eric", "654321")));
        MvcResult userMvcResult = userResultActions.andDo(print()).andReturn();
        String userContentAsString = userMvcResult.getResponse().getContentAsString();
        JSONObject userJson = new JSONObject(userContentAsString);
        this.userToken = "Bearer " + userJson.getJSONObject("data").getString("token");
    }

    @Test
    @DisplayName("Admin role: Check get all users (GET)")
    void testFindAllUsersSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
    }

    @Test
    @DisplayName("User role: Check get all users (GET)")
    void testFindAllUsersForbidden() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.userToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission"))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    @DisplayName("Admin role: Check get user by id (GET)")
    void testFindUserByIdSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.username").value("eric"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.roles").value("user"));
    }

    @Test
    @DisplayName("Admin role: Check get user with non-existing id (GET)")
    void testFindUserWithNonExistentId() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/users/999").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with Id 999 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("User role: Check get user by id (GET)")
    void testFindUserByIdForbidden() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/users/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.userToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission"))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    @DisplayName("Admin role: Check add user with valid input (POST)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testAddUserSuccess() throws Exception {
        HogwartsUser newUser = new HogwartsUser();
        newUser.setUsername("hermione");
        newUser.setPassword("hermione123");
        newUser.setEnabled(true);
        newUser.setRoles("user");

        String json = this.objectMapper.writeValueAsString(newUser);

        this.mockMvc.perform(post(this.baseUrl + "/users").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("hermione"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.roles").value("user"));

        this.mockMvc.perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(4)));
    }

    @Test
    @DisplayName("Admin role: Check add user with invalid input (POST)")
    void testAddUserErrorWithInvalidInput() throws Exception {
        HogwartsUser newUser = new HogwartsUser();
        newUser.setUsername("");
        newUser.setPassword("");
        newUser.setEnabled(true);
        newUser.setRoles("user");

        String json = this.objectMapper.writeValueAsString(newUser);

        this.mockMvc.perform(post(this.baseUrl + "/users").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
                .andExpect(jsonPath("$.data.username").value("username is required."))
                .andExpect(jsonPath("$.data.password").value("password is required."));

        this.mockMvc.perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
    }

    @Test
    @DisplayName("User role: Check add user (POST)")
    void testAddUserForbidden() throws Exception {
        HogwartsUser newUser = new HogwartsUser();
        newUser.setUsername("hermione");
        newUser.setPassword("hermione123");
        newUser.setEnabled(true);
        newUser.setRoles("user");

        String json = this.objectMapper.writeValueAsString(newUser);

        this.mockMvc.perform(post(this.baseUrl + "/users").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.userToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission"))
                .andExpect(jsonPath("$.data").value("Access Denied"));

        this.mockMvc.perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
    }

    @Test
    @DisplayName("Admin role: Check update user with valid input (PUT)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testUpdateUserSuccess() throws Exception {
        UserDto userDto = new UserDto(null, "tom-update", true, "user");

        String json = this.objectMapper.writeValueAsString(userDto);

        this.mockMvc.perform(put(this.baseUrl + "/users/3").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.username").value("tom-update"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.roles").value("user"));
    }

    @Test
    @DisplayName("Admin role: Check update user with non-existing id (PUT)")
    void testUpdateUserWithNonExistentId() throws Exception {
        UserDto userDto = new UserDto(null, "tom-update", true, "user");

        String json = this.objectMapper.writeValueAsString(userDto);

        this.mockMvc.perform(put(this.baseUrl + "/users/999").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with Id 999 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Admin role: Check update user with invalid input (PUT)")
    void testUpdateUserErrorWithInvalidInput() throws Exception {
        UserDto userDto = new UserDto(null, "", true, "user");

        String json = this.objectMapper.writeValueAsString(userDto);

        this.mockMvc.perform(put(this.baseUrl + "/users/3").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
                .andExpect(jsonPath("$.data.username").value("username is required."));
    }

    @Test
    @DisplayName("User role: Check update user (PUT)")
    void testUpdateUserForbidden() throws Exception {
        UserDto userDto = new UserDto(null, "tom-update", true, "user");

        String json = this.objectMapper.writeValueAsString(userDto);

        this.mockMvc.perform(put(this.baseUrl + "/users/3").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.userToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission"))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }

    @Test
    @DisplayName("Admin role: Check delete user (DELETE)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testDeleteUserSuccess() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/users/3").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"));

        this.mockMvc.perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(2)));
    }

    @Test
    @DisplayName("Admin role: Check delete user with non-existing id (DELETE)")
    void testDeleteUserWithNonExistentId() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/users/999").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with Id 999 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("User role: Check delete user (DELETE)")
    void testDeleteUserForbidden() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/users/3").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.userToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission"))
                .andExpect(jsonPath("$.data").value("Access Denied"));
    }
}
