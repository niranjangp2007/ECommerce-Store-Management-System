package com.ecommerce.model;

import java.util.Objects;

/**
 * Abstract base class representing a Person in the system.
 * Demonstrates:
 * 1. Abstraction: Defines contract for persons with abstract methods getRole() and getSummary().
 * 2. Encapsulation: Private fields with validated accessors and mutators.
 */
public abstract class Person {

    private int id;
    private String name;
    private String city;

    /**
     * Default constructor.
     */
    protected Person() {
    }

    /**
     * Parameterized constructor.
     *
     * @param id   the identifier
     * @param name person's full name
     * @param city person's city
     */
    protected Person(int id, String name, String city) {
        setId(id);
        setName(name);
        setCity(city);
    }

    // --- Abstract Methods (Demonstrating Abstraction) ---

    /**
     * Abstract method defining the role of this person in the e-commerce domain.
     *
     * @return role name (e.g., "Customer")
     */
    public abstract String getRole();

    /**
     * Abstract method providing a human-readable summary of this person's record.
     *
     * @return summary string
     */
    public abstract String getSummary();

    // --- Encapsulated Getters & Setters with Validation ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative: " + id);
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty.");
        }
        this.name = name.trim();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be null or empty.");
        }
        this.city = city.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person person)) return false;
        return id == person.id &&
                Objects.equals(name, person.name) &&
                Objects.equals(city, person.city);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, city);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", role='" + getRole() + '\'' +
                '}';
    }
}
