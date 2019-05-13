package io.neocdtv.modelling.reverse.reverse;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaPackage;
import org.batchjob.uml.io.exception.NotFoundException;
import org.batchjob.uml.io.utils.Uml2Utils;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLFactory;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author xix
 * @since 24.01.19
 */
public class Java2EclipseUml2 {

  private final static Logger LOGGER = Logger.getLogger(Java2Uml.class.getSimpleName());
  private final static UMLFactory UML_FACTORY = UMLFactory.eINSTANCE;
  private Set<String> visiblePackages;
  private PackageConverter packageConverter = new PackageConverter();

  public static void toUml(final Collection<JavaPackage> qPackages, final Model model) {
    Java2EclipseUml2 java2EclipseUml2 = new Java2EclipseUml2();
    java2EclipseUml2.build(qPackages, model);
  }

  public void build(final Collection<JavaPackage> qPackages, final Model model) {
    visiblePackages = qPackages.stream().map(javaPackage -> javaPackage.getName()).collect(Collectors.toSet());
    for (JavaPackage qPackage : qPackages) {
      final Collection<JavaClass> qClasses = qPackage.getClasses();
      for (JavaClass qClass : qClasses) {
        if (qClass.isEnum()) {
          Enumeration uEnum = getOrCreateEnumAndAddToPackage(qClass, model);
          // TODO: add generalizations??
          // TODO: add interface realizations??
        } else {
          Class uClass = getOrCreateClassAndAddToPackage(qClass, model);
          // TODO: add generalizations
          // TODO: add interface realizations
        }
      }
    }
  }

  private Enumeration getOrCreateEnumAndAddToPackage(JavaClass qClass, Model model) {
    Enumeration uEnum;
    try {
      List<String> packagePath = packageConverter.splitPackagePath(qClass.getPackageName());
      uEnum = Uml2Utils.findElement(createUmlPathForClassifier(qClass, model, packagePath), model);
      return uEnum;
    } catch (NotFoundException notFoundException) {
      Package parentPackage = packageConverter.findPackage(model, qClass.getPackageName());
      // TODO: is this check needed here
      //if (isTypeVisible(qClass)) {
      uEnum = createEnum(qClass);

      parentPackage.getOwnedTypes().add(uEnum);
      return uEnum;
    }
  }

  private Class getOrCreateClassAndAddToPackage(JavaClass qClass, final Model model) {
    Class uClass;
    try {
      List<String> packagePath = packageConverter.splitPackagePath(qClass.getPackageName());
      uClass = Uml2Utils.findElement(createUmlPathForClassifier(qClass, model, packagePath), model);
      return uClass;
    } catch (NotFoundException notFoundException) {
      Package parentPackage = packageConverter.findPackage(model, qClass.getPackageName());
      if (isTypeVisible(qClass)) {
        uClass = createClass(qClass);
      } else {
        uClass = createClassWithoutAttributes(qClass);
      }
      parentPackage.getOwnedTypes().add(uClass);
      return uClass;
    }
  }

  private String createUmlPathForClassifier(JavaClass javaClass, Model model, List<String> packagePath) {
    return packageConverter.convertJavaPackagePath2UmlPath(model.getName(), packagePath) + "::" + javaClass.getName();
  }

  private Enumeration createEnum(final JavaClass qClass) {
    Enumeration uEnum = UML_FACTORY.createEnumeration();
    uEnum.setName(qClass.getName());
    // TODO: add literals
    return uEnum;
  }

  private Class createClass(final JavaClass qClass) {
    Class classWithoutAttributes = createClassWithoutAttributes(qClass);
    // TODO: add attributes
    return classWithoutAttributes;
  }

  private Class createClassWithoutAttributes(final JavaClass qClass) {
    Class uClass = UML_FACTORY.createClass();
    uClass.setName(qClass.getName());
    return uClass;
  }

  private boolean isTypeVisible(JavaClass type) {
    return visiblePackages.contains(type.getPackageName());
  }
}