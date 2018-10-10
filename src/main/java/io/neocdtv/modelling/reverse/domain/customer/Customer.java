/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.modelling.reverse.domain.customer;

import javax.persistence.Entity;

/**
 * @author xix
 */
@Entity
public class Customer implements ICustomer {
	protected int attribute;

	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}

	@Override
	public int compareTo(Object o) {
		return 0;
	}
}
