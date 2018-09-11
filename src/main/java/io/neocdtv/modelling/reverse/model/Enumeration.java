/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.modelling.reverse.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author xix
 */
public class Enumeration extends Classifier {

	private Set<String> constants = new HashSet<>();

	public Enumeration(String id, final String label) {
		super(id, label);
	}

	public void addConstant(final String constant) {
		constants.add(constant);
	}

	public Set<String> getConstants() {
		return constants;
	}

}
