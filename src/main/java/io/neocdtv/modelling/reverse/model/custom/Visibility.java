package io.neocdtv.modelling.reverse.model.custom;

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
