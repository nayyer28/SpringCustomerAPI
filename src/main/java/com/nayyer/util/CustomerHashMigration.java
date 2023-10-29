package com.nayyer.util;

import com.nayyer.CustomerRepository;
import com.nayyer.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class CustomerHashMigration implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerHashMigration(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    private String generateHash(String name, int age, String email) {
        // Your SHA-256 hashing function implementation here
        return CustomerHashGenerator.generateHash(name, age, email);
    }

    @Override
    public void run(String... args) {
        var customers = customerRepository.findAll();
        for (Customer customer : customers) {
            String hash = generateHash(customer.getName(), customer.getAge(), customer.getEmail());
            customer.setHash(hash);
            customerRepository.save(customer);
        }
        System.out.println("Hash values updated for all rows in the Customer table.");
    }
}
