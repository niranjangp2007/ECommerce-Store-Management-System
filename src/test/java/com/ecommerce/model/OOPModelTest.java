package com.ecommerce.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OOP Architecture & Domain Models Verification")
public class OOPModelTest {

    @Test
    @DisplayName("1. Abstraction: Person is an abstract class with abstract methods")
    void testPersonIsAbstract() {
        assertTrue(Modifier.isAbstract(Person.class.getModifiers()), 
                "Person must be declared as an abstract class.");
        
        assertDoesNotThrow(() -> {
            assertTrue(Modifier.isAbstract(Person.class.getMethod("getRole").getModifiers()),
                    "getRole() must be an abstract method in Person.");
            assertTrue(Modifier.isAbstract(Person.class.getMethod("getSummary").getModifiers()),
                    "getSummary() must be an abstract method in Person.");
        });
    }

    @Test
    @DisplayName("2. Inheritance: Customer extends Person")
    void testCustomerInheritance() {
        Customer customer = new Customer(1, "Anita", "Chennai", LocalDate.of(2025, 11, 10));
        
        // Assert inheritance relationship
        assertTrue(customer instanceof Person, "Customer must inherit from Person.");
        assertEquals(1, customer.getId());
        assertEquals(1, customer.getCustomerId());
        assertEquals("Anita", customer.getName());
        assertEquals("Chennai", customer.getCity());
        assertEquals(LocalDate.of(2025, 11, 10), customer.getJoinDate());
    }

    @Test
    @DisplayName("3. Runtime Polymorphism: Dynamic method dispatch via Person reference")
    void testRuntimePolymorphism() {
        Person polymorphicCustomer = new Customer(2, "Bharath", "Bangalore", LocalDate.of(2026, 1, 15));
        
        // Overridden methods called polymorphically via base reference
        assertEquals("Customer", polymorphicCustomer.getRole(), 
                "Polymorphic call to getRole() should return Customer.");
        assertTrue(polymorphicCustomer.getSummary().contains("Bharath"), 
                "Polymorphic summary should contain customer name.");
        assertTrue(polymorphicCustomer.getSummary().contains("Bangalore"), 
                "Polymorphic summary should contain customer city.");
    }

    @Test
    @DisplayName("4. Encapsulation: Strict validation on Customer fields")
    void testCustomerEncapsulationValidation() {
        Customer customer = new Customer();
        customer.setId(10);
        customer.setName("Valid Name");
        customer.setCity("Valid City");
        customer.setJoinDate(LocalDate.now());

        // Negative ID rejection
        assertThrows(IllegalArgumentException.class, () -> customer.setId(-1));
        
        // Blank name rejection
        assertThrows(IllegalArgumentException.class, () -> customer.setName("   "));
        assertThrows(IllegalArgumentException.class, () -> customer.setName(null));
        
        // Blank city rejection
        assertThrows(IllegalArgumentException.class, () -> customer.setCity(""));
        
        // Null joinDate rejection
        assertThrows(IllegalArgumentException.class, () -> customer.setJoinDate(null));
    }

    @Test
    @DisplayName("5. Encapsulation: Product domain logic & validation")
    void testProductEncapsulationAndDomainLogic() {
        Product p = new Product(1, "Wireless Mouse", "Electronics", 599.00, 50);
        
        assertEquals(1, p.getProductId());
        assertEquals("Wireless Mouse", p.getName());
        assertEquals("Electronics", p.getCategory());
        assertEquals(new BigDecimal("599.00"), p.getPrice());
        assertEquals(50, p.getStock());
        assertFalse(p.isOutOfStock());
        assertTrue(p.hasSufficientStock(10));
        assertFalse(p.hasSufficientStock(60));

        // Inventory deduction
        p.reduceStock(5);
        assertEquals(45, p.getStock());

        // Inventory restoration
        p.restoreStock(5);
        assertEquals(50, p.getStock());

        // Validation rejection tests
        assertThrows(IllegalArgumentException.class, () -> p.setPrice(new BigDecimal("-10.00")));
        assertThrows(IllegalArgumentException.class, () -> p.setStock(-5));
        assertThrows(IllegalStateException.class, () -> p.reduceStock(100)); // Exceeds available stock
    }

    @Test
    @DisplayName("6. Composition & Aggregation: Order contains OrderItems and computes total")
    void testOrderCompositionAndTotalCalculation() {
        Order order = new Order(1001, 1, LocalDate.of(2026, 9, 1), "Delivered");
        
        // Add item 1: 2 Wireless Mouse @ 599.00 = 1198.00
        OrderItem item1 = new OrderItem(1, 1001, 1, 2, "Wireless Mouse", new BigDecimal("599.00"));
        assertEquals(new BigDecimal("1198.00"), item1.getLineTotal());

        // Add item 2: 5 Notebook @ 120.00 = 600.00
        OrderItem item2 = new OrderItem(2, 1001, 3, 5, "Notebook", new BigDecimal("120.00"));
        assertEquals(new BigDecimal("600.00"), item2.getLineTotal());

        order.addItem(item1);
        order.addItem(item2);

        assertEquals(2, order.getItems().size());
        assertEquals(new BigDecimal("1798.00"), order.calculateTotal(),
                "Order total must equal sum of line totals (1198.00 + 600.00 = 1798.00).");
        assertTrue(order.isDelivered());
        assertFalse(order.isCancelled());
    }

    @Test
    @DisplayName("7. Order status validation")
    void testOrderStatusValidation() {
        Order order = new Order();
        order.setStatus("Delivered");
        order.setStatus("Shipped");
        order.setStatus("Pending");
        order.setStatus("Cancelled");

        // Invalid status rejection
        assertThrows(IllegalArgumentException.class, () -> order.setStatus("UnknownStatus"));
        assertThrows(IllegalArgumentException.class, () -> order.setStatus(""));
    }
}
