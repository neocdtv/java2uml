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
	private final String id;
	private final String label;
	private Set<String> constants = new HashSet<>();

	public Enumeration(String name, final String label) {
		this.id = name;
		this.label = label;
	}

	public void addConstant(final String constant) {
		constants.add(constant);
	}

	public Set<String> getConstants() {
		return constants;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 67 * hash + Objects.hashCode(this.id);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Enumeration other = (Enumeration) obj;
		if (!Objects.equals(this.id, other.id)) {
			return false;
		}
		return true;
	}
}
