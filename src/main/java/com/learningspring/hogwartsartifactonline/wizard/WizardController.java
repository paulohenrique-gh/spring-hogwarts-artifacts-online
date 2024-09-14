package com.learningspring.hogwartsartifactonline.wizard;

import com.learningspring.hogwartsartifactonline.system.Result;
import com.learningspring.hogwartsartifactonline.system.StatusCode;
import com.learningspring.hogwartsartifactonline.wizard.converter.WizardToWizardDtoConverter;
import com.learningspring.hogwartsartifactonline.wizard.dto.WizardDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wizards")
public class WizardController {

    private final WizardService wizardService;

    private final WizardToWizardDtoConverter wizardToWizardDtoConverter;

    public WizardController(WizardService wizardService,
            WizardToWizardDtoConverter wizardToWizardDtoConverter) {
        this.wizardService = wizardService;
        this.wizardToWizardDtoConverter = wizardToWizardDtoConverter;
    }

    @GetMapping("/{wizardId}")
    public Result findWizardById(@PathVariable Integer wizardId) {
        Wizard foundWizard = this.wizardService.findById(wizardId);
        WizardDto wizardDto = this.wizardToWizardDtoConverter.convert(foundWizard);
        return new Result(true, StatusCode.SUCCESS, "Found One Success", wizardDto);
    }

    @GetMapping
    public Result findAllWizards() {
        List<WizardDto> wizardDtos = this.wizardService.findAll()
                .stream()
                .map(this.wizardToWizardDtoConverter::convert)
                .toList();
        return new Result(true, StatusCode.SUCCESS, "Find All Success", wizardDtos);
    }
}
