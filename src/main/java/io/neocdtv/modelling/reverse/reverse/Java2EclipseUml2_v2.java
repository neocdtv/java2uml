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
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
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
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.lang.System.out;


/**
 * @author xix
 * @since 24.01.19
 */
public class Java2EclipseUml2_v2 {

  private final static Logger LOGGER = Logger.getLogger(Java2EclipseUml2_v2.class.getSimpleName());
  // classifiers contained in visiblePackages will be built completely i.e. with all attributed and associations
  final static UMLFactory UML_FACTORY = UMLFactory.eINSTANCE;
  final static String UML_PACKAGE_PATH_SEPARATOR = "::";
  final static String JAVA_PACKAGE_PATH_SEPARATOR = ".";
  final static String JAVA_PACKAGE_PATH_SEPARATOR_REGEX = "\\" + JAVA_PACKAGE_PATH_SEPARATOR;
  private final Set<String> visiblePackages = new HashSet<>();
  // the rest will be just included in the model to build references from visiblePackages classifiers

  public static Model toUml(final Collection<JavaPackage> qPackages, final String modelName) {
    final Java2EclipseUml2_v2 java2EclipseUml2 = new Java2EclipseUml2_v2();
    return java2EclipseUml2.convert(qPackages, modelName);
  }

  Model convert(final Collection<JavaPackage> qPackages, final String modelName) {
    final Model model = createModel(modelName);
    for (JavaPackage qPackage : qPackages) {
      String packagePath = qPackage.getName();
      visiblePackages.add(packagePath);
      Package uPackage = findPackage(model, packagePath);
      final Collection<JavaClass> qClasses = qPackage.getClasses();
      for (JavaClass qClass : qClasses) {
        if (qClass.isEnum()) {
          final Enumeration uEnum = getOrCreateEnum(qClass);
          uPackage.getOwnedTypes().add(uEnum);
        } else {
          final Class uClass = getOrCreateClass(qClass, model);
          uPackage.getOwnedTypes().add(uClass);
          // TODO: add generalizations
          // TODO: add interface realizations
        }
      }
    }
    return model;
  }

  private Class findClass(JavaClass qClass, Model model) {
    try {
      findPackage(model, qClass.getPackageName());
      String umlClassPath = convertJavaClassPath2UmlClassPath(model.getName(), splitPackagePath(qClass.getPackageName()), qClass.getName());
      // TODO: Exception in thread "main" java.lang.ClassCastException: org.eclipse.uml2.uml.internal.impl.EnumerationImpl cannot be cast to org.eclipse.uml2.uml.Class
      // TODO: add enum support
      Class uClass = Uml2Utils.findElement(umlClassPath, model);
      out.println(String.format("Class '%s' found.", qClass.getFullyQualifiedName()));
      return uClass;
    } catch (NotFoundException notFoundException) {
      out.println(String.format("Class '%s' not found.", qClass.getFullyQualifiedName()));
      return null;
    }
  }

  private Model createModel(final String modelName) {
    final Model model = UML_FACTORY.createModel();
    model.setName(modelName);
    return model;
  }

  private Enumeration getOrCreateEnum(final JavaClass qClass) {
    Enumeration uEnum = UML_FACTORY.createEnumeration();
    uEnum.setName(qClass.getName());
    out.println(String.format("Enumeration '%s' created.", uEnum.getQualifiedName()));
    qClass.getEnumConstants().forEach(enumConstant -> {
      createEnumerationLiteral(uEnum, enumConstant.getName());
    });
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

    if (!qClass.isInterface() && isTypeVisible(qClass)) {
      return createClass(qClass, model);
    } else {
      return createClassWithoutAttributes(qClass, model);
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
    if (!field.isEnumConstant()) { // COMMENT: omit dependency from enum constants to the same enum
      final JavaClass fieldType = field.getType();

      // TODO: handle maps; maps in uml?
      // TODO: handle EEnum
      try {
        if (fieldType.isArray()) {

        } else if (fieldType.isA(Collection.class.getName())) {
          final List<JavaType> actualTypeArguments = ((DefaultJavaParameterizedType) fieldType).getActualTypeArguments();
          final DefaultJavaType genericTypeVariable = (DefaultJavaType) actualTypeArguments.get(0);
        } else {
          final Class referenced = getOrCreateClass(fieldType, model);
          referenced.getPackage().getOwnedTypes().add(referenced);
          createAssociation(uClass, true, AggregationKind.NONE_LITERAL,
              field.getName(), 1, 1,
              referenced, false, AggregationKind.NONE_LITERAL, "", 0, 1);
        }
      } catch (NullPointerException e) {
        LOGGER.severe(e.getMessage());
      }
    }
  }

  /*
  private Class getOrcreateClassOld(final JavaClass qClass, final Model model, final Package uPackage) {
    Class uClass = createClassWithoutAttributes(qClass, model);
    uPackage.getOwnedTypes().add(uClass);
    if (isTypeVisible(qClass)) {
      qClass.getFields().forEach(field -> {
        JavaClass type = field.getType();
        if (isPrimitiveType(type)) {
          PrimitiveType primitiveAttribute = getOrCreatePrimitiveAttribute(field, model);
          buildAttribute(uClass, field.getName(), primitiveAttribute, 1, 1);
        } else {
          Classifier dependentClassifier;
          if (qClass.isEnum()) {
            dependentClassifier = getOrCreateEnum(field.getType());
          } else {
            dependentClassifier = getOrcreateClass(field.getType(), model, uPackage);
          }
          createAssociation(
              uClass,
              false,
              AggregationKind.NONE_LITERAL,
              field.getName(), 0,
              LiteralUnlimitedNatural.UNLIMITED,
              dependentClassifier,
              false,
              AggregationKind.NONE_LITERAL,
              "",
              1, 1);
          // create dependency
        }
      });
    }
    return uClass;
  }
  */

  // handle multiplicity for arrays, lists and sets. Maps?
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
    final Package packageTree = getOrCreatePackageTree(model, packagePath);
    final String umlPath = convertJavaPackagePath2UmlPath(splitPackagePath(packagePath));
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

  String convertJavaClassPath2UmlClassPath(final String modelName, final List<String> packagePath, final String className) {
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