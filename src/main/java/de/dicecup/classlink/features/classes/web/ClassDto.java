package de.dicecup.classlink.features.classes.web;

import de.dicecup.classlink.features.classes.Class;

import java.util.UUID;

public record ClassDto(UUID id, String name) {
    public static ClassDto from(Class clazz) {
        return new ClassDto(clazz.getId(), clazz.getName());
    }
}
