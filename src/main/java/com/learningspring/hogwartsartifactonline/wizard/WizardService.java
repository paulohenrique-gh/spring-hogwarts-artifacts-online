package com.learningspring.hogwartsartifactonline.wizard;

import com.learningspring.hogwartsartifactonline.artifact.Artifact;
import com.learningspring.hogwartsartifactonline.artifact.ArtifactRepository;
import com.learningspring.hogwartsartifactonline.system.exception.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WizardService {

    private final WizardRepository wizardRepository;

    private final ArtifactRepository artifactRepository;

    public WizardService(WizardRepository wizardRepository, ArtifactRepository artifactRepository) {
        this.wizardRepository = wizardRepository;
        this.artifactRepository = artifactRepository;
    }

    public Wizard findById(Integer id) {
        return this.wizardRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("wizard", id));
    }

    public List<Wizard> findAll() {
        return this.wizardRepository.findAll();
    }

    public Wizard save(Wizard newWizard) {
        return this.wizardRepository.save(newWizard);
    }

    public Wizard update(Integer wizardId, Wizard update) {
        return this.wizardRepository.findById(wizardId)
                .map(oldWizard -> {
                    oldWizard.setName(update.getName());
                    return this.wizardRepository.save(oldWizard);
                })
                .orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));
    }

    public void delete(Integer wizardId) {
        Wizard wizard = this.wizardRepository.findById(wizardId)
                .orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));
        wizard.removeAllArtifacts();
        this.wizardRepository.delete(wizard);
    }

    public void assignArtifact(Integer wizardId, String artifactId) {
        Artifact artifactToBeAssigned = this.artifactRepository.findById(artifactId).orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));
        Wizard foundWizard = this.wizardRepository.findById(wizardId).orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));

        if (artifactToBeAssigned.getOwner() != null) {
            artifactToBeAssigned.getOwner().removeArtifact(artifactToBeAssigned);
        }

        foundWizard.addArtifact(artifactToBeAssigned);
        this.wizardRepository.save(foundWizard);
    }
}
