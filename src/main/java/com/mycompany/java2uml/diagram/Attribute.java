/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.java2uml.diagram;

/**
 * @author ofco
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
