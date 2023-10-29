package com.nayyer;

import com.nayyer.requests.AddCustomerRequest;
import com.nayyer.requests.AddCustomersRequest;
import com.nayyer.util.CustomerHashGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@RestController
@RequestMapping("api/v1")
public class CustomerApi {

    private final CustomerRepository customerRepository;

    public CustomerApi(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public static void main(String args[]) {
        System.out.println("Hello World");
        SpringApplication.run(CustomerApi.class, args);
    }

    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getCustomers() {
        return ResponseEntity.ok(customerRepository.findAll());
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Integer id) {
        return customerRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/customer/{id}")
    public ResponseEntity<String> deleteCustomerById(@PathVariable Integer id) {
        if (customerRepository.findById(id).isPresent()) {
            customerRepository.deleteById(id);
            return ResponseEntity.ok("Customer with id: " + id + " deleted.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer with id: " + id + " not found");
    }

    @PutMapping("/customer/{id}")
    public ResponseEntity<String> updateCustomerById(@PathVariable Integer id, @RequestBody AddCustomerRequest request) {
        if (request.email() == null && request.age() == null && request.name() == null) {
            return ResponseEntity.ok("Nothing provided to update.");
        }
        if (customerRepository.findById(id).isPresent()) {
            var customer = customerRepository.findById(id).get();
            String name = customer.getName();
            String email = customer.getEmail();
            Integer age = customer.getAge();
            if (request.name() != null) {
                name = request.name();
            }
            if (request.age() != null) {
                age = request.age();
            }
            if (request.email() != null) {
                email = request.email();
            }

            String updatedHash = CustomerHashGenerator.generateHash(name, age, email);

            List<Customer> existingCustomers = customerRepository.findByHash(updatedHash);

            if (!existingCustomers.isEmpty()) {
                if (existingCustomers.size() > 1) {
                    throw new RuntimeException("Database hashes are corrupted. Try implementing a better hash");
                }
                Customer existingCustomer = existingCustomers.get(0);
                if(existingCustomer == customer)
                {
                    return ResponseEntity.ok("Nothing changed for customer with id:" + id);
                }
                return ResponseEntity.badRequest().body("Such a customer already exists. Provide a different email address");
            }
            customer.setName(name);
            customer.setAge(age);
            customer.setEmail(email);
            customer.setHash(CustomerHashGenerator.generateHash(name, age, email));
            customerRepository.save(customer);
            return ResponseEntity.ok("Customer with id: " + id + " updated.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer with id: " + id + " not found");
    }


    @PostMapping("/customer")
    public ResponseEntity<String> addCustomer(@RequestBody AddCustomerRequest request) {
        if (request.age() == null || request.email() == null || request.name() == null) {
            return ResponseEntity.badRequest().body("Please specify all three attributes: name, age and email");
        }
        Customer customer = new Customer();
        customer.setName(request.name());
        customer.setAge(request.age());
        customer.setEmail(request.email());
        String hash = CustomerHashGenerator.generateHash(request.name(), request.age(), request.email());
        customer.setHash(hash);
        var existingCustomers = customerRepository.findByHash(hash);
        if (!existingCustomers.isEmpty()) {
            if (existingCustomers.size() > 1) {
                throw new RuntimeException("Database hashes are corrupted. Try implementing a better hash");
            }
            return ResponseEntity.ok("Customer already exists: " + existingCustomers.get(0));
        }
        return ResponseEntity.ok(customerRepository.save(customer).getId().toString());
    }

    @PostMapping("/customers")
    public ResponseEntity<String> addCustomers(@RequestBody AddCustomersRequest request) {
        List<String> responses = new ArrayList<>();
        for (var customerRequest : request.customers()) {
            var result = addCustomer(customerRequest);
            if (result.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                return ResponseEntity.badRequest().body("Please specify all three attributes: name, age and email. Incorrect data: " + customerRequest);
            }
            responses.add(result.getBody());
        }
        return ResponseEntity.ok(responses.toString());
    }
}
