/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.java2uml.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class Clazz extends Classifier {
	private final String id;
	private final String label;
	private Set<Attribute> attributes = new HashSet<>();

	public Clazz(String name, final String label) {
		this.id = name;
		this.label = label;
	}

	public void addAttribute(final String name, final String type, final Visibility visibility) {
		attributes.add(new Attribute(name, type, visibility));
	}

	public Set<Attribute> getAttributes() {
		return attributes;
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
		int hash = 7;
		hash = 97 * hash + Objects.hashCode(this.id);
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
		final Clazz other = (Clazz) obj;
		if (!Objects.equals(this.id, other.id)) {
			return false;
		}
		return true;
	}
}
