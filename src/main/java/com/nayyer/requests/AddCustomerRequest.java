package com.nayyer.requests;

public record AddCustomerRequest(String name, String email, Integer age) {
    @Override
    public String toString() {
        String st = "";
        if (name != null) {
            st += "name: " + name + ", ";
        }
        if (email != null) {
            st += "email: " + email + ", ";
        }
        if (age != null) {
            st += "age: " + age + ", ";
        }
        return st;
    }
}
