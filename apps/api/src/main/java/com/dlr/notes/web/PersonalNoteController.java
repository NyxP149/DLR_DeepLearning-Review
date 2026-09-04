package com.dlr.notes.web;

import com.dlr.notes.application.PersonalNoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PersonalNoteController {

    private final PersonalNoteService personalNoteService;

    public PersonalNoteController(PersonalNoteService personalNoteService) {
        this.personalNoteService = personalNoteService;
    }

    @GetMapping("/notes")
    public List<PersonalNoteService.Note> notes() {
        return personalNoteService.notes();
    }

    @GetMapping("/labs/{labCode}/note")
    public PersonalNoteService.Note note(@PathVariable String labCode) {
        return personalNoteService.note(labCode);
    }

    @PutMapping("/labs/{labCode}/note")
    public PersonalNoteService.Note save(
            @PathVariable String labCode,
            @Valid @RequestBody SaveNoteRequest request
    ) {
        return personalNoteService.save(labCode, request.content());
    }

    @GetMapping("/labs/{labCode}/reflection-analyses")
    public List<PersonalNoteService.ReflectionAnalysis> analyses(@PathVariable String labCode) {
        return personalNoteService.analyses(labCode);
    }

    @PutMapping("/labs/{labCode}/reflection-analyses/{questionId}")
    public PersonalNoteService.ReflectionAnalysis saveAnalysis(
            @PathVariable String labCode,
            @PathVariable String questionId,
            @Valid @RequestBody SaveAnalysisRequest request
    ) {
        return personalNoteService.saveAnalysis(labCode, questionId, request.content());
    }

    @DeleteMapping("/labs/{labCode}/reflection-analyses/{questionId}")
    public void deleteAnalysis(@PathVariable String labCode, @PathVariable String questionId) {
        personalNoteService.deleteAnalysis(labCode, questionId);
    }

    public record SaveNoteRequest(@NotNull @Size(max = 20_000) String content) {
    }

    public record SaveAnalysisRequest(@NotNull @Size(min = 1, max = 20_000) String content) {
    }
}
