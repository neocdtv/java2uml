/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.java2uml.renderer;

import io.neocdtv.java2uml.model.Model;

/**
 * @author xix
 */
public interface Renderer {
	public String renderer(final Model model);
}
