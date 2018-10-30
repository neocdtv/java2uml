package io.neocdtv.modelling.reverse.model.custom;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author xix
 */
public class Package {
	private final String id;
	private final String label;
	private final String name;
	private Set<Classifier> classifiers = new HashSet<>();

	public Package(String name) {
		this.id = "cluster." + name;
		this.label = "Package " + name;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public void addClassifier(final Classifier clazz) {
		getClassifiers().add(clazz);
	}

	public Set<Classifier> getClassifiers() {
		return classifiers;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + Objects.hashCode(this.id);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Package other = (Package) obj;
		if (!Objects.equals(this.id, other.id)) {
			return false;
		}
		return true;
	}
}