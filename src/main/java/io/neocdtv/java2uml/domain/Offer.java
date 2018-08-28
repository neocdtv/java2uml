/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.java2uml.domain;

import io.neocdtv.java2uml.domain.customer.ICustomer;

import java.util.Date;
import java.util.Set;

/**
 * @author xix
 */
public class Offer {
	private Date date;
	private Set<ICustomer> keepers;
	private ICustomer owner;
	private Car car;
	private Set<Price> prices;
	private ICustomer contactPerson;
	private ICustomer conditionProvider;
}
