package io.neocdtv.java2uml.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author xix
 */
public class Package {
	private final String id;
	private final String label;
	private Set<Classifier> classifiers = new HashSet<>();

	public Package(String name) {
		this.id = "cluster." + name;
		this.label = "Package " + name;
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