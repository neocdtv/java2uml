/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.modelling.reverse.model;

/**
 * @author xix
 */
public class Attribute {
	private final String name;
	private final String type;
	private final Visibility visibility;
	private boolean constant = false;

	public Attribute(String name, String type, Visibility visibility) {
		this.name = name;
		this.type = type;
		this.visibility = visibility;
	}

	public Attribute(String name, String type, Visibility visibility, final boolean constant) {
		this.name = name;
		this.type = type;
		this.visibility = visibility;
		this.constant = constant;
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

	public boolean isConstant() {
		return constant;
	}
}
