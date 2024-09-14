package com.learningspring.hogwartsartifactonline.wizard;

import com.learningspring.hogwartsartifactonline.artifact.Artifact;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class WizardServiceTest {

    @Mock
    WizardRepository wizardRepository;

    @InjectMocks
    WizardService wizardService;

    List<Wizard> wizards = new ArrayList<>();

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

        Artifact a3 = new Artifact();
        a3.setId("1250808601744904193");
        a3.setName("Elder Wand");
        a3.setDescription("The Elder Wand, known throughout history as the Deathstick or the Wand of Destiny, is an extremely powerful wand made of elder wood with a core of Thestral tail hair.");
        a3.setImageUrl("ImageUrl");

        Artifact a4 = new Artifact();
        a4.setId("1250808601744904194");
        a4.setName("The Marauder's Map");
        a4.setDescription("A magical map of Hogwarts created by Remus Lupin, Peter Pettigrew, Sirius Black, and James Potter while they were students at Hogwarts.");
        a4.setImageUrl("ImageUrl");

        Wizard w1 = new Wizard();
        w1.setId(1);
        w1.setName("Harry");
        w1.setArtifacts(List.of(a1, a2));
        this.wizards.add(w1);

        Wizard w2 = new Wizard();
        w2.setId(2);
        w2.setName("Potter");
        w2.setArtifacts(List.of(a3, a4));
        this.wizards.add(w2);
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void testFindByIdSuccess() {
        // Given
        Wizard w = this.wizards.get(0);

        given(this.wizardRepository.findById(1)).willReturn(Optional.of(w));

        // When
        Wizard returnedWizard = this.wizardService.findById(1);

        // Then
        assertThat(returnedWizard.getId()).isEqualTo(1);
        assertThat(returnedWizard.getName()).isEqualTo("Harry");
        assertThat(returnedWizard.getArtifacts()).hasSize(2);
        verify(this.wizardRepository, times(1)).findById(1);
    }

    @Test
    void testFindByIdNotFound() {
        // Given
        given(this.wizardRepository.findById(1)).willReturn(Optional.empty());

        // When
        Throwable thrown = catchThrowable(() -> this.wizardService.findById(1));

        // Then
        assertThat(thrown)
                .isInstanceOf(WizardNotFoundException.class)
                .hasMessage("Could not find wizard with Id 1 :(");
        verify(this.wizardRepository, times(1)).findById(1);
    }

    @Test
    void testFindAllSuccess() {
        // Given
        given(this.wizardRepository.findAll()).willReturn(this.wizards);

        // When
        List<Wizard> actualWizards = this.wizardService.findAll();

        // Then
        assertThat(actualWizards.size()).isEqualTo(this.wizards.size());
        verify(this.wizardRepository, times(1)).findAll();
    }

}
