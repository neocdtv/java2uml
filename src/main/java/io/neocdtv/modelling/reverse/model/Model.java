package io.neocdtv.modelling.reverse.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author xix
 */
public class Model {
	private Set<Package> packages = new HashSet<>();

	public void addPackage(final Package packageToAdd) {
		packages.add(packageToAdd);
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
