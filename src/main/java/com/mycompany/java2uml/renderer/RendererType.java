/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.java2uml.renderer;

/**
 *
 * @author ofco
 */
public enum RendererType {
    DOT("dot"),
    XML("xmi");
    
    private String value;
    
    private RendererType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    public static RendererType valueOfByValue(final String value) {
        for (RendererType rendererType: RendererType.values()) {
            if (rendererType.getValue().equals(value)) {
                return rendererType;
            }
        }
        throw new RuntimeException("value not supported");
    }
}
