/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.modelling.reverse.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author xix
 */
public abstract class Classifier {

	private Set<Relation> relations = new HashSet<>();

	public abstract String getId();

	public abstract String getLabel();

	public void addRelation(Classifier toNode, RelationType relationType, Direction direction) {
		final Relation relation = new Relation(this, toNode, relationType, direction);
		relations.add(relation);
	}

	public void addRelation(Classifier toNode, RelationType relationType, Direction direction, String toNodeLabel, String toNodeCardinality) {
		final Relation relation = new Relation(this, toNode, relationType, direction, toNodeLabel, toNodeCardinality);
		relations.add(relation);
	}

	public Set<Relation> getRelations() {
		return relations;
	}
}
