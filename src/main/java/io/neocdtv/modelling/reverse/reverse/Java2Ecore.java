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
public class Java2Ecore {

  private final static Logger LOGGER = Logger.getLogger(Java2Ecore.class.getSimpleName());

  private final static EcoreFactory ECORE_FACTORY = EcoreFactory.eINSTANCE;
  private final static EcorePackage ECORE_PACKAGE = EcorePackage.eINSTANCE;

  private Set<EPackage> ePackages;
  private Set<String> visiblePackages;

  public static Set<EPackage> toEcore(final Collection<JavaPackage> qPackages) {
    Java2Ecore java2Ecore = new Java2Ecore();
    return java2Ecore.build(qPackages);
  }

  public Set<EPackage> build(final Collection<JavaPackage> qPackages) {
    visiblePackages = qPackages.stream().map(javaPackage -> javaPackage.getName()).collect(Collectors.toSet());

    ePackages = new HashSet<>();
    for (JavaPackage qPackage : qPackages) {
      final Collection<JavaClass> qClasses = qPackage.getClasses();
      for (JavaClass qClass : qClasses) {
        EClassifier eClassifier;
        if (qClass.isEnum()) {
          buildEEnum(qClass);
        } else {
          eClassifier = getOrCreateClass(qClass);
          buildGeneralizationRelations((EClass) eClassifier, qClass);
        }
      }
    }
    return ePackages;
  }

  private void addToPackage(final EClassifier eClassifier, final String packageName) {
    EPackage ePackage = getOrCreatePackage(packageName);
    ePackage.getEClassifiers().add(eClassifier);
    ePackages.add(ePackage);
  }

  // TODO: what about subpackages?!?
  private EPackage getOrCreatePackage(final String packageName) {
    Set<EPackage> collectedPackages = ePackages.stream().filter(ePackage -> ePackage.getName().equals(packageName)).collect(Collectors.toSet());
    if (collectedPackages.size() > 1) {
      throw new RuntimeException("multiple packages found in ecore model with name: " + packageName);
    } else if (collectedPackages.size() == 1) {
      return collectedPackages.iterator().next();
    } else {
      EPackage ePackage = ECORE_FACTORY.createEPackage();
      ePackage.setName(packageName);
      ePackage.setNsPrefix(buildPrefix(packageName));
      ePackage.setNsURI("http://" + packageName);
      ePackages.add(ePackage);
      return ePackage;
    }
  }

  private void buildGeneralizationRelations(final EClass eClass, final JavaClass qClass) {
    final List<JavaClass> implementedInterfaces = qClass.getInterfaces();
    buildInterfaceImplementation(implementedInterfaces, eClass);
    buildSuperClass(eClass, qClass);
  }

  private void buildSuperClass(final EClass eClass, final JavaClass qClass) {
    final JavaClass superQClass = qClass.getSuperJavaClass();
    if (determineIfSuperClassShouldBeIncluded(superQClass)) {
      LOGGER.info("building relation to super class: " + superQClass.getCanonicalName() + " from: " + qClass.getClass().getCanonicalName());
      EClass superEClass = getOrCreateClass(superQClass);
      eClass.getESuperTypes().add(superEClass);
    } else {
      LOGGER.info("not building super class: " + superQClass != null ? null : superQClass.getCanonicalName() + " for: " + qClass.getClass().getCanonicalName());
    }
  }

  private void buildInterfaceImplementation(final List<JavaClass> implementedInterfaces, final EClass eClass) {
    for (JavaClass implementedInterface : implementedInterfaces) {
      final String canonicalName = implementedInterface.getCanonicalName();
      LOGGER.info("building interface: " + canonicalName + " for: " + eClass.getInstanceClassName());
      LOGGER.info("building interface realization relation to interface: " + canonicalName + " from: " + eClass.getInstanceClassName());
      EClass eInterface = getOrCreateClass(implementedInterface);
      eInterface.setInterface(true);
      eClass.getESuperTypes().add(eInterface);
    }
  }

  private EClass buildEClass(JavaClass qClass) {
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

  private EClass buildEClassWithoutAttributes(JavaClass qClass) {
    final EClass eClass = ECORE_FACTORY.createEClass();
    eClass.setName(qClass.getSimpleName());
    eClass.setInstanceClassName(qClass.getFullyQualifiedName());
    addToPackage(eClass, qClass.getPackageName());
    return eClass;
  }

  private boolean isTypeVisible(JavaClass type) {
    return visiblePackages.contains(type.getPackageName());
  }

  private void buildDependency(EClass eClass, JavaField field) {
    if (!field.isEnumConstant()) { // COMMENT: omit dependency from enum constants to the same enum
      final JavaClass fieldType = field.getType();
      final EReference eReference = ECORE_FACTORY.createEReference();
      eReference.setName(field.getName());

      // check if eClass is in selected package
      LOGGER.info("working on dependency from class: " + eClass.getName() + " and field " + field.getName());
      //if (fieldType != null) { // TODO: understand why this can happen
      EClass referenced;
      // TODO: handle maps; maps in uml?
      // TODO: handle EEnum
      try {
        if (fieldType.isArray()) {
          referenced = getOrCreateClass(fieldType.getComponentType());
          eReference.setContainment(true);
          eReference.setLowerBound(0);
          eReference.setUpperBound(EStructuralFeature.UNBOUNDED_MULTIPLICITY);
        } else if (fieldType.isA(Collection.class.getName())) {
          final List<JavaType> actualTypeArguments = ((DefaultJavaParameterizedType) fieldType).getActualTypeArguments();
          final DefaultJavaType genericTypeVariable = (DefaultJavaType) actualTypeArguments.get(0);
          referenced = getOrCreateClass(genericTypeVariable);
          eReference.setContainment(true);
          eReference.setLowerBound(0);
          eReference.setUpperBound(EStructuralFeature.UNBOUNDED_MULTIPLICITY);
        } else {
          referenced = getOrCreateClass(fieldType);
        }
        eReference.setEType(referenced);
        eClass.getEStructuralFeatures().add(eReference);
      } catch (NullPointerException e) {
        LOGGER.severe(e.getMessage());
      }
      //}
    }
  }

  private EClass getOrCreateClass(JavaClass javaClass) {
    EClass referenced = findClass(javaClass);
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

  private EClass findClass(JavaClass javaClass) {
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
        existing = (EClass) next;
      }
    }
    return existing;
  }

  // TODO: does Attribute needs to be added to ePackages?
  private void buildPrimitiveAttribute(EClass eClass, JavaField field) {
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

  // TODO: why aren't there relations from enum? i.e. generalizations, depedencies?
  // TODO: maybe just switch to eClass and convert enumConstants to field (constant)?
  private EClassifier buildEEnum(JavaClass qClass) {
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

    addToPackage(eEnum, qClass.getPackageName());
    return eEnum;
  }

  private String buildPrefix(final String packageName) {
    final String[] split = packageName.split("\\.");
    return split[split.length - 1];
  }

  private boolean determineIfSuperClassShouldBeIncluded(final JavaClass superJavaClass) {
    return superJavaClass != null && !superJavaClass.isPrimitive(); // CHECK: && !isJavaLibraryType(superJavaClass);
  }
}
