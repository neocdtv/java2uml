/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.modelling.reverse.domain.offer;

import io.neocdtv.modelling.reverse.domain.customer.ICustomer;
import io.neocdtv.modelling.reverse.domain.vehicle.Car;

import javax.persistence.Entity;
import java.util.Date;
import java.util.Set;

/**
 * @author xix
 */
@Entity
public class Offer {
	private Date date;
	private Set<ICustomer> keepers;
	private ICustomer owner;
	private Car car;
	private Set<Price> prices;
	private ICustomer contactPerson;
	private ICustomer conditionProvider;
}
