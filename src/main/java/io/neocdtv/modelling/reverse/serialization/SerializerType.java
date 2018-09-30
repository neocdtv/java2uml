package io.neocdtv.modelling.reverse.serialization;

/**
 * @author xix
 */
public enum SerializerType {
	DOT("dot"),
	XML("xmi");

	private String value;

	SerializerType(final String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static SerializerType valueOfByValue(final String value) {
		for (SerializerType serializerType : SerializerType.values()) {
			if (serializerType.getValue().equals(value)) {
				return serializerType;
			}
		}
		throw new RuntimeException("value not supported");
	}
}
