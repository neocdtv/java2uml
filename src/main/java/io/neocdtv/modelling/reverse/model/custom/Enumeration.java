package io.neocdtv.modelling.reverse.model.custom;

import java.util.HashSet;
import java.util.Set;

/**
 * @author xix
 */
public class Enumeration extends Classifier {

  // TODO: how to show in model Set<String>, currently the following is shown: constants: String
  // try: if classifier will be rendered as an attribute add (0..* ->) e.g.
  // (0..* -> ) String (-> can also be a link to this type, if available on a second diagram)
  // (->) Person
  private Set<String> constants = new HashSet<>();

  public Enumeration(String id, final String label, final String packageName) {
    super(id, label, packageName, null);
  }

  public void addConstant(final String constant) {
    constants.add(constant);
  }

  public Set<String> getConstants() {
    return constants;
  }

}
