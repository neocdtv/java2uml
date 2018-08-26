/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.java2uml.renderer;

import com.mycompany.java2uml.reverse.DotRenderer;

/**
 *
 * @author ofco
 */
public class RendererFactory {
    private static Renderer dotRenderer;
    
    public static Renderer buildOrGetByName(final RendererType commandRenderer) {
        switch (commandRenderer) {
            case DOT:
                if (dotRenderer==null) {
                    dotRenderer = new DotRenderer();
                }
                return dotRenderer;
        }
        throw new RuntimeException("not implemented renderer type");
    }
}
