package io.neocdtv.modelling.reverse.reverse;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaPackage;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author xix
 */
public class UmlModelBuilder {

  private final static Logger LOGGER = Logger.getLogger(UmlModelBuilder.class.getSimpleName());

  private final static UMLFactory UML_FACTORY = UMLFactory.eINSTANCE;

  public static Set<Package> build(final Collection<JavaPackage> qPackages) {

    final Set<Package> uPackages = new HashSet<>();
    for (JavaPackage qPackage : qPackages) {
      Package uPackage = UML_FACTORY.createPackage();
      uPackage.setName(qPackage.getName());
      uPackage.setURI("http://" + qPackage.getName());

      final Collection<JavaClass> qClasses = qPackage.getClasses();
      for (JavaClass qClass : qClasses) {
        // TODO: handle qClass.isInterface() separately to build a cleaner model
        if (qClass.isEnum()) {
          Enumeration uEnumeration = uPackage.createOwnedEnumeration(qClass.getName());
          buildEnumeration(uEnumeration, qClass);
        } else if (qClass.isInterface()) {
          uPackage.createOwnedInterface(qClass.getName());
        } else {
          Class uClass = uPackage.createOwnedClass(qClass.getName(), qClass.isAbstract());
          buildClass(uClass, qClass);
        }
      }
      uPackages.add(uPackage);
    }

    buildRelations(uPackages, qPackages);
    return uPackages;
  }

  private static void buildRelations(final Set<Package> uPackages, Collection<JavaPackage> qPackages) {

    for (Package uPackage : uPackages) {
      List<Type> uClasses = uPackage
          .getOwnedTypes()
          .stream()
          .filter(uType -> uType instanceof Class).collect(Collectors.toList());

      for (Type uClass : uClasses) {
        LOGGER.info("qualifiedName " + uClass.getQualifiedName());
        String packageName = uPackage.getName();
        String typeName = uClass.getName();
        JavaClass qClass = findQClass(packageName, typeName, qPackages);
        List<JavaClass> interfaces = qClass.getInterfaces();
        interfaces.forEach(javaClass -> {
          String iPackageName = javaClass.getPackageName();
          String iTypeName = javaClass.getName();
          Type iUType = findUType(iPackageName, iTypeName, uPackages);
          if (iUType != null) {
            ((Class)uClass).createInterfaceRealization(null, (Interface) iUType);
          } else {
            LOGGER.log(Level.WARNING, "type {} not present in uml packages", javaClass.getFullyQualifiedName());
          }
        });
      }
    }
  }

  private static JavaClass findQClass(final String packageName, final String typeName, final Collection<JavaPackage> qPackages) {

    JavaPackage qPackage = qPackages
        .stream()
        .filter(javaPackage ->
            javaPackage.getName().equals(packageName)).findFirst().get();

    return qPackage.getClasses().stream().filter(javaClass -> javaClass.getName().equals(typeName)).findFirst().get();
  }

  private static Type findUType(final String packageName, final String typeName, final Set<Package> uPackages) {
    Optional<Package> first = uPackages
        .stream()
        .filter(uPackage ->
            uPackage.getName().equals(packageName)).findFirst();
    if (first.isPresent()) {
      return first.get().getOwnedType(typeName);
    } else {
      LOGGER.log(Level.WARNING, "package {} not found in uml packages", packageName);
    }
    return null;
  }

  private static Class buildClass(Class uClass, JavaClass qClass) {
    uClass.setName(qClass.getName());
    for (JavaField qField : qClass.getFields()) {
      if (determineIfFieldShouldTreatedAsAnAttribute(qField.getType())) {
        PrimitiveType primitiveType = UML_FACTORY.createPrimitiveType();
        primitiveType.setName(qField.getName());
        uClass.createOwnedAttribute(qField.getName(), primitiveType);
      }
    }

    return uClass;
  }

  private static Enumeration buildEnumeration(Enumeration uEnumeration, JavaClass qClass) {
    uEnumeration.setName(qClass.getName());
    final List<JavaField> enumConstants = qClass.getEnumConstants();
    for (JavaField enumConstant : enumConstants) {
      uEnumeration.createOwnedLiteral(enumConstant.getName());
    }

    return uEnumeration;
  }

  private static String extractPackage(final String fullyQualifiedName) {
    String name = extractName(fullyQualifiedName);
    return fullyQualifiedName.replaceAll(name + ".", "");
  }

  private static String extractName(final String fullyQualifiedName) {
    final String[] split = fullyQualifiedName.split("\\.");
    return split[split.length - 1];
  }

  private static boolean determineIfInterfaceShouldBeIncluded(final JavaClass superJavaClass) {
    return true; // CHECK:		return !isJavaLibraryType(superJavaClass);
  }

  private static boolean determineIfFieldShouldTreatedAsAnAttribute(JavaClass fieldsType) {
    return fieldsType.isPrimitive(); // CHECK: || isJavaLibraryType(fieldsType) && !fieldsType.isA(Collection.class.getName());
  }
}
