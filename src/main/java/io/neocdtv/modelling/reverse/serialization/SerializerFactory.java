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
