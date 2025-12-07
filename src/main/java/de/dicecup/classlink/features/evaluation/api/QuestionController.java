package de.dicecup.classlink.features.evaluation.api;

import de.dicecup.classlink.features.evaluation.domain.Question;
import de.dicecup.classlink.features.evaluation.repository.QuestionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionRepository repository;

    public QuestionController(QuestionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Question> getAllQuestions() {
        return repository.findAll();
    }

    @PostMapping
    public Question createQuestion(@RequestBody CreateQuestionRequest request) {
        return repository.save(new Question(request.text()));
    }

    @DeleteMapping("/{id}")
    public void deleteQuestion(@PathVariable UUID id) {
        repository.deleteById(id);
    }

    public record CreateQuestionRequest(String text) {}

}
