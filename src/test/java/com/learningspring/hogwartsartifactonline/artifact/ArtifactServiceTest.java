package com.learningspring.hogwartsartifactonline.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningspring.hogwartsartifactonline.artifact.dto.ArtifactDto;
import com.learningspring.hogwartsartifactonline.artifact.utils.IdWorker;
import com.learningspring.hogwartsartifactonline.client.ai.chat.ChatClient;
import com.learningspring.hogwartsartifactonline.client.ai.chat.dto.ChatRequest;
import com.learningspring.hogwartsartifactonline.client.ai.chat.dto.ChatResponse;
import com.learningspring.hogwartsartifactonline.client.ai.chat.dto.Choice;
import com.learningspring.hogwartsartifactonline.client.ai.chat.dto.Message;
import com.learningspring.hogwartsartifactonline.system.exception.ObjectNotFoundException;
import com.learningspring.hogwartsartifactonline.wizard.Wizard;
import com.learningspring.hogwartsartifactonline.wizard.dto.WizardDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value = "dev")
class ArtifactServiceTest {

    @Mock
    ArtifactRepository artifactRepository;

    @Mock
    IdWorker idWorker;

    @Mock
    ChatClient chatClient;

    @InjectMocks
    ArtifactService artifactService;

    List<Artifact> artifacts;

    @BeforeEach
    void setUp() {
        Artifact a1 = new Artifact();
        a1.setId("1250808601744904191");
        a1.setName("Deluminator");
        a1.setDescription("A Deluminator is a device invented by Albus Dumbledore that resembles a cigarette lighter. It is used to remove or absorb (as well as return) the light from any light source to provide cover to the user.");
        a1.setImageUrl("imageUrl");

        Artifact a2 = new Artifact();
        a2.setId("1250808601744904192");
        a2.setName("Invisibility Cloak");
        a2.setDescription("An invisibility cloak is used to make the wearer invisible.");
        a2.setImageUrl("imageUrl");

        this.artifacts = new ArrayList<>();
        this.artifacts.add(a1);
        this.artifacts.add(a2);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindByIdSuccess() {
        // Given. Arrange inputs and targets. Define the behavior of Mock object artifactRepository
        Artifact a = new Artifact();
        a.setId("1250808601744904192");
        a.setName("Invisibility Cloak");
        a.setDescription("An invisibility cloak is used to make the wearer invisible.");
        a.setImageUrl("ImageUrl");

        Wizard w = new Wizard();
        w.setId(2);
        w.setName("Harry Potter");

        a.setOwner(w);

        given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.of(a)); // Defines the behavior of the mock object

        // When. Act on the target behavior. When steps should cover the method to be tested.
        Artifact returnedArtifact = artifactService.findById("1250808601744904192");

        // Then. Assert expected outcomes.
        assertThat(returnedArtifact.getId()).isEqualTo(a.getId());
        assertThat(returnedArtifact.getName()).isEqualTo(a.getName());
        assertThat(returnedArtifact.getDescription()).isEqualTo(a.getDescription());
        assertThat(returnedArtifact.getImageUrl()).isEqualTo(a.getImageUrl());
        verify(artifactRepository, times(1)).findById("1250808601744904192");
    }

    @Test
    void testFindByIdNotFound() {
        // Given
        given(artifactRepository.findById(Mockito.any(String.class))).willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> artifactService.findById("1250808601744904192"));

        // Then
        assertThat(thrown)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find artifact with Id 1250808601744904192 :(");
        verify(artifactRepository, times(1)).findById("1250808601744904192");
    }

    @Test
    void testFindAllSuccess() {
        // Given
        given(artifactRepository.findAll()).willReturn(this.artifacts);

        // When
        List<Artifact> actualArtifacts = artifactService.findAll();

        // Then
        assertThat(actualArtifacts.size()).isEqualTo(this.artifacts.size());
        verify(artifactRepository, times(1)).findAll();
    }

    @Test
    void testSaveSuccess() {
        // Given
        Artifact newArtifact = new Artifact();
        newArtifact.setName("Artifact 3");
        newArtifact.setDescription("Description...");
        newArtifact.setImageUrl("ImageUrl...");

        given(idWorker.nextId()).willReturn(123456L);
        given(artifactRepository.save(newArtifact)).willReturn(newArtifact);

        // When
        Artifact savedArtifact = artifactService.save(newArtifact);

        // Then
        assertThat(savedArtifact.getId()).isEqualTo(123456L + "");
        assertThat(savedArtifact.getName()).isEqualTo(newArtifact.getName());
        assertThat(savedArtifact.getDescription()).isEqualTo(newArtifact.getDescription());
        assertThat(savedArtifact.getImageUrl()).isEqualTo(newArtifact.getImageUrl());
        verify(artifactRepository, times(1)).save(newArtifact);
    }

