package io.neocdtv.modelling.reverse.reverse;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;
import com.thoughtworks.qdox.model.impl.DefaultJavaType;
import org.batchjob.uml.io.exception.NotFoundException;
import org.batchjob.uml.io.utils.Uml2Utils;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.lang.System.out;


/**
 * @author xix
 * @see http://wiki.eclipse.org/MDT/UML2/Getting_Started_with_UML2
 * @since 24.01.19
 *
 */
public class Java2EclipseUml2 {

  private final static Logger LOGGER = Logger.getLogger(Java2EclipseUml2.class.getSimpleName());
  // classifiers contained in visiblePackages will be built completely i.e. with all attributed and associations
  final static UMLFactory UML_FACTORY = UMLFactory.eINSTANCE;
  final static String UML_PACKAGE_PATH_SEPARATOR = "::";
  final static String JAVA_PACKAGE_PATH_SEPARATOR = ".";
  final static String JAVA_PACKAGE_PATH_SEPARATOR_REGEX = "\\" + JAVA_PACKAGE_PATH_SEPARATOR;
  private final Set<String> visiblePackages = new HashSet<>();
  // the rest will be just included in the model to build references from visiblePackages classifiers

  public static Model toUml(final Collection<JavaPackage> qPackages, final String modelName) {
    final Java2EclipseUml2 java2EclipseUml2 = new Java2EclipseUml2();
    return java2EclipseUml2.convert(qPackages, modelName);
  }

  Model convert(final Collection<JavaPackage> qPackages, final String modelName) {
    final Model model = createModel(modelName);
    for (JavaPackage qPackage : qPackages) {
      String packagePath = qPackage.getName();
      visiblePackages.add(packagePath);
      final Collection<JavaClass> qClasses = qPackage.getClasses();
      for (JavaClass qClass : qClasses) {
        if (qClass.isEnum()) {
          final Enumeration uEnum = getOrCreateEnum(qClass, model);
        } else {
          final Class uClass = getOrCreateClass(qClass, model);
          buildGeneralizationRelations(uClass, qClass, model);
        }
      }
    }
    return model;
  }

  private void buildGeneralizationRelations(final Class uClass, final JavaClass qClass, final Model model) {
    final List<JavaClass> implementedInterfaces = qClass.getInterfaces();
    buildInterfaceImplementation(implementedInterfaces, uClass, model);
    buildSuperClass(uClass, qClass, model);
  }

  private void buildSuperClass(final Class specificClassifier, final JavaClass qClass, final Model model) {
    final JavaClass superQClass = qClass.getSuperJavaClass();
    if (determineIfSuperClassShouldBeIncluded(superQClass)) {
      LOGGER.info("building relation to super class: " + superQClass.getCanonicalName() + " from: " + qClass.getClass().getCanonicalName());
      Class generalClassifier = getOrCreateClass(superQClass, model);
      createGeneralization(specificClassifier, generalClassifier);
    } else {
      LOGGER.info("not building super class: " + superQClass != null ? null : superQClass.getCanonicalName() + " for: " + qClass.getClass().getCanonicalName());
    }
  }

  protected static Generalization createGeneralization(
      Classifier specificClassifier, Classifier generalClassifier) {

    Generalization generalization = specificClassifier
        .createGeneralization(generalClassifier);

    out.println(String.format("Generalization %s --|> %s created.",
        specificClassifier.getQualifiedName(),
        generalClassifier.getQualifiedName()));

    return generalization;
  }


  private void buildInterfaceImplementation(final List<JavaClass> implementedInterfaces, final Class uClass, final Model model) {
    /*
    for (JavaClass implementedInterface : implementedInterfaces) {
      final String canonicalName = implementedInterface.getCanonicalName();
      LOGGER.info("building interface: " + canonicalName + " for: " + eClass.getInstanceClassName());
      LOGGER.info("building interface realization relation to interface: " + canonicalName + " from: " + eClass.getInstanceClassName());
      EClass eInterface = getOrCreateClass(implementedInterface);
      eInterface.setInterface(true);
      eClass.getESuperTypes().add(eInterface);
    }
    */
  }

  private boolean determineIfSuperClassShouldBeIncluded(final JavaClass superJavaClass) {
    return superJavaClass != null && !superJavaClass.isPrimitive(); // CHECK: && !isJavaLibraryType(superJavaClass);
  }


  // TODO: unify findClass and finaEnum  to findType, use generics?
  private Class findClass(JavaClass qClass, Model model) {
    try {
      findPackage(model, qClass.getPackageName());
      String umlClassPath = convertJavaTypePath2UmlTypePath(model.getName(), splitPackagePath(qClass.getPackageName()), qClass.getName());
      Class uClass = Uml2Utils.findElement(umlClassPath, model);
      out.println(String.format("Class '%s' found.", qClass.getFullyQualifiedName()));
      return uClass;
    } catch (NotFoundException notFoundException) {
      out.println(String.format("Class '%s' not found.", qClass.getFullyQualifiedName()));
      return null;
    }
  }

