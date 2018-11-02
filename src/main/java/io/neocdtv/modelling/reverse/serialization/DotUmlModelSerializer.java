package io.neocdtv.modelling.reverse.serialization;

import com.thoughtworks.qdox.model.JavaClass;
import io.neocdtv.modelling.reverse.model.custom.Classifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.InterfaceRealization;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DotUmlModelSerializer {

  private final boolean doPackages = false;
  private final boolean doConstants = false;
  private Set<JavaClass> qClasses = new HashSet<>();

  public String start(final Set<Package> uPackages, final Set<JavaClass> qClasses) {
    // COMMENT: qClasses are selected to be rendered
    this.qClasses = qClasses;

    StringBuilder dot = new StringBuilder();
    dot.append("digraph G {\n");
    configureLayout(dot);

    doPackagesWithClassifiers(uPackages, dot);
    doRelations(uPackages, dot);

    dot.append("}");
    return dot.toString();
  }

  private void doPackagesWithClassifiers(final Set<Package> uPackages, StringBuilder dot) {
    for (Package uPackage : uPackages) {
      doPackageWithClassifiers(dot, uPackage);
    }
  }


  private void doRelations(Set<Package> uPackages, StringBuilder dot) {
    for (Package uPackage : uPackages) {
      List<Type> types = uPackage.getOwnedTypes()
          .stream()
          .filter(type -> type instanceof Class || type instanceof Enumeration || type instanceof Interface)
          .collect(Collectors.toList());
      types.forEach(type -> {
        if (type instanceof Class) {
          Class typeAsClass = (Class) type;
          EList<InterfaceRealization> interfaceRealizations = typeAsClass.getInterfaceRealizations();
          interfaceRealizations.forEach(interfaceRealization -> {
            Interface contract = interfaceRealization.getContract();
            doInterfaceRealization(typeAsClass, contract, dot);
          });
        }

      });
      doPackageWithClassifiers(dot, uPackage);
    }
  }

  private void doInterfaceRealization(Class uClass, Interface contract, StringBuilder dot) {
    //if (isClassifierPackageAvailable(relation.getToNode())) {
      dot.append("\t");
      dot.append("\"").append(contract.getName()).append("\"");
      dot.append(" -> ");
      dot.append("\"").append(uClass.getName()).append("\"");
      doInterfaceImplementation(dot);
      dot.append("\n");
    //}
  }

  private boolean isClassifierPackageAvailable(final EClassifier eClassifier) {
    for (JavaClass qClass : this.qClasses) {
      if (qClass.getFullyQualifiedName().equals(eClassifier.getInstanceClassName())) {

        return true;
      }
    }
    return false;
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

  private void doPackageWithClassifiers(final StringBuilder dot, final Package uPackage) {
    if (doPackages) {
      // TODO: how to render package in dot, can subraphs be configured with shape like nodes and edges?
      dot.append("\t");
      dot.append("subgraph ");
      dot.append("\"").append(uPackage.getName()).append("\"");
      dot.append(" {\n");
      dot.append("\t\t" + "label = \"").append(uPackage.getName()).append("\"\n");
    }

    uPackage.getOwnedTypes()
        .stream()
        .filter(type -> type instanceof Class)
        .forEach(type ->
            doClass(dot, (Class) type)
        );

    uPackage.getOwnedTypes()
        .stream()
        .filter(type -> type instanceof Enumeration)
        .forEach(type ->
            doEnumeration(dot, (Enumeration) type)
        );

    uPackage.getOwnedTypes()
        .stream()
        .filter(type -> type instanceof Interface)
        .forEach(type ->
            doInterface(dot, (Interface) type)
        );

    if (doPackages) {
      dot.append("\t}\n");
    }
  }

  private void doInterface(StringBuilder dot, Interface uType) {
    dot.append("\t\t");
    dot.append("\"").append(uType.getName()).append("\"");
    dot.append(" [\n");
    dot.append("\t\t\tlabel = ");
    dot.append("\"{");
    dot.append(uType.getName());
    dot.append("|");
    dot.append("}\"\n\t\t]\n");

  }

  private void doClass(final StringBuilder dot, final Class uType) {
    dot.append("\t\t");
    dot.append("\"").append(uType.getName()).append("\"");
    dot.append(" [\n");
    dot.append("\t\t\tlabel = ");
    dot.append("\"{");
    dot.append(uType.getName());
    dot.append("|");

    uType.getOwnedAttributes()
        .stream()
        .filter(property -> property.isAttribute())
        .forEach(property -> doAttribute(dot, property));

		/*
    node.getRelations().stream().filter(relation -> relation.getRelationType().equals(RelationType.DEPEDENCY)).forEach(relation -> {
			final Classifier toNode = relation.getToNode();
			if (!isClassifierPackageAvailable(toNode) && (!relation.isToNodeLabelConstant() || doConstants)) {
				doClassifierAsAttribute(dot, toNode, relation.getToNodeLabel(), relation.getToNodeVisibility());
			}
		});
		*/
    //dot.append("|"); - methods
    dot.append("}\"\n\t\t]\n");
  }

  private void doEnumeration(final StringBuilder dot, final Enumeration uType) {
    dot.append("\t\t");
    dot.append("\"").append(uType.getName()).append("\"");
    dot.append(" [\n");
    dot.append("\t\t\tlabel = ");
    dot.append("\"{");
    dot.append(uType.getName());
    dot.append("|");

    uType.getOwnedLiterals().forEach(enumerationLiteral -> {
      dot.append(enumerationLiteral.getName());
      dot.append("\\l");
    });
    //dot.append("|"); - methods
    dot.append("}\"\n\t\t]\n");
  }

  private void doAttribute(StringBuilder dot, Property eAttribute) {
    //dot.append(doVisibility(attribute.getVisibility()));
    //dot.append(" ");
    dot.append(eAttribute.getName());
    dot.append(" : ");
    dot.append(eAttribute.getType().getName());
    dot.append("\\l");
  }

  private void doClassifierAsAttribute(final StringBuilder dot, final Classifier classifier, final String name) {
    dot.append(name);
    dot.append(" : ");
    dot.append(classifier.getLabel());
    dot.append("\\l");
  }

  private void doInterfaceImplementation(StringBuilder dot) {
    dot.append(" [dir=back, style=dashed, arrowtail=empty]");
  }

  private void doInheritance(StringBuilder dot) {
    dot.append(" [dir=back, arrowtail=empty]");
  }

  private void doDependency(final EReference eReference, StringBuilder dot) {
    String label = eReference.getName();
    if (eReference.isContainment()) {
      label = label + " \n[0..*]";

    }
    dot.append(String.format(" [dir=back, arrowtail=open ,taillabel=\"%s\"]", label)); // COMMENT: play also with labelangle=\"-7\"
  }
}
