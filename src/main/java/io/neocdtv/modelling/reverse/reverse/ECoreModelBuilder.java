package io.neocdtv.modelling.reverse.reverse;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;
import com.thoughtworks.qdox.model.impl.DefaultJavaType;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author xix
 */
public class ECoreModelBuilder {

  private final static Logger LOGGER = Logger.getLogger(ECoreModelBuilder.class.getSimpleName());

  private final static EcoreFactory ECORE_FACTORY = EcoreFactory.eINSTANCE;
  private final static EcorePackage ECORE_PACKAGE = EcorePackage.eINSTANCE;

  // contains only packages (1) selected but not packages referenced by classes from the selected packages (1) - TODO: should at the end contain all packages
  private static Set<EPackage> E_PACKAGES;
  private static Set<String> VISIBLE_PACKAGES;

  public static Set<EPackage> build(final Collection<JavaPackage> qPackages) {

    VISIBLE_PACKAGES = qPackages.stream().map(javaPackage -> javaPackage.getName()).collect(Collectors.toSet());

    E_PACKAGES = new HashSet<>();
    for (JavaPackage qPackage : qPackages) {
      EPackage ePackage = getOrCreatePackage(qPackage.getName());
      final Collection<JavaClass> qClasses = qPackage.getClasses();
      for (JavaClass qClass : qClasses) {
        EClassifier eClassifier;
        if (qClass.isEnum()) {
          eClassifier = buildEEnum(qClass);
        } else {
          eClassifier = buildEClass(qClass);
          buildGeneralizationRelations((EClass) eClassifier, qClass);
        }
        ePackage.getEClassifiers().add(eClassifier);
      }
      E_PACKAGES.add(ePackage);
    }
    return E_PACKAGES;
  }

  // TODO: what about subpackages?!?
  private static EPackage getOrCreatePackage(final String packageName) {
    Set<EPackage> collectedPackages = E_PACKAGES.stream().filter(ePackage -> ePackage.getName().equals(packageName)).collect(Collectors.toSet());
    if (collectedPackages.size() > 1) {
      throw new RuntimeException("multiple packages found in ecore model with name: " + packageName);
    } else if (collectedPackages.size() == 1) {
      return collectedPackages.iterator().next();
    } else {
      EPackage ePackage = ECORE_FACTORY.createEPackage();
      ePackage.setName(packageName);
      ePackage.setNsPrefix(buildPrefix(packageName));
      ePackage.setNsURI("http://" + packageName);
      E_PACKAGES.add(ePackage);
      return ePackage;
    }
  }

  private static void buildGeneralizationRelations(final EClass eClass, final JavaClass qClass) {
    final List<JavaClass> implementedInterfaces = qClass.getInterfaces();
    buildInterfaceImplementation(implementedInterfaces, eClass);
    buildSuperClass(eClass, qClass);
  }

  private static void buildSuperClass(final EClass eClass, final JavaClass qClass) {
    final JavaClass superQClass = qClass.getSuperJavaClass();
    if (determineIfSuperClassShouldBeIncluded(superQClass)) {
      LOGGER.info("building relation to super class: " + superQClass.getCanonicalName() + " from: " + qClass.getClass().getCanonicalName());
      EClass superEClass = buildEClass(superQClass);
      eClass.getESuperTypes().add(superEClass);
    } else {
      LOGGER.info("not building super class: " + superQClass != null ? null : superQClass.getCanonicalName() + " for: " + qClass.getClass().getCanonicalName());
    }
  }

  private static void buildInterfaceImplementation(final List<JavaClass> implementedInterfaces, final EClass eClass) {
    for (JavaClass implementedInterface : implementedInterfaces) {
      final String canonicalName = implementedInterface.getCanonicalName();
      LOGGER.info("building interface: " + canonicalName + " for: " + eClass.getInstanceClassName());
      LOGGER.info("building interface realization relation to interface: " + canonicalName + " from: " + eClass.getInstanceClassName());
      EClass eInterface = buildEClass(implementedInterface);
      eInterface.setInterface(true);
      eClass.getESuperTypes().add(eInterface);
    }
  }

  private static EClass buildEClass(JavaClass qClass) {
    final EClass eClass = buildEClassWithoutAttributes(qClass);
    for (JavaField field : qClass.getFields()) {
      JavaClass type = field.getType();
      if (type.isPrimitive()) {
        buildPrimitiveAttribute(eClass, field);
      } else {
        buildDependency(eClass, field);
      }
    }
    return eClass;
  }

  private static EClass buildEClassWithoutAttributes(JavaClass qClass) {
    final EClass eClass = ECORE_FACTORY.createEClass();
    eClass.setName(qClass.getSimpleName());
    eClass.setInstanceClassName(qClass.getFullyQualifiedName());
    EPackage aPackage = getOrCreatePackage(qClass.getPackageName());
    aPackage.getEClassifiers().add(eClass);
    return eClass;
  }

  private static boolean isTypeVisible(JavaClass type) {
    return VISIBLE_PACKAGES.contains(type.getPackageName());
  }

