package com.learningspring.hogwartsartifactonline.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests for Artifact API endpoints")
@Tag("integration")
@ActiveProfiles(value = "dev")
public class ArtifactControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    String token;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() throws Exception {
        ResultActions resultActions = this.mockMvc
                .perform(post(this.baseUrl + "/users/login")
                                 .with(httpBasic("john", "123456")));
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        this.token = "Bearer " + json.getJSONObject("data").getString("token"); // Don't forget to add "Bearer " as a prefix
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testFindAllArtifactsSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/artifacts").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(6)));
    }

    @Test
    @DisplayName("Check findArtifactId (GET)")
    void testFindArtifactByIdSuccess() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/artifacts/1250808601744904191").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.id").value("1250808601744904191"))
                .andExpect(jsonPath("$.data.name").value("Deluminator"));
    }

    @Test
    @DisplayName("Check findArtifactId with non-existing ID (GET)")
    void testFindArtifactByIdNotFound() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/artifacts/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find artifact with Id 1 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check addArtifact with valid input (POST)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testAddArtifactSuccess() throws Exception {
        Artifact a = new Artifact();
        a.setName("Remembrall");
        a.setDescription("A Remembrall was a magical large marble-sized glass ball that contained smoke which turned red when its owner or user had forgotten something. It turned clear once whatever was forgotten was remembered.");
        a.setImageUrl("ImageUrl");

        String json = this.objectMapper.writeValueAsString(a);

        this.mockMvc.perform(post(this.baseUrl + "/artifacts").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value("Remembrall"))
                .andExpect(jsonPath("$.data.description").value("A Remembrall was a magical large marble-sized glass ball that contained smoke which turned red when its owner or user had forgotten something. It turned clear once whatever was forgotten was remembered."))
                .andExpect(jsonPath("$.data.imageUrl").value("ImageUrl"));

        this.mockMvc.perform(get(this.baseUrl + "/artifacts").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(7)));
    }

    @Test
    @DisplayName("Check addArtifact with invalid input (POST)")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void testAddArtifactErrorWithInvalidInput() throws Exception {
        Artifact a = new Artifact();
        a.setName("");
        a.setDescription("");
        a.setImageUrl("");

        String json = this.objectMapper.writeValueAsString(a);

        this.mockMvc.perform(post(this.baseUrl + "/artifacts").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
                .andExpect(jsonPath("$.data.name").value("name is required"))
                .andExpect(jsonPath("$.data.description").value("description is required"))
                .andExpect(jsonPath("$.data.imageUrl").value("imageUrl is required"));

        this.mockMvc.perform(get(this.baseUrl + "/artifacts").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(6)));
    }

    @Test
    @DisplayName("Update an artifact with valid input (PUT)")
    void testUpdateArtifactSuccess() throws Exception {
        Artifact a = new Artifact();
        a.setId("1250808601744904191");
        a.setName("New Artifact Name");
        a.setDescription("New Artifact Description");
        a.setImageUrl("New Image Url");

        String json = this.objectMapper.writeValueAsString(a);

        this.mockMvc.perform(put(this.baseUrl + "/artifacts/1250808601744904191").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.id").value("1250808601744904191"))
                .andExpect(jsonPath("$.data.name").value("New Artifact Name"))
                .andExpect(jsonPath("$.data.description").value("New Artifact Description"))
                .andExpect(jsonPath("$.data.imageUrl").value("New Image Url"));
    }

    @Test
    @DisplayName("Update an artifact with non-existent Id (PUT)")
    void testUpdateArtifactErrorWithNonExistentId() throws Exception {
        Artifact a = new Artifact();
        a.setId("1");
        a.setName("New Artifact Name");
        a.setDescription("New Artifact Description");
        a.setImageUrl("New Image Url");

        String json = this.objectMapper.writeValueAsString(a);

        this.mockMvc.perform(put(this.baseUrl + "/artifacts/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find artifact with Id 1 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Update an artifact with invalid input (PUT)")
    void testUpdateArtifactWithInvalidInput() throws Exception {
        Artifact a = new Artifact();
        a.setId("1250808601744904191");
        a.setName("");
        a.setDescription("");
        a.setImageUrl("");

        String json = this.objectMapper.writeValueAsString(a);

        this.mockMvc.perform(put(this.baseUrl + "/artifacts/1250808601744904191").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are invalid, see data for details."))
                .andExpect(jsonPath("$.data.name").value("name is required"))
                .andExpect(jsonPath("$.data.description").value("description is required"))
                .andExpect(jsonPath("$.data.imageUrl").value("imageUrl is required"));
    }

    @Test
    @DisplayName("Delete an artifact with valid input (DELETE)")
    void testDeleteArtifactSuccess() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/artifacts/1250808601744904191").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"));
        this.mockMvc.perform(get(this.baseUrl + "/artifacts/1250808601744904191").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find artifact with Id 1250808601744904191 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check delete an artifact with non-existent Id (DELETE)")
    void testDeleteArtifactErrorWithNonExistentId() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/artifacts/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.token))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find artifact with Id 1 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Check find artifact by description (GET)")
    void testFindArtifactsByDescription() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("description", "Hogwarts");
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "2");
        requestParams.add("sort", "name,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/artifacts/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Search Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));

    }

    @Test
    @DisplayName("Check find artifact by name and description (GET)")
    void testFindArtifactsByNameAndDescription() throws Exception {
        // Given
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("description", "Hogwarts");
        searchCriteria.put("name", "Sword");
        String json = this.objectMapper.writeValueAsString(searchCriteria);

        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page", "0");
        requestParams.add("size", "2");
        requestParams.add("sort", "name,asc");

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/artifacts/search").contentType(MediaType.APPLICATION_JSON).content(json).params(requestParams).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Search Success"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)));

    }
}
