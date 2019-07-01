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
import java.util.stream.Collectors;

import static java.lang.System.out;


/**
 * @author xix
 * @see http://wiki.eclipse.org/MDT/UML2/Getting_Started_with_UML2
 * @since 24.01.19
 */
public class Java2EclipseUml2 {

  // classifiers contained in visiblePackages will be build completely i.e. with all attributed and associations
  // classifiers not contained in visiblePackages will be build as a "empty" type e.g. class without attributes
  // TODO: decide what to do with enums not in visiblePackages, is it even possible to access literals, which are not in qdox source?
  // TODO: how to handle types from packages like: java.lang, javax?
  // TODO: add unit tests!
  private final static UMLFactory UML_FACTORY = UMLFactory.eINSTANCE;
  private final static String UML_PACKAGE_PATH_SEPARATOR = "::";
  private final static String JAVA_PACKAGE_PATH_SEPARATOR = ".";
  private final static String JAVA_PACKAGE_PATH_SEPARATOR_REGEX = "\\" + JAVA_PACKAGE_PATH_SEPARATOR;
  private final Set<String> visiblePackages = new HashSet<>();

  public static Model toUml(final Collection<JavaPackage> qPackages, final String modelName) {
    final Java2EclipseUml2 java2EclipseUml2 = new Java2EclipseUml2();
    return java2EclipseUml2.convert(qPackages, modelName);
  }

  Model convert(final Collection<JavaPackage> qPackages, final String modelName) {
    configureVisiblePackages(qPackages);
    final Model model = createModel(modelName);
    for (JavaPackage qPackage : qPackages) {
      final Collection<JavaClass> qClasses = qPackage.getClasses();
      for (JavaClass qClass : qClasses) {
        getOrCreateType(qClass, model);
      }
    }
    return model;
  }

  private void configureVisiblePackages(final Collection<JavaPackage> qPackages) {
    qPackages.forEach(qPackage -> {
      String packagePath = qPackage.getName();
      visiblePackages.add(packagePath);
    });
  }

  private void buildInterfaceRealizations(final JavaClass qClass, final Enumeration uEnum, final Model model) {
    qClass.getInterfaces().forEach(implementedInterface -> {
      final Interface uInterface = getOrCreateInterface(implementedInterface, model);
      uEnum.createGeneralization(uInterface);
    });
  }

  private void buildGeneralizations(final Classifier uSpecificClassifier, final JavaClass qSpecificClass, final Model model) {
    final JavaClass qGeneralClass = qSpecificClass.getSuperJavaClass();
    if (shouldGeneralizationBeProcessed(qGeneralClass)) {
      Classifier generalClassifier = getOrCreateClass(qGeneralClass, model);
      createGeneralization(uSpecificClassifier, generalClassifier);
    } else {
      out.println(String.format("Generalization %s --|> %s skipped.", qSpecificClass.getClass().getCanonicalName(), qGeneralClass != null ? null : qGeneralClass.getCanonicalName()));
    }
  }

