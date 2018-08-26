/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.java2uml.diagram;

/**
 *
 * @author ofco
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
