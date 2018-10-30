package io.neocdtv.modelling.reverse.model.custom;

import java.util.Objects;

/**
 * @author xix
 */

public class Relation {
  private final Classifier fromNode;
  private final Classifier toNode;
  private final RelationType relationType;
  private Direction direction;
  private String toNodeLabel;
  private String fromNodeLabel;
  private boolean toNodeCardinalityCollection;
  private boolean toNodeLabelConstant = false;
  private Visibility toNodeVisibility;

  public Relation(Classifier fromNode, Classifier toNode, RelationType relationType, Direction direction) {
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.relationType = relationType;
    this.direction = direction;
  }

  public Relation(Classifier fromNode,
                  Classifier toNode,
                  RelationType relationType,
                  Direction direction,
                  String toNodeLabel,
                  boolean toNodeCardinalityCollection,
                  boolean toNodeConstant,
                  Visibility toNodeVisibility) {
    this.fromNode = fromNode;
    this.toNode = toNode;
    this.relationType = relationType;
    this.direction = direction;
    this.toNodeLabel = toNodeLabel;
    this.toNodeCardinalityCollection = toNodeCardinalityCollection;
    this.toNodeLabelConstant = toNodeConstant;
    this.toNodeVisibility = toNodeVisibility;
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

  public Classifier getFromNode() {
    return fromNode;
  }

  public Classifier getToNode() {
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

  public boolean isToNodeCardinalityCollection() {
    return toNodeCardinalityCollection;
  }

  public void setToNodeCardinalityCollection(boolean toNodeCardinalityCollection) {
    this.toNodeCardinalityCollection = toNodeCardinalityCollection;
  }

  public boolean isToNodeLabelConstant() {
    return toNodeLabelConstant;
  }

  public void setToNodeLabelConstant(boolean toNodeLabelConstant) {
    this.toNodeLabelConstant = toNodeLabelConstant;
  }

  public Visibility getToNodeVisibility() {
    return toNodeVisibility;
  }

  public void setToNodeVisibility(Visibility toNodeVisibility) {
    this.toNodeVisibility = toNodeVisibility;
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
