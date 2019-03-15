package io.neocdtv.modelling.reverse.reverse;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaPackage;
import org.batchjob.uml.io.exception.NotFoundException;
import org.batchjob.uml.io.utils.Uml2Utils;
import org.eclipse.uml2.uml.Class;
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
          // buildEnum
        } else {
          Class orCreateClass = getOrCreateClass(qClass, model);
          //eClassifier = buildForVisibleAndInvisibleTypes(qClass);
          //buildGeneralizationRelations((EClass) eClassifier, qClass);
        }
      }
    }
  }

  private Class getOrCreateClass(JavaClass javaClass, final Model model) {
    Class uClass;
    try {
      List<String> packagePath = packageConverter.splitPackagePath(javaClass.getPackageName());
      uClass = Uml2Utils.findElement(packageConverter.convertJavaPackagePath2UmlPath(model.getName(), packagePath), model);
      return uClass;
    } catch (NotFoundException notFoundException) {
      Package parentPackage = packageConverter.getOrCreatePackage(model, javaClass.getPackageName());
      if (isTypeVisible(javaClass)) {
        uClass = createClass(javaClass);
      } else {
        uClass = createClassWithoutAttributes(javaClass);
      }
      parentPackage.getOwnedTypes().add(uClass);
      return uClass;
    }
  }

  private Class createClass(final JavaClass qClass) {
    Class classWithoutAttributes = createClassWithoutAttributes(qClass);
    // TODO: add attributes
    return classWithoutAttributes;
  }

  private Class createClassWithoutAttributes(final JavaClass qClass) {
    Class aClass = UML_FACTORY.createClass();
    aClass.setName(qClass.getName());
    return aClass;
  }

  private boolean isTypeVisible(JavaClass type) {
    return visiblePackages.contains(type.getPackageName());
  }
}