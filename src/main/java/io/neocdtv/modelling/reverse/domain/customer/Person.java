/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.modelling.reverse.domain.customer;

import io.neocdtv.modelling.reverse.domain.customer.address.Address;

import javax.persistence.Entity;

/**
 * @author xix
 */
@Entity
public class Person extends Customer implements IPerson {
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
