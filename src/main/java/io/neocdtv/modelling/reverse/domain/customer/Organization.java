/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.modelling.reverse.domain.customer;

import java.util.Map;
import java.util.Set;

/**
 * @author xix
 */
public class Organization extends Customer {
	private Set<Address> setAddresses;
	private Set<Address> listAddresses;
	private Address[] arrayAddresses;
	private Map<String, Address> mapAddresses;
}
