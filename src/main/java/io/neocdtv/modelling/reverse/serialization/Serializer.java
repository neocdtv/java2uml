/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.modelling.reverse.serialization;

import io.neocdtv.modelling.reverse.model.Model;

/**
 * @author xix
 */
public interface Serializer {
	String renderer(final Model model);
}
