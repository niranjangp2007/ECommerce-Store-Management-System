package com.ecommerce.service;

import com.ecommerce.database.DBConnection;
import com.ecommerce.model.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomerService Validation & Operations Test")
public class CustomerServiceTest {

    private final CustomerService customerService = new CustomerService();

    @Test
    @DisplayName("Validation: Rejects empty customer name and city")
    void testCustomerValidation() {
        assertThrows(DatabaseOperationException.class, () ->
                customerService.addCustomer(999, "", "Chennai", LocalDate.now()));

        assertThrows(DatabaseOperationException.class, () ->
                customerService.addCustomer(999, "Valid Name", "   ", LocalDate.now()));

        assertThrows(DatabaseOperationException.class, () ->
                customerService.addCustomer(999, "Valid Name", "Chennai", null));
    }

    @Test
    @DisplayName("Database Live: Customer retrieval when connected")
    void testCustomerLiveRetrieval() throws SQLException {
        if (!DBConnection.testConnection()) {
            System.out.println("Skipping live customer DB assertions: Database connection not active in test runner environment.");
            return;
        }

        List<Customer> list = customerService.getAllCustomers();
        assertNotNull(list);
        assertTrue(list.size() >= 5, "Sample customers dataset must contain at least 5 records.");

        Customer c1 = customerService.getCustomerById(1);
        assertNotNull(c1);
        assertEquals("Anita", c1.getName());
        assertEquals("Chennai", c1.getCity());
    }
}
