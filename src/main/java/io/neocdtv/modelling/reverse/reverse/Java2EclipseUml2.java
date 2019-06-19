package io.neocdtv.modelling.reverse.reverse;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;
import com.thoughtworks.qdox.model.impl.DefaultJavaType;
import org.batchjob.uml.io.exception.NotFoundException;
import org.batchjob.uml.io.utils.Uml2Utils;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.InterfaceRealization;
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
          // BUG? Qdox seems not be recognizing OrgType implements IOrgType
          // that is why the next code block is kicking in
          buildInterfaceRealizations(model, qClass, uEnum);
        } else if (qClass.isInterface()) {
          final Interface uInterface = getOrCreateInterface(qClass, model);
          buildGeneralizations(qClass.getInterfaces(), uInterface, model);
        } else {
          final Class uClass = getOrCreateClass(qClass, model);
          buildInterfaceRealizations(qClass.getInterfaces(), uClass, model);
          buildGeneralizations(uClass, qClass, model);
        }
      }
    }
    return model;
  }

  private void buildInterfaceRealizations(Model model, JavaClass qClass, Enumeration uEnum) {
    qClass.getInterfaces().forEach(implementedInterface -> {
      final Interface uInterface = getOrCreateInterface(implementedInterface, model);
      uEnum.createGeneralization(uInterface);
    });
  }

  private void buildGeneralizations(final Classifier specificClassifier, final JavaClass qClass, final Model model) {
    final JavaClass superQClass = qClass.getSuperJavaClass();
    if (determineIfSuperClassShouldBeIncluded(superQClass)) {
      Classifier generalClassifier = getOrCreateClass(superQClass, model);
      createGeneralization(specificClassifier, generalClassifier);
    } else {
      LOGGER.info("not building super class: " + superQClass != null ? null : superQClass.getCanonicalName() + " for: " + qClass.getClass().getCanonicalName());
    }
  }

  protected void addInterfaceRealization(
      final Interface uInterface,
      final Interface contract) {

    uInterface.createGeneralization(contract);
    out.println(String.format("Generalization %s --|> %s created.",
        uInterface.getQualifiedName(),
        contract.getQualifiedName()));
  }

  protected void addInterfaceRealization(
      final Class implementingClassifier, final Interface contract) {

    InterfaceRealization interfaceRealization = UML_FACTORY.createInterfaceRealization();
    interfaceRealization.setContract(contract);
    interfaceRealization.setImplementingClassifier(implementingClassifier);
    out.println(String.format("Interface Realization %s --|> %s created.",
        implementingClassifier.getQualifiedName(),
        contract.getQualifiedName()));

    implementingClassifier.getInterfaceRealizations().add(interfaceRealization);
  }

  protected Generalization createGeneralization(
      Classifier specificClassifier, Classifier generalClassifier) {

    Generalization generalization = specificClassifier
        .createGeneralization(generalClassifier);

    out.println(String.format("Generalization %s --|> %s created.",
        specificClassifier.getQualifiedName(),
        generalClassifier.getQualifiedName()));

    return generalization;
  }

  private void buildGeneralizations(final List<JavaClass> implementedInterfaces, final Interface anInterface, final Model model) {

    for (JavaClass implementedInterface : implementedInterfaces) {
      final Interface uInterface = getOrCreateInterface(implementedInterface, model);
      addInterfaceRealization(anInterface, uInterface);
    }
  }

  private void buildInterfaceRealizations(final List<JavaClass> implementedInterfaces, final Class uClass, final Model model) {

    for (JavaClass implementedInterface : implementedInterfaces) {
      final Interface uInterface = getOrCreateInterface(implementedInterface, model);
      addInterfaceRealization(uClass, uInterface);
    }
  }

  private boolean determineIfSuperClassShouldBeIncluded(final JavaClass superJavaClass) {
    return superJavaClass != null && !superJavaClass.isPrimitive(); // CHECK: && !isJavaLibraryType(superJavaClass);
  }

  private Classifier findClassifier(JavaClass qClass, Model model) {
    try {
      findPackage(model, qClass.getPackageName());
      String umlClassPath = convertJavaTypePath2UmlTypePath(model.getName(), splitPackagePath(qClass.getPackageName()), qClass.getName());
      Classifier uClass = Uml2Utils.findElement(umlClassPath, model);
      out.println(String.format("Classifier '%s' found.", qClass.getFullyQualifiedName()));
      return uClass;
    } catch (NotFoundException notFoundException) {
      out.println(String.format("Classifier '%s' not found.", qClass.getFullyQualifiedName()));
      return null;
    }
  }

  private Model createModel(final String modelName) {
    final Model model = UML_FACTORY.createModel();
    model.setName(modelName);
    return model;
  }

  protected static EnumerationLiteral createEnumerationLiteral(
      Enumeration enumeration, String name) {

    EnumerationLiteral enumerationLiteral = enumeration
        .createOwnedLiteral(name);

    out.println(String.format("Enumeration literal '%s' created.", enumerationLiteral.getQualifiedName()));
    return enumerationLiteral;
  }

  private Class getOrCreateClass(final JavaClass qClass, final Model model) {
    Class existing = (Class) findClassifier(qClass, model);
    if (existing != null) {
      return existing;
    }

    if (isTypeVisible(qClass)) {
      return createClass(qClass, model);
    } else {
      return createClassWithoutAttributes(qClass, model);
    }
  }

  private Enumeration getOrCreateEnum(final JavaClass qClass, final Model model) {
    Enumeration existing = (Enumeration) findClassifier(qClass, model);
    if (existing != null) {
      return existing;
    }

    if (isTypeVisible(qClass)) {
      return createEnum(qClass, model);
    } else {
      return createEnumWithoutAttributes(qClass, model);
    }
  }

  private Interface getOrCreateInterface(final JavaClass qClass, final Model model) {
    Interface existing = (Interface) findClassifier(qClass, model);
    if (existing != null) {
      return existing;
    }

    return createInterface(qClass, model);
  }

  private Class createClass(final JavaClass qClass, final Model model) {
    Class uClass = createClassWithoutAttributes(qClass, model);
    createProperties(qClass, model, uClass);

    return uClass;
  }

  private Enumeration createEnum(final JavaClass qClass, final Model model) {
    Enumeration uType = createEnumWithoutAttributes(qClass, model);
    qClass.getEnumConstants().forEach(enumConstant -> {
      createEnumerationLiteral(uType, enumConstant.getName());
    });
    createProperties(qClass, model, uType);
    return uType;
  }

  private void createProperties(JavaClass qClass, Model model, Classifier uType) {
    for (JavaField field : qClass.getFields()) {
      JavaClass type = field.getType();
      if (isPrimitiveType(type)) {
        // attributes are NOT serialized/represented on enums into ECORE and ECORE_JSON, they are visible on UML
        buildAttribute(model, uType, field);
      } else {
        // TOOO:
        buildDependency(uType, field, model);
      }
    }
  }

  private void buildDependency(final Type uClass, JavaField field, Model model) {
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
    } else if (componentType.isInterface()) {
      referenced = getOrCreateInterface(componentType, model);
    } else {
      referenced = getOrCreateClass(componentType, model);
    }
    return referenced;
  }

  private void buildDependencyWithMultiplicityOne(Type uType, JavaField field, Type referenced) {
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
      final Classifier classifier,
      final JavaField field) {

    PrimitiveType primitiveAttribute = getOrCreatePrimitiveAttribute(field, model);

    final int lowerBound = 1;
    final int upperBound = 1; //LiteralUnlimitedNatural.UNLIMITED;

    Property property = UML_FACTORY.createProperty();
    property.setLower(lowerBound);
    property.setUpper(upperBound);
    property.setName(field.getName());
    property.setType(primitiveAttribute);
    // Class and Enumeration do not have a common ancestor, which would provide the required methods to
    // add an attribute
    if (classifier instanceof Enumeration) {
      ((Enumeration) classifier).getOwnedAttributes().add(property);
    } else if (classifier instanceof Class) {
      ((Class) classifier).getOwnedAttributes().add(property);
    } else {
      throw new RuntimeException(classifier.getClass().getName() + " not supported yet");
    }

    out.println(String.format("Attribute '%s' : %s [%s..%s] created.", //
        property.getQualifiedName(),
        primitiveAttribute.getName(),
        lowerBound,
        (upperBound == LiteralUnlimitedNatural.UNLIMITED)
            ? "*"
            : upperBound));

    return property;
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

  private Enumeration createEnumWithoutAttributes(final JavaClass qClass, final Model model) {
    final Enumeration uEnum = UML_FACTORY.createEnumeration();
    uEnum.setName(qClass.getName());
    out.println(String.format("Enumeration '%s' created.", uEnum.getQualifiedName()));
    addToPackage(model, qClass.getPackageName(), uEnum);
    return uEnum;
  }

  private Interface createInterface(final JavaClass qClass, final Model model) {
    final Interface uInterface = UML_FACTORY.createInterface();
    uInterface.setName(qClass.getName());
    out.println(String.format("Interface '%s' created.", uInterface.getQualifiedName()));
    addToPackage(model, qClass.getPackageName(), uInterface);
    return uInterface;
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
    // handles top level packages wrong, investigate later
    if (packageTree.getName().equals(packagePath)) {
      return packageTree;
    }
    return Uml2Utils.findElement(umlPath, packageTree);
  }

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