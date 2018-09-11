/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.modelling.reverse.serialization;

/**
 * @author xix
 */
public class SerializerFactory {
	private static ModelSerializer dotModelSerializer;

	public static ModelSerializer buildOrGetByName(final SerializerType commandRenderer) {
		switch (commandRenderer) {
			case DOT:
				if (dotModelSerializer == null) {
					dotModelSerializer = new DotModelSerializer();
				}
				return dotModelSerializer;
		}
		throw new RuntimeException("not implemented start type");
	}
}