  private static void buildDependency(EClass eClass, JavaField field) {
    if (!field.isEnumConstant()) { // COMMENT: omit dependency from enum constants to the same enum
      final JavaClass fieldType = field.getType();
      final EReference eReference = ECORE_FACTORY.createEReference();
      eReference.setName(field.getName());

      // check if eClass is in selected package
      LOGGER.info("working on dependency from class: " + eClass.getName() + " and field " + field.getName());
      if (fieldType != null) { // TODO: understand why this can happen
        EClass referenced;
        // TODO: handle maps; maps in uml?
        // TODO: handle EEnum
        try {
          if (fieldType.isArray()) {
            referenced = buildForVisibleAndInvisibleTypes(fieldType.getComponentType());
            eReference.setContainment(true);
            eReference.setLowerBound(0);
            eReference.setUpperBound(EStructuralFeature.UNBOUNDED_MULTIPLICITY);
          } else if (fieldType.isA(Collection.class.getName())) {
            final List<JavaType> actualTypeArguments = ((DefaultJavaParameterizedType) fieldType).getActualTypeArguments();
            final DefaultJavaType genericTypeVariable = (DefaultJavaType) actualTypeArguments.get(0);
            referenced = buildForVisibleAndInvisibleTypes(genericTypeVariable);
            eReference.setContainment(true);
            eReference.setLowerBound(0);
            eReference.setUpperBound(EStructuralFeature.UNBOUNDED_MULTIPLICITY);
          } else {
            referenced = buildForVisibleAndInvisibleTypes(fieldType);
          }
          eReference.setEType(referenced);
          eClass.getEStructuralFeatures().add(eReference);
        } catch (NullPointerException e) {
          LOGGER.severe(e.getMessage());
        }
      }

    }
  }

  private static EClass buildForVisibleAndInvisibleTypes(JavaClass javaClass) {
    EClass referenced = getOrCreateClassifier(javaClass);
    if (referenced != null) {
      return referenced;
    }

    if (isTypeVisible(javaClass)) {
      referenced = buildEClass(javaClass);
    } else {
      referenced = buildEClassWithoutAttributes(javaClass);
    }
    return referenced;
  }

  private static EClass getOrCreateClassifier(JavaClass javaClass) {
    EClass existing = null;
    EPackage ePackage = getOrCreatePackage(javaClass.getPackageName());
    Set<EClassifier> collectedClassifiers = ePackage.getEClassifiers()
        .stream()
        .filter(eClassifier -> eClassifier.getInstanceClassName().equals(javaClass.getFullyQualifiedName()))
        .collect(Collectors.toSet());
    if (collectedClassifiers.size() > 1) {
      throw new RuntimeException("multiple classes found in ecore model with name: " + javaClass.getFullyQualifiedName());
    } else if (collectedClassifiers.size() == 1) {
      // should contain only classes, enums currently not supported
      LOGGER.info("found existing class: " + javaClass.getFullyQualifiedName());
      EClassifier next = collectedClassifiers.iterator().next();
      if (next instanceof EClass) {
        existing = (EClass)next;
      }
    }
    return existing;
  }

  private static void buildPrimitiveAttribute(EClass eClass, JavaField field) {
    final EAttribute eAttribute = ECORE_FACTORY.createEAttribute();
    eAttribute.setName(field.getName());
    eAttribute.setEType(mapPrimitiveType(field.getType()));
    eClass.getEStructuralFeatures().add(eAttribute);
  }

  private static EDataType mapPrimitiveType(final JavaClass type) {
    final String name = type.getName();
    if (name.equals("boolean")) {
      return ECORE_PACKAGE.getEBoolean();
    } else if (name.equals("int")) {
      return ECORE_PACKAGE.getEInt();
    }
    LOGGER.log(Level.WARNING, "Mapping for primitive type {0} not available, defaulting to EString", name);
    return ECORE_PACKAGE.getEInt();
  }

  private static EClassifier buildEEnum(JavaClass qClass) {
    final EEnum eEnum = ECORE_FACTORY.createEEnum();
    final String name = qClass.getSimpleName();
    eEnum.setName(name);
    eEnum.setInstanceClassName(qClass.getFullyQualifiedName());
    final List<JavaField> enumConstants = qClass.getEnumConstants();
    for (JavaField enumConstant : enumConstants) {
      final EEnumLiteral eEnumLiteral = ECORE_FACTORY.createEEnumLiteral();
      eEnumLiteral.setLiteral(enumConstant.getName());
      eEnumLiteral.setName(name + "." + enumConstant.getName());
      eEnum.getELiterals().add(eEnumLiteral);
    }

    return eEnum;
  }

  private static String buildPrefix(final String packageName) {
    final String[] split = packageName.split("\\.");
    return split[split.length - 1];
  }

  private static boolean determineIfSuperClassShouldBeIncluded(final JavaClass superJavaClass) {
    return superJavaClass != null && !superJavaClass.isPrimitive(); // CHECK: && !isJavaLibraryType(superJavaClass);
  }
}
