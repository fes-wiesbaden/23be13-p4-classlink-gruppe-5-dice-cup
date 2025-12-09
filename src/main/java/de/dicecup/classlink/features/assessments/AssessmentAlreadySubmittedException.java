package de.dicecup.classlink.features.assessments;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AssessmentAlreadySubmittedException extends RuntimeException {
    public AssessmentAlreadySubmittedException(String message) {
        super(message);
    }
}
