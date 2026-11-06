package com.ecommerce.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Customer model class extending abstract Person.
 * Demonstrates:
 * 1. Inheritance: Inherits id, name, and city attributes and behaviors from Person.
 * 2. Polymorphism: Implements and overrides abstract methods getRole() and getSummary().
 * 3. Encapsulation: Validates joinDate and maintains encapsulation of all fields.
 *
 * Mapped to database table: 'customers' (customer_id, name, city, join_date).
 */
public class Customer extends Person {

    private LocalDate joinDate;

    /**
     * Default constructor.
     */
    public Customer() {
        super();
    }

    /**
     * Parameterized constructor.
     *
     * @param customerId unique customer identifier
     * @param name       full name of the customer
     * @param city       residential city
     * @param joinDate   date of joining
     */
    public Customer(int customerId, String name, String city, LocalDate joinDate) {
        super(customerId, name, city);
        setJoinDate(joinDate);
    }

    // --- Polymorphic Overrides (Demonstrating Polymorphism) ---

    @Override
    public String getRole() {
        return "Customer";
    }

    @Override
    public String getSummary() {
        return String.format("Customer #%d: %s from %s (Member since: %s)",
                getId(), getName(), getCity(), joinDate != null ? joinDate.toString() : "N/A");
    }

    // --- Schema Mapping Delegates & Accessors ---

    public int getCustomerId() {
        return getId();
    }

    public void setCustomerId(int customerId) {
        setId(customerId);
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        if (joinDate == null) {
            throw new IllegalArgumentException("Join date cannot be null.");
        }
        this.joinDate = joinDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer customer)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(joinDate, customer.joinDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), joinDate);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", city='" + getCity() + '\'' +
                ", joinDate=" + joinDate +
                '}';
    }
}
