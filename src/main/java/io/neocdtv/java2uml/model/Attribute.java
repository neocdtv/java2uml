/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.java2uml.model;

/**
 * @author xix
 */
public class Attribute {
	private final String name;
	private final String type;
	private final Visibility visibility;

	public Attribute(String name, String type, Visibility visibility) {
		this.name = name;
		this.type = type;
		this.visibility = visibility;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public Visibility getVisibility() {
		return visibility;
	}
}
