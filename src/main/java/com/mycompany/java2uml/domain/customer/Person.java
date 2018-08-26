/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.java2uml.domain.customer;

/**
 *
 * @author ofco
 */
public class Person extends Customer implements IPerson{
    private String firstName;
    public String lastName;
    private Salutation salutation;
    private Address firstAddress;
    private Address secondAddress;

    @Override
    public String getFirstName() {
        return firstName;
    }
    
}
