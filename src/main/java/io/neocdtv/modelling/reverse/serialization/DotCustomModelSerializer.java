package io.neocdtv.modelling.reverse.serialization;

import io.neocdtv.modelling.reverse.model.custom.Classifier;
import io.neocdtv.modelling.reverse.model.custom.Attribute;
import io.neocdtv.modelling.reverse.model.custom.Clazz;
import io.neocdtv.modelling.reverse.model.custom.Enumeration;
import io.neocdtv.modelling.reverse.model.custom.Model;
import io.neocdtv.modelling.reverse.model.custom.Relation;
import io.neocdtv.modelling.reverse.model.custom.RelationType;
import io.neocdtv.modelling.reverse.model.custom.Visibility;
import io.neocdtv.modelling.reverse.model.custom.Package;

import java.util.HashSet;
import java.util.Set;

public class DotCustomModelSerializer implements ModelSerializer {

  private final boolean doPackages = false;
  private final boolean doConstants = false;
  private Set<Classifier> classes = new HashSet<>();

  @Override
  public String start(final Model model) {
    // COMMENT: classes collected in model.packages.classes are selected to be rendered
    model.getPackages().forEach(aPackage -> {
      classes.addAll(aPackage.getClassifiers());
    });
    StringBuilder dot = new StringBuilder();
    dot.append("digraph G {\n");
    configureLayout(dot);

    doPackagesWithClassifiers(model, dot);
    doRelations(model, dot);

    dot.append("}");
    return dot.toString();
  }

  private void doPackagesWithClassifiers(Model model, StringBuilder dot) {
    for (final Package currentPackage : model.getPackages()) {
      doPackageWithClassifiers(dot, currentPackage);
    }
  }

  private void doRelations(Model model, StringBuilder dot) {
    for (final Package currentPackage : model.getPackages()) {
      currentPackage.getClassifiers().forEach(classifier -> {
        classifier.getRelations().forEach(relation -> doRelation(dot, relation));
      });
      doPackageWithClassifiers(dot, currentPackage);
    }
  }

  private void configureLayout(StringBuilder dot) {
    dot.append("\tfontname  = \"Courier\"\n");
    dot.append("\tfontsize  = 8\n");
    dot.append("\tnodesep=0.9\n");
    dot.append("\tranksep=0.9\n");
    dot.append("\tsplines=polyline\n");
    //dot.append("\tsep=\"+50,50\"\n"); // meaning ??
    dot.append("\toverlap=scalexy\n");
    dot.append("\tnode [\n");
    dot.append("\t\tfontname = \"Courier\"\n");
    dot.append("\t\tfontsize  = 8\n");
    dot.append("\t\tshape  = \"record\"\n");
    dot.append("\t]\n");
    dot.append("\tedge [\n");
    dot.append("\t\tfontname = \"Courier\"\n");
    dot.append("\t\tfontsize  = 8\n");
    dot.append("\t]\n");
  }

  private void doPackageWithClassifiers(final StringBuilder dot, final Package currnetPackage) {
    if (doPackages) {
      // TODO: how to render package in dot, can subraphs be configured with shape like nodes and edges?
      dot.append("\t");
      dot.append("subgraph ");
      dot.append("\"").append(currnetPackage.getId()).append("\"");
      dot.append(" {\n");
      dot.append("\t\t" + "label = \"").append(currnetPackage.getLabel()).append("\"\n");
    }

    currnetPackage.getClassifiers().forEach(classifier -> {
      if (classifier instanceof Clazz) {
        doClass(dot, (Clazz) classifier);
      } else if (classifier instanceof Enumeration) {
        doEnumeration(dot, (Enumeration) classifier);
      }
    });
    if (doPackages) {
      dot.append("\t}\n");
    }
  }

  private void doEnumeration(final StringBuilder dot, final Enumeration node) {
    dot.append("\t\t");
    dot.append("\"").append(node.getId()).append("\"");
    dot.append(" [\n");
    dot.append("\t\t\tlabel = ");
    dot.append("\"{");
    if (node.hasStereotype()) {
      dot.append("\\<\\<" + node.getStereotype() + "\\>\\>");
      dot.append("\\n");
    }
    dot.append(node.getLabel());
    dot.append("|");
    for (String enumConstant : node.getConstants()) {
      dot.append(enumConstant);
      dot.append("\\l");
    }
    //dot.append("|"); - methods
    dot.append("}\"\n\t\t]\n");
  }

