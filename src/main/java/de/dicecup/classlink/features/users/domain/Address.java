package de.dicecup.classlink.features.users.domain;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Embeddable
public class Address {
    @Size(max = 100) private String street;
    @Size(max = 20) private String houseNumber;
    @Size(max = 100) private String addressLine2;
    @Size(max = 10) @Pattern(regexp = "^[0-9A-Za-z -]{3,10}$")
    private String postalCode;
    @Size(max = 100) private String city;
    @Size(max = 100) private String state;
    @Size(max = 3) private String countryCode = "DE";
}
