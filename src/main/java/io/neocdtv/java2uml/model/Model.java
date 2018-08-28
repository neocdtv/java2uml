/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.java2uml.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author xix
 */
public class Model {
	private Set<Clazz> classes = new HashSet<>();
	private Set<Relation> relations = new HashSet<>();
	private Set<Package> packages = new HashSet<>();

	public void addClass(final Clazz node) {
		classes.add(node);
	}

	public void addRelations(final Set<Relation> relations) {
		this.relations.addAll(relations);
	}

	public void addPackage(final Package packageToAdd) {
		packages.add(packageToAdd);
	}

	public Set<Clazz> getClasses() {
		return classes;
	}

	public Set<Relation> getRelations() {
		return relations;
	}

	public Package getPackage(final String packageToGet) {
		for (Package subGraph : packages) {
			if (subGraph.equals(new Package(packageToGet))) {
				return subGraph;
			}
		}
		final Package packageToReturn = new Package(packageToGet);
		packages.add(packageToReturn);
		return packageToReturn;
	}

	public Set<Package> getPackages() {
		return packages;
	}
}