  private void doClass(final StringBuilder dot, final Clazz node) {
    dot.append("\t\t");
    dot.append("\"").append(node.getId()).append("\"");
    dot.append(" [\n");
    dot.append("\t\t\tlabel = ");
    dot.append("\"{");
    if (node.hasStereotype()) {
      dot.append("\\<\\<" + node.getStereotype() + "\\>\\>");
      dot.append("\\n");
    }
    dot.append(node.getLabel());
    dot.append("|");
    for (Attribute attribute : node.getAttributes()) {
      if (!attribute.isConstant() || doConstants) {
        doAttribute(dot, attribute);
      }
    }
    node.getRelations().stream().filter(relation -> relation.getRelationType().equals(RelationType.DEPEDENCY)).forEach(relation -> {
      final Classifier toNode = relation.getToNode();
      if (!isClassifierPackageAvailable(toNode) && (!relation.isToNodeLabelConstant() || doConstants)) {
        doClassifierAsAttribute(dot, toNode, relation.getToNodeLabel(), relation.getToNodeVisibility());
      }
    });
    //dot.append("|"); - methods
    dot.append("}\"\n\t\t]\n");
  }

  private void doAttribute(StringBuilder dot, Attribute attribute) {
    //dot.append(doVisibility(attribute.getVisibility()));
    //dot.append(" ");
    dot.append(attribute.getName());
    dot.append(" : ");
    dot.append(attribute.getType());
    dot.append("\\l");
  }

  private void doClassifierAsAttribute(StringBuilder dot, Classifier classifier, String name, Visibility visibility) {
    //dot.append(doVisibility(visibility));
    //dot.append(" ");
    dot.append(name);
    dot.append(" : ");
    dot.append(classifier.getLabel());
    dot.append("\\l");
  }

  private void doRelation(final StringBuilder dot, final Relation relation) {
    if (isClassifierPackageAvailable(relation.getToNode())) {
      dot.append("\t");
      dot.append("\"").append(relation.getToNode().getId()).append("\"");
      dot.append(" -> ");
      dot.append("\"").append(relation.getFromNode().getId()).append("\"");
      switch (relation.getRelationType()) {
        case INTERFACE_REALIZATION:
          doInterfaceImplementation(dot);
          break;
        case INHERITANCE:
          doInheritance(dot);
          break;
        case DEPEDENCY:
          doDependency(relation, dot);
          break;
      }
      dot.append("\n");
    }
  }

  private boolean isClassifierPackageAvailable(final Classifier clazz) {
    for (Classifier current : this.classes) {
      if (current.getId().equals(clazz.getId())) {
        return true;
      }
    }
    return false;
  }

  private String doVisibility(final Visibility visibility) {
    switch (visibility) {
      case PRIVATE:
        return "-";
      case PROTECTED:
        return "#";
      case PUBLIC:
        return "+";
    }
    return "";
  }

  private void doInterfaceImplementation(StringBuilder dot) {
    dot.append(" [dir=back, style=dashed, arrowtail=empty]");
  }

  private void doInheritance(StringBuilder dot) {
    dot.append(" [dir=back, arrowtail=empty]");
  }

  private void doDependency(Relation relation, StringBuilder dot) {
    switch (relation.getDirection()) {
      case BI: // COMMENT: still in progress see: method updateRelationsDirection
        final String toNodeLabel = relation.getToNodeLabel();
        final String fromNodeLabel = relation.getFromNodeLabel();
        dot.append(String.format(" [dir=none, arrowtail=empty, taillabel=\"%s\", headlabel=\"%s\"]", toNodeLabel, fromNodeLabel));
        break;
      case UNI:
        String label = relation.getToNodeLabel();
        if (relation.isToNodeCardinalityCollection()) {
          label = label + " \n[0..*]";
        }
        dot.append(String.format(" [dir=back, arrowtail=open ,taillabel=\"%s\"]", label)); // COMMENT: lay also with labelangle=\"-7\"
        break;
    }
  }
}
