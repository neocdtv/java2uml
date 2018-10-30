package io.neocdtv.modelling.reverse.model.custom;

/**
 * @author xix
 */
public class Attribute {
  private final String name;
  private final String type;
  private final Visibility visibility;
  private boolean constant = false;

  public Attribute(String name, String type, Visibility visibility) {
    this.name = name;
    this.type = type;
    this.visibility = visibility;
  }

  public Attribute(String name, String type, Visibility visibility, final boolean constant) {
    this.name = name;
    this.type = type;
    this.visibility = visibility;
    this.constant = constant;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public Visibility getVisibility() {
    return visibility;
  }

  public boolean isConstant() {
    return constant;
  }
}
