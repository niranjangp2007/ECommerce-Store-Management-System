package com.ecommerce.dao;

import com.ecommerce.model.Customer;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for Customer operations.
 * Demonstrates Abstraction by decoupling business logic from SQL persistence.
 */
public interface CustomerDAO {

    List<Customer> getAllCustomers() throws SQLException;

    Customer getCustomerById(int customerId) throws SQLException;

    List<Customer> searchCustomers(String query) throws SQLException;

    boolean addCustomer(Customer customer) throws SQLException;

    boolean updateCustomer(Customer customer) throws SQLException;

    boolean deleteCustomer(int customerId) throws SQLException;

    int getNextCustomerId() throws SQLException;

    int getCustomerCount() throws SQLException;
}
