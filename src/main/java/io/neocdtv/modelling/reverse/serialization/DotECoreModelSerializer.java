package io.neocdtv.modelling.reverse.serialization;

import com.thoughtworks.qdox.model.JavaClass;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DotECoreModelSerializer {

  private static final Logger LOGGER = Logger.getLogger(DotECoreModelSerializer.class.getName());

  private final boolean renderPackages = false;
  private final boolean renderConstants = false;
  private final boolean linkToPackage = false;
  private Set<JavaClass> qClasses = new HashSet<>();


  public String start(final Set<EPackage> ePackages, final Set<JavaClass> qClasses) {
    // COMMENT: qClasses are selected to be rendered
    this.qClasses = qClasses;

    StringBuilder dot = new StringBuilder();
    dot.append("digraph G {\n");
    configureLayout(dot);

    doPackagesWithClassifiers(ePackages, dot);
    doRelations(ePackages, dot);

    dot.append("}");
    return dot.toString();
  }

  private void doPackagesWithClassifiers(final Set<EPackage> ePackages, StringBuilder dot) {
    for (EPackage ePackage : ePackages) {
      doPackageWithClassifiers(dot, ePackage);
    }
  }

  private void doRelations(Set<EPackage> ePackages, StringBuilder dot) {
    for (EPackage ePackage : ePackages) {
      for (EClassifier eClassifier : ePackage.getEClassifiers()) {
        if (eClassifier instanceof EClass) {
          EClass eClass = (EClass) eClassifier;
          eClass.getEReferences().forEach(eReference -> {
            doDependency(eClass, eReference, dot);
          });
          doInterfaces(eClass, dot);
          doSuperClasses(eClass, dot);
        }
      }
      doPackageWithClassifiers(dot, ePackage);
    }
  }

  private void doSuperClasses(final EClass eClass, final StringBuilder dot) {
    eClass
        .getESuperTypes()
        .stream()
        .filter(eClass1 -> !eClass1.isInterface())
        .collect(Collectors.toSet())
        .forEach(eClass1 -> {
          if (isClassifierPackageAvailable(eClass1)) {
            dot.append("\t");
            dot.append("\"").append(eClass1.getInstanceClassName()).append("\"");
            dot.append(" -> ");
            dot.append("\"").append(eClass.getInstanceClassName()).append("\"");
            doInheritance(dot);
            dot.append("\n");
          }
        });
  }

  private void doInterfaces(final EClass eClass, final StringBuilder dot) {
    eClass
        .getESuperTypes()
        .stream()
        .filter(eClass1 -> eClass1.isInterface())
        .collect(Collectors.toSet())
        .forEach(eClass1 -> {
          if (isClassifierPackageAvailable(eClass1)) {
            dot.append("\t");
            dot.append("\"").append(eClass1.getInstanceClassName()).append("\"");
            dot.append(" -> ");
            dot.append("\"").append(eClass.getInstanceClassName()).append("\"");
            doInterfaceImplementation(dot);
            dot.append("\n");
          }
        });
  }

  private void doDependency(EClass eClassifier, final EReference relation, final StringBuilder dot) {
    if (isClassifierPackageAvailable(relation.getEReferenceType())) {
      dot.append("\t");
      dot.append("\"").append(relation.getEReferenceType().getInstanceClassName()).append("\"");
      dot.append(" -> ");
      dot.append("\"").append(eClassifier.getInstanceClassName()).append("\"");
      doDependency(relation, dot);
      dot.append("\n");
    }
  }

  private boolean isClassifierPackageAvailable(final EClassifier eClassifier) {
    LOGGER.info("eClassifier " + eClassifier);
    for (JavaClass qClass : this.qClasses) {
      LOGGER.finest("isClassifierPackageAvailable, qClass: " + qClass.getFullyQualifiedName() + ", eClassifier: " + eClassifier.getInstanceClassName());
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

  private void doPackageWithClassifiers(final StringBuilder dot, final EPackage ePackage) {
    if (renderPackages) {
      // TODO: how to render package in dot, can subraphs be configured with shape like nodes and edges?
      dot.append("\t");
      dot.append("subgraph ");
      dot.append("\"").append(ePackage.getName()).append("\"");
      dot.append(" {\n");
      dot.append("\t\t" + "label = \"").append(ePackage.getNsPrefix()).append("\"\n");
    }
    LOGGER.info("ePackage: " + ePackage.getName());
    ePackage.getEClassifiers().forEach(eClassifier -> {
      if (eClassifier instanceof EClass) {
        doClass(dot, (EClass) eClassifier);
      } else if (eClassifier instanceof EEnum) {
        doEnumeration(dot, (EEnum) eClassifier);
      }
    });
    if (renderPackages) {
      dot.append("\t}\n");
    }
  }

  private void doClass(final StringBuilder dot, final EClass eClass) {
    LOGGER.info("eClass: " + eClass.getName());
    dot.append("\t\t");
    dot.append("\"").append(eClass.getInstanceClassName()).append("\"");
    dot.append(" [\n");
    dot.append("\t\t\tlabel = ");
    dot.append("\"{");
    dot.append(eClass.getName());
    dot.append("|");

    for (EStructuralFeature eStructuralFeature : eClass.getEStructuralFeatures()) {
      LOGGER.info("eStructuralFeature: " + eStructuralFeature.getName());
      if (eStructuralFeature instanceof EAttribute) {
        doAttribute(dot, (EAttribute) eStructuralFeature);
      } else if (isTypeInvisible(eStructuralFeature)) {
        // TODO: why are Enums working???
        doClassifierAsAttribute(dot, ((EReference)eStructuralFeature).getEReferenceType(), eStructuralFeature.getName());
      } else {
        LOGGER.warning("omited structural feature " + eStructuralFeature.getName());
      }
    }

    //dot.append("|"); - methods
    dot.append("}\"\n\t\t]\n");
  }

  private boolean isTypeInvisible(EStructuralFeature eStructuralFeature) {
    LOGGER.info("eStructuralFeature: " + eStructuralFeature);
    boolean isEReference = eStructuralFeature instanceof EReference;
    if (isEReference) {
      EClass eReferenceType = ((EReference) eStructuralFeature).getEReferenceType();
      LOGGER.info("eReferenceType: " + eReferenceType);
      if (!isClassifierPackageAvailable(eReferenceType)) {
        return true;
      }
    }
    return false;
  }

  private void doEnumeration(final StringBuilder dot, final EEnum eEnum) {
    LOGGER.info("doEnumeration: " + eEnum.getName());
    dot.append("\t\t");
    dot.append("\"").append(eEnum.getInstanceClassName()).append("\"");
    dot.append(" [\n");
    dot.append("\t\t\tlabel = ");
    dot.append("\"{");
    dot.append(eEnum.getName());
    dot.append("|");
    for (EEnumLiteral eEnumLiteral : eEnum.getELiterals()) {
      dot.append(eEnumLiteral.getLiteral());
      dot.append("\\l");
    }
    //dot.append("|"); - methods
    dot.append("}\"\n\t\t]\n");
  }

  private void doAttribute(StringBuilder dot, EAttribute eAttribute) {
    LOGGER.finest("doAttribute, name: " + eAttribute.getName() + ", type: " + eAttribute.getEAttributeType().getName());
    dot.append(eAttribute.getName());
    dot.append(" : ");
    dot.append(eAttribute.getEAttributeType().getName());
    dot.append("\\l");
  }

  private void doClassifierAsAttribute(final StringBuilder dot, final EClassifier eClassifier, final String attributeName) {
    LOGGER.finest("doClassifierAsAttribute, name: " + attributeName + ", type: " + eClassifier.getName());
    if (linkToPackage) {
      PackageLink packageLink = new PackageLink();
      packageLink.setName(attributeName);
      // NullPointerException, not every eClassifier has a valid package?!?
      //packageLink.setPackageName(eClassifier.getEPackage().getName());
      packageLink.setType(eClassifier.getName());
      // TODO: serialize packageLink to json and use instead of hardcoded: dot.append(packageLink.getMarker());
      dot.append(packageLink);
      dot.append("\\l");
    } else {
      dot.append(attributeName);
      dot.append(" : ");
      dot.append(eClassifier.getName());
      dot.append("\\l");
    }
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
