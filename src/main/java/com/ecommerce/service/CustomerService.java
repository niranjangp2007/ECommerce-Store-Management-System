package com.ecommerce.service;

import com.ecommerce.dao.CustomerDAO;
import com.ecommerce.dao.CustomerDAOImpl;
import com.ecommerce.model.Customer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service class handling Customer business logic, validation, and exception translation.
 */
public class CustomerService {

    private final CustomerDAO customerDAO;

    public CustomerService() {
        this(new CustomerDAOImpl());
    }

    public CustomerService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.getAllCustomers();
    }

    public Customer getCustomerById(int customerId) throws SQLException {
        return customerDAO.getCustomerById(customerId);
    }

    public List<Customer> searchCustomers(String query) throws SQLException {
        return customerDAO.searchCustomers(query);
    }

    public int getNextCustomerId() throws SQLException {
        return customerDAO.getNextCustomerId();
    }

    public int getCustomerCount() throws SQLException {
        return customerDAO.getCustomerCount();
    }

    public void addCustomer(int customerId, String name, String city, LocalDate joinDate) 
            throws DatabaseOperationException, SQLException {
        validateCustomerData(name, city, joinDate);

        if (customerDAO.getCustomerById(customerId) != null) {
            throw new DatabaseOperationException("Customer ID " + customerId + " already exists. Please choose a unique ID.");
        }

        Customer customer = new Customer(customerId, name.trim(), city.trim(), joinDate);
        boolean success = customerDAO.addCustomer(customer);
        if (!success) {
            throw new DatabaseOperationException("Failed to add customer to database.");
        }
    }

    public void updateCustomer(int customerId, String name, String city, LocalDate joinDate) 
            throws DatabaseOperationException, SQLException {
        validateCustomerData(name, city, joinDate);

        Customer existing = customerDAO.getCustomerById(customerId);
        if (existing == null) {
            throw new DatabaseOperationException("Customer ID " + customerId + " not found.");
        }

        Customer customer = new Customer(customerId, name.trim(), city.trim(), joinDate);
        boolean success = customerDAO.updateCustomer(customer);
        if (!success) {
            throw new DatabaseOperationException("Failed to update customer details.");
        }
    }

    public void deleteCustomer(int customerId) throws DatabaseOperationException, SQLException {
        Customer existing = customerDAO.getCustomerById(customerId);
        if (existing == null) {
            throw new DatabaseOperationException("Customer ID " + customerId + " not found.");
        }

        try {
            boolean success = customerDAO.deleteCustomer(customerId);
            if (!success) {
                throw new DatabaseOperationException("Failed to delete customer.");
            }
        } catch (SQLException e) {
            // MySQL error code 1451: Foreign key constraint fails
            if (e.getErrorCode() == 1451 || "23000".equals(e.getSQLState())) {
                throw new DatabaseOperationException("Cannot delete customer #" + customerId + 
                        " ('" + existing.getName() + "') because existing orders are linked to this customer.\n" +
                        "Foreign-key restriction enforced: Delete customer's orders first.");
            }
            throw e;
        }
    }

    private void validateCustomerData(String name, String city, LocalDate joinDate) throws DatabaseOperationException {
        if (name == null || name.trim().isEmpty()) {
            throw new DatabaseOperationException("Customer name cannot be empty.");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new DatabaseOperationException("Customer city cannot be empty.");
        }
        if (joinDate == null) {
            throw new DatabaseOperationException("Join date must be provided.");
        }
    }
}
