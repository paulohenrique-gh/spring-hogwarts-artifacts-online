package com.learningspring.hogwartsartifactonline.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learningspring.hogwartsartifactonline.artifact.converter.ArtifactDtoToArtifactConverter;
import com.learningspring.hogwartsartifactonline.artifact.converter.ArtifactToArtifactDtoConverter;
import com.learningspring.hogwartsartifactonline.artifact.dto.ArtifactDto;
import com.learningspring.hogwartsartifactonline.client.imagestorage.ImageStorageClient;
import com.learningspring.hogwartsartifactonline.system.Result;
import com.learningspring.hogwartsartifactonline.system.StatusCode;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}/artifacts")
public class ArtifactController {

    private final ArtifactService artifactService;

    private final ArtifactToArtifactDtoConverter artifactToArtifactDtoConverter;

    private final ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter;

    private final MeterRegistry meterRegistry;

    private final ImageStorageClient imageStorageClient;

    public ArtifactController(ArtifactService artifactService,
            ArtifactToArtifactDtoConverter artifactToArtifactDtoConverter,
            ArtifactDtoToArtifactConverter artifactDtoToArtifactConverter,
            MeterRegistry meterRegistry,
            ImageStorageClient imageStorageClient) {
        this.artifactService = artifactService;
        this.artifactToArtifactDtoConverter = artifactToArtifactDtoConverter;
        this.artifactDtoToArtifactConverter = artifactDtoToArtifactConverter;
        this.meterRegistry = meterRegistry;
        this.imageStorageClient = imageStorageClient;
    }

    @GetMapping("/{artifactId}")
    public Result findArtifactById(@PathVariable String artifactId) {
        Artifact foundArtifact = this.artifactService.findById(artifactId);
        this.meterRegistry.counter("artifact.id." + artifactId).increment();
        ArtifactDto artifactDto = this.artifactToArtifactDtoConverter.convert(foundArtifact);
        return new Result(true, StatusCode.SUCCESS, "Find One Success", artifactDto);
    }

    @GetMapping
    public Result findAllArtifacts(Pageable pageable) {
        Page<Artifact> artifactPage = this.artifactService.findAll(pageable);

        Page<ArtifactDto> artifactDtoPage = artifactPage
                .map(this.artifactToArtifactDtoConverter::convert);
        return new Result(true, StatusCode.SUCCESS, "Find All Success", artifactDtoPage);
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

    @GetMapping("/summary")
    public Result summarizeArtifacts() throws JsonProcessingException {
        List<ArtifactDto> artifactDtos = this.artifactService.findAll()
                .stream()
                .map(this.artifactToArtifactDtoConverter::convert)
                .toList();

        String summary = this.artifactService.summarize(artifactDtos);

        return new Result(true, StatusCode.SUCCESS, "Summarize Success", summary);
    }

    @PostMapping("/search")
    public Result findArtifactsByCriteria(@RequestBody Map<String, String> searchCriteria, Pageable pageable) {
        Page<Artifact> artifactPage = this.artifactService.findByCriteria(searchCriteria, pageable);
        Page<ArtifactDto> artifactDtoPage = artifactPage.map(this.artifactToArtifactDtoConverter::convert);

        return new Result(true, StatusCode.SUCCESS, "Search Success", artifactDtoPage);
    }

    @PostMapping("/images")
    public Result uploadImage(@RequestParam String containerName, @RequestParam MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            String originalImageName = file.getOriginalFilename();
            String imageUrl = this.imageStorageClient.uploadImage(containerName, originalImageName, inputStream, file.getSize());
            return new Result(true, StatusCode.SUCCESS, "Upload Image Success", imageUrl);
        }
    }
}
