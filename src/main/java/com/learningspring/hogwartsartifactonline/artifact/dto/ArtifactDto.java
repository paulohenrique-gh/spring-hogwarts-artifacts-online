package com.learningspring.hogwartsartifactonline.artifact.dto;

import com.learningspring.hogwartsartifactonline.wizard.dto.WizardDto;

public record ArtifactDto(
        String id,
        String name,
        String description,
        String imageUrl,
        WizardDto owner
) {
}
