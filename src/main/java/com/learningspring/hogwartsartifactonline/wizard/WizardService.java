package com.learningspring.hogwartsartifactonline.wizard;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WizardService {

    private final WizardRepository wizardRepository;

    public WizardService(WizardRepository wizardRepository) {
        this.wizardRepository = wizardRepository;
    }

    public Wizard findById(Integer id) {
        return this.wizardRepository.findById(id).orElseThrow(() -> new WizardNotFoundException(id));
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
                    oldWizard.setArtifacts(update.getArtifacts());

                    return this.wizardRepository.save(oldWizard);
                })
                .orElseThrow(() -> new WizardNotFoundException(wizardId));
    }

    public void delete(Integer wizardId) {
        Wizard wizard = this.wizardRepository.findById(wizardId)
                .orElseThrow(() -> new WizardNotFoundException(wizardId));
        this.wizardRepository.delete(wizard);
    }
}
