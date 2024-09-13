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
}