  protected void buildInterfaceRealization(
      final Class uImplementingClassifier, final Interface uContract) {

    InterfaceRealization interfaceRealization = UML_FACTORY.createInterfaceRealization();
    interfaceRealization.setContract(uContract);
    interfaceRealization.setImplementingClassifier(uImplementingClassifier);
    out.println(String.format("Interface Realization %s --|> %s created.",
        uImplementingClassifier.getQualifiedName(),
        uContract.getQualifiedName()));

    uImplementingClassifier.getInterfaceRealizations().add(interfaceRealization);
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

  private void buildGeneralizations(final List<JavaClass> qGeneralInterfaces, final Interface uSpecificInterface, final Model model) {

    for (JavaClass implementedInterface : qGeneralInterfaces) {
      final Interface uGeneralInterface = getOrCreateInterface(implementedInterface, model);

      uSpecificInterface.createGeneralization(uGeneralInterface);
      out.println(String.format("Generalization %s --|> %s created.",
          uSpecificInterface.getQualifiedName(),
          uGeneralInterface.getQualifiedName()));
    }
  }

  private void buildInterfaceRealizations(final List<JavaClass> implementedInterfaces, final Class uClass, final Model model) {

    for (JavaClass implementedInterface : implementedInterfaces) {
      final Interface uInterface = getOrCreateInterface(implementedInterface, model);
      buildInterfaceRealization(uClass, uInterface);
    }
  }

  private boolean shouldGeneralizationBeProcessed(final JavaClass superJavaClass) {
    return superJavaClass != null && !superJavaClass.isPrimitive();
    // CHECK: && !isJavaLibraryType(superJavaClass);, where lib in package java*
  }

  private Classifier findClassifier(JavaClass qClass, Model model) {
    try {
      findPackage(model, qClass.getPackageName());
      String umlClassPath = convertJavaTypePath2UmlTypePath(model.getName(), splitPackagePath(qClass.getPackageName()), qClass.getName());
      Classifier uClass = Uml2Utils.findElement(umlClassPath, model);
      out.println(String.format("Classifier '%s' found. Create is not needed.", qClass.getFullyQualifiedName()));
      return uClass;
    } catch (NotFoundException notFoundException) {
      out.println(String.format("Classifier '%s' not found. Create is needed.", qClass.getFullyQualifiedName()));
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

  // TODO: should buildInterfaceRealizations and buildGeneralizations be also considered for isTypeVisible==false?
  private Class getOrCreateClass(final JavaClass qClass, final Model model) {
    Class existing = (Class) findClassifier(qClass, model);
    if (existing != null) {
      return existing;
    }

    if (isTypeVisible(qClass)) {
      final Class uClass = createClass(qClass, model);
      buildInterfaceRealizations(qClass.getInterfaces(), uClass, model);
      buildGeneralizations(uClass, qClass, model);
      return uClass;
    } else {
      return createClassWithoutAttributes(qClass, model);
    }
  }

  private Interface getOrCreateInterface(final JavaClass qClass, final Model model) {
    Interface existing = (Interface) findClassifier(qClass, model);
    if (existing != null) {
      return existing;
    }

    final Interface uInterface = createInterface(qClass, model);
    buildGeneralizations(qClass.getInterfaces(), uInterface, model);
    return uInterface;
  }

  // TODO: should buildInterfaceRealizations be also considered for isTypeVisible==false?
  private Enumeration getOrCreateEnum(final JavaClass qClass, final Model model) {
    Enumeration existing = (Enumeration) findClassifier(qClass, model);
    if (existing != null) {
      return existing;
    }

    if (isTypeVisible(qClass)) {
      Enumeration uEnum = createEnum(qClass, model);
      // BUG? Qdox seems not be recognizing OrgType implements IOrgType
      // that is why the next code block is not kicking in
      buildInterfaceRealizations(qClass, uEnum, model);
      return uEnum;
    } else {
      return createEnumWithoutAttributes(qClass, model);
    }
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
      if (isContainer(type)) {
        // multiplicity 0..*
      } else {
        // multiplicity 1..1
      }
      /*
      if multiply type
         
       */
      if (isPrimitiveType(type)) {
        // attributes are NOT serialized/represented on enums into/in ECORE and ECORE_JSON, but into/in UML
        buildAttribute(uType, field, model);
      } else {
        buildDependency(uType, field, model);
      }
    }
  }

  private boolean isContainer(JavaClass type) {
    return type.isArray() ||
        type.isA(Collection.class.getName()) ||
        type.isA(Map.class.getName());
  }

  // TODO: check usage Type vs Classifier
  private void buildDependency(final Classifier uClassifier, JavaField field, Model model) {
    if (!field.isEnumConstant()) { // omit dependency from enum constants to the same enum.
      final JavaClass fieldType = field.getType();

      if (fieldType.isArray()) {
        handleArray(uClassifier, field, model, fieldType);
      } else if (fieldType.isA(Collection.class.getName())) { // TODO: is this check sufficient, will subtypes like HashSet be recognized
        handleCollection(uClassifier, field, model, (DefaultJavaParameterizedType) fieldType);
      } else if (fieldType.isA(Map.class.getName())) { // TODO: is this check sufficient, will subtypes like HashMap be recognized
        // TODO: handle maps;
        // TODO: how to represent maps in UML
        out.println("Association to maps skipped.");
      } else {
        final Type referenced = getOrCreateType(fieldType, model);
        buildDependencyWithMultiplicityOne(uClassifier, field, referenced);
      }
    }
  }

  private void handleCollection(Type uClass, JavaField field, Model model, DefaultJavaParameterizedType fieldType) {
    final List<JavaType> actualTypeArguments = fieldType.getActualTypeArguments();
    // TODO: is get(0) a problem, can there be > 1
    // TODO: handle old school without generics?
    final DefaultJavaType componentType = (DefaultJavaType) actualTypeArguments.get(0);
    final Type referenced = getOrCreateType(componentType, model);
    buildDependencyWithMultiplicityMany(uClass, field, referenced);
  }

  private void handleArray(Type uClass, JavaField field, Model model, JavaClass fieldType) {
    final JavaClass componentType = fieldType.getComponentType();
    final Type referenced = getOrCreateType(componentType, model);
    buildDependencyWithMultiplicityMany(uClass, field, referenced);
  }

  private Type getOrCreateType(final JavaClass qClass, final Model model) {
    Type referenced;
    if (qClass.isEnum()) {
      referenced = getOrCreateEnum(qClass, model);
    } else if (qClass.isInterface()) {
      referenced = getOrCreateInterface(qClass, model);
    } else {
      referenced = getOrCreateClass(qClass, model);
    }
    return referenced;
  }

  private void buildDependencyWithMultiplicityOne(final Type uFrom, final JavaField qField, final Type uTo) {
    createAssociation(uFrom,
        true,
        AggregationKind.NONE_LITERAL,
        qField.getName(),
        1,
        1,
        uTo,
        false,
        AggregationKind.NONE_LITERAL,
        "",
        0,
        1);
  }

  private void buildDependencyWithMultiplicityMany(final Type uFrom, final JavaField qField, final Type uTo) {
    createAssociation(uFrom,
        true,
        AggregationKind.NONE_LITERAL,
        qField.getName(),
        0,
        LiteralUnlimitedNatural.UNLIMITED,
        uTo,
        false,
        AggregationKind.NONE_LITERAL,
        "",
        0,
        1);
  }

  // TODO: handle multiplicity to primitive attributes
  // TODO: how is multiplicity to primitive attributes represented in UML?
  protected Property buildAttribute(
      final Classifier uClassifier,
      final JavaField field,
      final Model model) {

    Type primitiveAttribute = getOrCreatePrimitiveAttribute(field, model);

    final int lowerBound = 1;
    final int upperBound = 1; //LiteralUnlimitedNatural.UNLIMITED;

    Property property = UML_FACTORY.createProperty();
    property.setLower(lowerBound);
    property.setUpper(upperBound);
    property.setName(field.getName());
    property.setType(primitiveAttribute);
    // Class and Enumeration do not have a common ancestor in Eclipse UML, which would provide the required methods to
    // add an attribute, thats why the next if-else part
    // handling of interface  not needed, since no attributes are possible
    if (uClassifier instanceof Enumeration) {
      ((Enumeration) uClassifier).getOwnedAttributes().add(property);
    } else if (uClassifier instanceof Class) {
      ((Class) uClassifier).getOwnedAttributes().add(property);
    } else {
      throw new RuntimeException(uClassifier.getClass().getName() + " not supported yet");
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

  // TODO: refactor somehow, this is really unreadable!
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

  // TODO: diff between attribute and primitive attribute?
  private PrimitiveType getOrCreatePrimitiveAttribute(JavaField field, Model model) {
    String name = field.getType().getName();
    try {
      PrimitiveType primitiveType = Uml2Utils.findElement(model.getName() + "::" + name, model);
      out.println(String.format("Primitive type '%s' found. Create not needed.", primitiveType.getQualifiedName()));
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
    addToPackage(model, qClass.getPackageName(), uClass);
    out.println(String.format("Class '%s' created.", uClass.getQualifiedName()));
    return uClass;
  }

  private Enumeration createEnumWithoutAttributes(final JavaClass qClass, final Model model) {
    final Enumeration uEnum = UML_FACTORY.createEnumeration();
    uEnum.setName(qClass.getName());
    addToPackage(model, qClass.getPackageName(), uEnum);
    out.println(String.format("Enumeration '%s' created.", uEnum.getQualifiedName()));
    return uEnum;
  }

  private Interface createInterface(final JavaClass qClass, final Model model) {
    final Interface uInterface = UML_FACTORY.createInterface();
    uInterface.setName(qClass.getName());
    addToPackage(model, qClass.getPackageName(), uInterface);
    out.println(String.format("Interface '%s' created.", uInterface.getQualifiedName()));
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

  // TODO: remove duplication, remove PackageConverter?
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