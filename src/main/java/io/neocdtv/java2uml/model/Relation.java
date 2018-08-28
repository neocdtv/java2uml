/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.java2uml.model;

import java.util.Objects;


public class Relation {
	private final IClass fromNode;
	private final IClass toNode;
	private final RelationType relationType;
	private Direction direction;
	private String toNodeLabel;
	private String fromNodeLabel;
	private String toNodeCardinality;
	private String fromNodeCardinality;

	public Relation(IClass fromNode, IClass toNode, RelationType relationType, Direction direction) {
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.relationType = relationType;
		this.direction = direction;
	}

	public String getToNodeLabel() {
		return toNodeLabel;
	}

	public void setToNodeLabel(String toNodeLabel) {
		this.toNodeLabel = toNodeLabel;
	}

	public String getFromNodeLabel() {
		return fromNodeLabel;
	}

	public void setFromNodeLabel(String fromNodeLabel) {
		this.fromNodeLabel = fromNodeLabel;
	}

	public IClass getFromNode() {
		return fromNode;
	}

	public IClass getToNode() {
		return toNode;
	}

	public RelationType getRelationType() {
		return relationType;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public String getToNodeCardinality() {
		return toNodeCardinality;
	}

	public void setToNodeCardinality(String cardinality) {
		this.toNodeCardinality = cardinality;
	}

	public String getFromNodeCardinality() {
		return fromNodeCardinality;
	}

	public void setFromNodeCardinality(String fromNodeCardinality) {
		this.fromNodeCardinality = fromNodeCardinality;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + Objects.hashCode(this.fromNode);
		hash = 97 * hash + Objects.hashCode(this.toNode);
		hash = 97 * hash + Objects.hashCode(this.relationType);
		hash = 97 * hash + Objects.hashCode(this.toNodeLabel);
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
		final Relation other = (Relation) obj;
		if (!Objects.equals(this.toNodeLabel, other.toNodeLabel)) {
			return false;
		}
		if (!Objects.equals(this.fromNode, other.fromNode)) {
			return false;
		}
		if (!Objects.equals(this.toNode, other.toNode)) {
			return false;
		}
		if (this.relationType != other.relationType) {
			return false;
		}
		return true;
	}


}
