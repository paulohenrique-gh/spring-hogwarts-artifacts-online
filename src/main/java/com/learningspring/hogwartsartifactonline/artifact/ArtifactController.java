package com.learningspring.hogwartsartifactonline.artifact;

import com.learningspring.hogwartsartifactonline.artifact.converter.ArtifactDtoToArtifactConverter;
import com.learningspring.hogwartsartifactonline.artifact.converter.ArtifactToArtifactDtoConverter;
import com.learningspring.hogwartsartifactonline.artifact.dto.ArtifactDto;
import com.learningspring.hogwartsartifactonline.system.Result;
import com.learningspring.hogwartsartifactonline.system.StatusCode;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}/artifacts")
public class ArtifactController {

    private final ArtifactService artifactService;

    private final ArtifactToArtifactDtoConverter artifactToArtifactDtoConverter;

    private final ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter;

    private final MeterRegistry meterRegistry;

    public ArtifactController(ArtifactService artifactService,
            ArtifactToArtifactDtoConverter artifactToArtifactDtoConverter,
            ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter,
            MeterRegistry meterRegistry) {
        this.artifactService = artifactService;
        this.artifactToArtifactDtoConverter = artifactToArtifactDtoConverter;
        this.artifactDtoToArtifactConverter = artifactDtoToArtifactConverter;
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/{artifactId}")
    public Result findArtifactById(@PathVariable String artifactId) {
        Artifact foundArtifact = this.artifactService.findById(artifactId);
        this.meterRegistry.counter("artifact.id." + artifactId).increment();
        ArtifactDto artifactDto = this.artifactToArtifactDtoConverter.convert(foundArtifact);
        return new Result(true, StatusCode.SUCCESS, "Find One Success", artifactDto);
    }

    @GetMapping
    public Result findAllArtifacts() {
        List<ArtifactDto> artifactDtos = this.artifactService.findAll()
                .stream()
                .map(this.artifactToArtifactDtoConverter::convert)
                .toList();
        return new Result(true, StatusCode.SUCCESS, "Find All Success", artifactDtos);
    }

    @PostMapping()
    public Result addArtifact(@RequestBody @Valid ArtifactDto artifactDto) {
        Artifact newArtifact = this.artifactDtoToArtifactConverter.convert(artifactDto);
        Artifact savedArtifact = this.artifactService.save(newArtifact);
        ArtifactDto savedArtifactDto = this.artifactToArtifactDtoConverter.convert(savedArtifact);
        return new Result(true, StatusCode.SUCCESS, "Add Success", savedArtifactDto);
    }

    @PutMapping("/{artifactId}")
    public Result updateArtifact(@PathVariable String artifactId, @RequestBody @Valid ArtifactDto artifactDto) {
        Artifact update = this.artifactDtoToArtifactConverter.convert(artifactDto);
        Artifact updatedArtifact = this.artifactService.update(artifactId, update);
        ArtifactDto updatedArtifactDto = this.artifactToArtifactDtoConverter.convert(updatedArtifact);
        return new Result(true, StatusCode.SUCCESS, "Update Success", updatedArtifactDto);
    }

    @DeleteMapping("/{artifactId}")
    public Result deleteArtifact(@PathVariable String artifactId) {
        this.artifactService.delete(artifactId);
        return new Result(true, StatusCode.SUCCESS, "Delete Success");
    }
}
