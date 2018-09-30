package io.neocdtv.modelling.reverse.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author xix
 */
public class Enumeration extends Classifier {

	// TODO: how to show in model Set<String>, currently the following is shown: constants: String
	private Set<String> constants = new HashSet<>();

	public Enumeration(String id, final String label) {
		super(id, label);
	}

	public void addConstant(final String constant) {
		constants.add(constant);
	}

	public Set<String> getConstants() {
		return constants;
	}

}
