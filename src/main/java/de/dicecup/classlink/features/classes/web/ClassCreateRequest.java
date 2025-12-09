package de.dicecup.classlink.features.classes.web;

import jakarta.validation.constraints.NotBlank;

public record ClassCreateRequest(@NotBlank String name) {
}
