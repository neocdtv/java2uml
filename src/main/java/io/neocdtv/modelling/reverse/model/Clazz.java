/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.modelling.reverse.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class Clazz extends Classifier {
	private Set<Attribute> attributes = new HashSet<>();

	public Clazz(String id, final String label) {
		super(id, label);
	}

	public void addAttribute(final String name, final String type, final Visibility visibility) {
		attributes.add(new Attribute(name, type, visibility));
	}

	public void addAttribute(final String name, final String type, final Visibility visibility, final boolean constant) {
		attributes.add(new Attribute(name, type, visibility, constant));
	}

	public Set<Attribute> getAttributes() {
		return attributes;
	}

}