/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.java2uml.domain.customer;

import com.mycompany.java2uml.domain.customer.Customer;
import com.mycompany.java2uml.domain.customer.Address;
import java.util.Set;

/**
 *
 * @author ofco
 */
public class Organization extends Customer{
    private Set<Address> addresses;

    public Set<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<Address> addresses) {
        this.addresses = addresses;
    }
}
