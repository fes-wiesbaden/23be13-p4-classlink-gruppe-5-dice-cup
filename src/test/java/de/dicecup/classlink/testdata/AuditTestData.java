package de.dicecup.classlink.testdata;

import net.datafaker.Faker;

import java.util.Locale;
import java.util.Random;

public class AuditTestData {
    private final Faker faker;


    public AuditTestData() {
        this.faker = new Faker(Locale.GERMAN, new Random(42L));

    }
}
