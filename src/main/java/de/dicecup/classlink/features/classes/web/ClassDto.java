package de.dicecup.classlink.features.classes.web;

import de.dicecup.classlink.features.classes.SchoolClass;

import java.util.UUID;

public record ClassDto(UUID id, String name) {
    public static ClassDto from(SchoolClass clazz) {
        return new ClassDto(clazz.getId(), clazz.getName());
    }
}