  // TODO: unify findClass and finaEnum  to findType, use generics?
  private Enumeration findEnum(JavaClass qClass, Model model) {
    try {
      findPackage(model, qClass.getPackageName());
      String umlEnumPath = convertJavaTypePath2UmlTypePath(model.getName(), splitPackagePath(qClass.getPackageName()), qClass.getName());
      Enumeration uType = Uml2Utils.findElement(umlEnumPath, model);
      out.println(String.format("Enum '%s' found.", qClass.getFullyQualifiedName()));
      return uType;
    } catch (NotFoundException notFoundException) {
      out.println(String.format("Enum '%s' not found.", qClass.getFullyQualifiedName()));
      return null;
    }
  }

  private Model createModel(final String modelName) {
    final Model model = UML_FACTORY.createModel();
    model.setName(modelName);
    return model;
  }

  private Enumeration getOrCreateEnum(final JavaClass qClass, final Model model) {
    Enumeration existingClass = findEnum(qClass, model);
    if (existingClass != null) {
      return existingClass;
    }

    return createEnum(qClass, model);
    // TODO: implement isTypeVisible(qClass) here also? this will result in a enum w/o literals

  }

  private Enumeration createEnum(final JavaClass qClass, final Model model) {
    final Enumeration uEnum = UML_FACTORY.createEnumeration();
    uEnum.setName(qClass.getName());
    out.println(String.format("Enumeration '%s' created.", uEnum.getQualifiedName()));
    qClass.getEnumConstants().forEach(enumConstant -> {
      createEnumerationLiteral(uEnum, enumConstant.getName());
    });
    addToPackage(model, qClass.getPackageName(), uEnum);
    return uEnum;
  }

  protected static EnumerationLiteral createEnumerationLiteral(
      Enumeration enumeration, String name) {

    EnumerationLiteral enumerationLiteral = enumeration
        .createOwnedLiteral(name);

    out.println(String.format("Enumeration literal '%s' created.", enumerationLiteral.getQualifiedName()));
    return enumerationLiteral;
  }

  private Class getOrCreateClass(final JavaClass qClass, final Model model) {
    Class existingClass = findClass(qClass, model);
    if (existingClass != null) {
      return existingClass;
    }

    if (isTypeVisible(qClass)) {
      Class uClass = createClass(qClass, model);
      return uClass;
    } else {
      Class classWithoutAttributes = createClassWithoutAttributes(qClass, model);
      return classWithoutAttributes;
    }
  }

  private Class createClass(final JavaClass qClass, final Model model) {
    Class uClass = createClassWithoutAttributes(qClass, model);
    for (JavaField field : qClass.getFields()) {
      JavaClass type = field.getType();
      if (isPrimitiveType(type)) {
        buildAttribute(model, uClass, field);
      } else {
        buildDependency(uClass, field, model);
      }
    }
    return uClass;
  }

  private void buildDependency(final Class uClass, JavaField field, Model model) {
    if (!field.isEnumConstant()) { // omit dependency from enum constants to the same enum.
      final JavaClass fieldType = field.getType();

      try {
        if (fieldType.isArray()) {
          JavaClass componentType = fieldType.getComponentType();
          Type referenced = getOrCreateType(model, componentType);
          addDependencyWithMultiplicityMany(uClass, field, referenced);
        } else if (fieldType.isA(Collection.class.getName())) { // is this check sufficient, will subtypes like HashSet be recognized
          final List<JavaType> actualTypeArguments = ((DefaultJavaParameterizedType) fieldType).getActualTypeArguments();
          final DefaultJavaType componentType = (DefaultJavaType) actualTypeArguments.get(0);
          Type referenced = getOrCreateType(model, componentType);
          addDependencyWithMultiplicityMany(uClass, field, referenced);
        } else if (fieldType.isA(Map.class.getName())) { // is this check sufficient, will subtypes like HashMap be recognized
          // TODO: handle maps; maps in uml?
        } else {
          Type referenced = getOrCreateType(model, fieldType);
          buildDependencyWithMultiplicityOne(uClass, field, referenced);
        }
      } catch (Exception e) {
        e.printStackTrace();

      }
    }
  }

  private Type getOrCreateType(Model model, JavaClass componentType) {
    Type referenced;
    if (componentType.isEnum()) {
      referenced = getOrCreateEnum(componentType, model);
    } else {
      referenced = getOrCreateClass(componentType, model);
    }
    return referenced;
  }

