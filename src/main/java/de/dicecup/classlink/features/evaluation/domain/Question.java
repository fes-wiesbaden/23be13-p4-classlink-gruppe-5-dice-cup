package de.dicecup.classlink.features.evaluation.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Question {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String text;

    public Question() { }

    public Question(String text) {
        this.text = text;
    }

    public UUID getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
