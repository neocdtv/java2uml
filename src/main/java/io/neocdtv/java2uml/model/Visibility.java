/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.java2uml.model;

/**
 * @author xix
 */
public enum Visibility {

	PUBLIC("+"),
	PROTECTED("#"),
	PRIVATE("-");

	private final String umlValue;

	private Visibility(String umlValue) {
		this.umlValue = umlValue;
	}

	public String getUmlValue() {
		return umlValue;
	}
}