  private void buildDependencyWithMultiplicityOne(Class uType, JavaField field, Type referenced) {
    createAssociation(uType,
        true,
        AggregationKind.NONE_LITERAL,
        field.getName(),
        1,
        1,
        referenced,
        false,
        AggregationKind.NONE_LITERAL,
        "",
        0,
        1);
  }

  private void addDependencyWithMultiplicityMany(Type uType, JavaField field, Type referenced) {
    createAssociation(uType,
        true,
        AggregationKind.NONE_LITERAL,
        field.getName(),
        0,
        LiteralUnlimitedNatural.UNLIMITED,
        referenced,
        false,
        AggregationKind.NONE_LITERAL,
        "",
        0,
        1);
  }

  // TODO: handle multiplicity for arrays, lists and sets. Maps? How are they represented in uml?-
  protected Property buildAttribute(
      final Model model,
      final Class uClass,
      final JavaField field) {

    PrimitiveType primitiveAttribute = getOrCreatePrimitiveAttribute(field, model);

    final int lowerBound = 1;
    final int upperBound = 1; //LiteralUnlimitedNatural.UNLIMITED;

    final Property attribute = uClass.createOwnedAttribute(field.getName(), primitiveAttribute,
        lowerBound, upperBound);

    out.println(String.format("Attribute '%s' : %s [%s..%s] created.", //
        attribute.getQualifiedName(),
        primitiveAttribute.getName(),
        lowerBound,
        (upperBound == LiteralUnlimitedNatural.UNLIMITED)
            ? "*"
            : upperBound));

    return attribute;
  }

  protected static Association createAssociation(Type type1,
                                                 boolean end1IsNavigable, AggregationKind end1Aggregation,
                                                 String end1Name, int end1LowerBound, int end1UpperBound,
                                                 Type type2, boolean end2IsNavigable,
                                                 AggregationKind end2Aggregation, String end2Name,
                                                 int end2LowerBound, int end2UpperBound) {

    Association association = type1.createAssociation(end1IsNavigable,
        end1Aggregation, end1Name, end1LowerBound, end1UpperBound, type2,
        end2IsNavigable, end2Aggregation, end2Name, end2LowerBound,
        end2UpperBound);

    out.println(String.format("Association %s [%s..%s] %s-%s %s [%s..%s] created.", //
        UML2Util.isEmpty(end1Name)
            // compute a placeholder for the name
            ? String.format("{%s}", type1.getQualifiedName()) //
            // user-specified name
            : String.format("'%s::%s'", type1.getQualifiedName(), end1Name), //
        end1LowerBound, // no special case for this
        (end1UpperBound == LiteralUnlimitedNatural.UNLIMITED)
            ? "*" // special case for unlimited upper bound
            : end1UpperBound, // finite upper bound
        end2IsNavigable
            ? "<" // indicate navigability
            : "-", // not navigable
        end1IsNavigable
            ? ">" // indicate navigability
            : "-", // not navigable
        UML2Util.isEmpty(end2Name)
            // compute a placeholder for the name
            ? String.format("{%s}", type2.getQualifiedName()) //
            // user-specified name
            : String.format("'%s::%s'", type2.getQualifiedName(), end2Name), //
        end2LowerBound, // no special case for this
        (end2UpperBound == LiteralUnlimitedNatural.UNLIMITED)
            ? "*" // special case for unlimited upper bound
            : end2UpperBound));

    return association;
  }

  private PrimitiveType getOrCreatePrimitiveAttribute(JavaField field, Model model) {
    String name = field.getType().getName();
    try {
      PrimitiveType primitiveType = Uml2Utils.findElement(model.getName() + "::" + name, model);
      out.println(String.format("Primitive type '%s' found, create not needed.", primitiveType.getQualifiedName()));
      return primitiveType;
    } catch (NotFoundException notFoundException) {
      PrimitiveType primitiveType = model.createOwnedPrimitiveType(name);
      out.println(String.format("Primitive type '%s' created.", primitiveType.getQualifiedName()));
      return primitiveType;
    }
  }

  private void addToPackage(final Model model, final String packagePath, final Type uType) {
    final Package uPackage = findPackage(model, packagePath);
    uPackage.getOwnedTypes().add(uType);
  }

  private Class createClassWithoutAttributes(final JavaClass qClass, final Model model) {
    Class uClass = UML_FACTORY.createClass();
    uClass.setName(qClass.getName());
    out.println(String.format("Class '%s' created.", uClass.getQualifiedName()));
    addToPackage(model, qClass.getPackageName(), uClass);
    return uClass;
  }

  private boolean isPrimitiveType(JavaClass type) {
    return type.isPrimitive() ||
        type.isA(String.class.getName()) ||
        type.isA(Date.class.getName());
  }

