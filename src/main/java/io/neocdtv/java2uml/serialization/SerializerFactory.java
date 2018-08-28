/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.java2uml.serialization;

/**
 * @author xix
 */
public class SerializerFactory {
	private static Serializer dotSerializer;

	public static Serializer buildOrGetByName(final SerializerType commandRenderer) {
		switch (commandRenderer) {
			case DOT:
				if (dotSerializer == null) {
					dotSerializer = new DotSerializer();
				}
				return dotSerializer;
		}
		throw new RuntimeException("not implemented renderer type");
	}
}
