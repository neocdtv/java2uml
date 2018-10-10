/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.modelling.reverse.domain.customer;

import io.neocdtv.modelling.reverse.domain.customer.address.Address;

import javax.persistence.Entity;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xix
 */
@Entity
public class Organization extends Customer {
	private Set<Address> aSetAddresses;
	private List<Address> aListAddresses;
	private Address[] arrayAddresses;
	private Map<String, Address> mapAddresses;
}