  private boolean isTypeVisible(JavaClass type) {
    return visiblePackages.contains(type.getPackageName());
  }

  Package findPackage(final Model model, final String packagePath) {
    if (packagePath.equals("domain")) {
      System.out.println("debug");
    }
    final Package packageTree = getOrCreatePackageTree(model, packagePath);
    final String umlPath = convertJavaPackagePath2UmlPath(splitPackagePath(packagePath));
    // this is a quickfix, not sure if Uml2Utils.findElement or my code
    // handles top level packages wrong, investigate laterr
    if (packageTree.getName().equals(packagePath)){
      return packageTree;
    }
    return Uml2Utils.findElement(umlPath, packageTree);
  }

  /**
   * @param model
   * @param packagePath
   * @return package tree
   */
  Package getOrCreatePackageTree(final Model model, final String packagePath) {
    return getOrCreatePackageTree(model, model, packagePath, 0);
  }

  Package getOrCreatePackageTree(final Model model, final Package parentPackage, final String packagePath, int packagePathThreshold) {
    Package packageToGetOrCreate;
    final List<String> packagePathToGetOrCreate = getPackagePathToThreshold(packagePath, packagePathThreshold);

    try {
      final String modelName = model.getName();
      final String umlPath = convertJavaPackagePath2UmlPath(modelName, packagePathToGetOrCreate);
      packageToGetOrCreate = Uml2Utils.findElement(umlPath, model);
    } catch (NotFoundException notFoundException) {
      packageToGetOrCreate = UML_FACTORY.createPackage();
      final int lastPathIndex = packagePathToGetOrCreate.size() - 1;
      packageToGetOrCreate.setName(packagePathToGetOrCreate.get(lastPathIndex));
      parentPackage.getNestedPackages().add(packageToGetOrCreate);
    }
    if (hasSubPackages(packagePath, packagePathToGetOrCreate)) {
      getOrCreatePackageTree(model, packageToGetOrCreate, packagePath, ++packagePathThreshold);
    }
    return packageToGetOrCreate;
  }

  List<String> getPackagePathToThreshold(final String packagePath, final int packagePathThreshold) {
    final List<String> packagePathSplit = splitPackagePath(packagePath);

    final List<String> packagePathToThreshold = new ArrayList<>();
    for (int packagePathIndex = 0; packagePathIndex <= packagePathThreshold; packagePathIndex++) {
      packagePathToThreshold.add(packagePathSplit.get(packagePathIndex));
    }
    return packagePathToThreshold;
  }

  List<String> splitPackagePath(String packagePath) {
    return Arrays.asList(packagePath.split(JAVA_PACKAGE_PATH_SEPARATOR_REGEX));
  }

  String getParentPackageName(final String packagePath) {
    String[] packagePathParts = packagePath.split(JAVA_PACKAGE_PATH_SEPARATOR_REGEX);
    return packagePathParts[0];
  }

  String getChildPackagePath(final String packagePath) {
    final String[] packagePathParts = packagePath.split(JAVA_PACKAGE_PATH_SEPARATOR_REGEX);
    if (packagePathParts.length > 1) {
      ArrayList<String> childPackagePath = new ArrayList<>();
      for (int i = 1; i < packagePathParts.length; i++) {
        childPackagePath.add(packagePathParts[i]);
      }
      return childPackagePath.stream()
          .map(i -> i.toString())
          .collect(Collectors.joining(JAVA_PACKAGE_PATH_SEPARATOR));
    } else {
      return null;
    }
  }

  String convertJavaPackagePath2UmlPath(final String modelName, final List<String> packagePath) {
    final List<String> packagePathWithModelName = new ArrayList<>();
    packagePathWithModelName.add(modelName);
    packagePathWithModelName.addAll(packagePath);
    return packagePathWithModelName
        .stream()
        .collect(Collectors.joining(UML_PACKAGE_PATH_SEPARATOR));
  }

  String convertJavaPackagePath2UmlPath(final List<String> packagePath) {
    final List<String> packagePathWithModelName = new ArrayList<>();
    packagePathWithModelName.addAll(packagePath);
    return packagePathWithModelName
        .stream()
        .collect(Collectors.joining(UML_PACKAGE_PATH_SEPARATOR));
  }

  String convertJavaTypePath2UmlTypePath(final String modelName, final List<String> packagePath, final String className) {
    final List<String> path = new ArrayList<>();
    path.add(modelName);
    path.addAll(packagePath);
    path.add(className);
    return path
        .stream()
        .collect(Collectors.joining(UML_PACKAGE_PATH_SEPARATOR));
  }

  private boolean hasSubPackages(String packagePath, List<String> packagePathToGetOrCreate) {
    return splitPackagePath(packagePath).size() > packagePathToGetOrCreate.size();
  }
}