    @Test
    void testUpdateSuccess() {
        // Given
        Artifact oldArtifact = new Artifact();
        oldArtifact.setId("1250808601744904192");
        oldArtifact.setName("Invisibility Cloak");
        oldArtifact.setDescription("An invisibility cloak is used to make the wearer invisible.");
        oldArtifact.setImageUrl("ImageUrl");

        Artifact update = new Artifact();
        update.setId("1250808601744904192");
        update.setName("Invisibility Cloak");
        update.setDescription("A new description");
        update.setImageUrl("ImageUrl");

        given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.of(oldArtifact));
        given(artifactRepository.save(oldArtifact)).willReturn(oldArtifact);

        // When
        Artifact updatedArtifact = artifactService.update("1250808601744904192", update);

        // Then
        assertThat(updatedArtifact.getId()).isEqualTo(update.getId());
        assertThat(updatedArtifact.getDescription()).isEqualTo(update.getDescription());
        verify(artifactRepository, times(1)).findById("1250808601744904192");
        verify(artifactRepository, times(1)).save(oldArtifact);
    }

    @Test
    void testUpdateNotFound() {
        // Given
        Artifact update = new Artifact();
        update.setName("Invisibility Cloak");
        update.setDescription("A new description");
        update.setImageUrl("ImageUrl");

        given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.empty());

        // When
        assertThrows(ObjectNotFoundException.class, () -> {
            artifactService.update("1250808601744904192", update);
        });

        // Then
        verify(artifactRepository, times(1)).findById("1250808601744904192");
    }

    @Test
    void testDeleteSuccess() {
        // Given
        Artifact artifact = new Artifact();
        artifact.setId("1250808601744904192");
        artifact.setName("Invisibility Cloak");
        artifact.setDescription("A new description");
        artifact.setImageUrl("ImageUrl");

        given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.of(artifact));
        doNothing().when(artifactRepository).deleteById("1250808601744904192");

        // When
        artifactService.delete(artifact.getId());

        // Then
        verify(artifactRepository, times(1)).deleteById(artifact.getId());
    }

    @Test
    void testDeleteNotFound() {
        // Given
        given(this.artifactRepository.findById("1250808601744904192")).willReturn(Optional.empty());

        // When
        assertThrows(ObjectNotFoundException.class, () -> {
            this.artifactService.delete("1250808601744904192");
        });

        // Then
        verify(this.artifactRepository, times(1)).findById("1250808601744904192");
    }

    @Test
    void testSummarizeSuccess() throws JsonProcessingException {
        // Given
        WizardDto wizardDto = new WizardDto(1, "Albus Dumbledore", 2);
        List<ArtifactDto> artifactDtos = List.of(
                new ArtifactDto("1250808601744904191", "Deluminator", "A Deluminator is a device invented by Albus Dumbledore that resembles a cigarette lighter. It is used to remove or absorb (as well as return) the light from any light source to provide cover to the user.", "ImageUrl", wizardDto),
                new ArtifactDto("1250808601744904193", "Elder Wand", "The Elder Wand, known throughout history as the Deathstick or the Wand of Destiny, is an extremely powerful wand made of elder wood with a core of Thestral tail hair.", "ImageUrl", wizardDto)
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonArray = objectMapper.writeValueAsString(artifactDtos);

        List<Message> messages = List.of(
                new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
                new Message("user", jsonArray)
        );

        ChatRequest chatRequest = new ChatRequest("llama3-8b-8192", messages);

        ChatResponse chatResponse = new ChatResponse(List.of(
                new Choice(0, new Message("assistant", "A summary of two artifacts owned by Albus Dumbledore"))
        ));

        given(this.chatClient.generate(chatRequest)).willReturn(chatResponse);

        // When
        String summary = this.artifactService.summarize(artifactDtos);

        // Then
        assertThat(summary).isEqualTo("A summary of two artifacts owned by Albus Dumbledore");
        verify(this.chatClient, times(1)).generate(chatRequest);
    }
}