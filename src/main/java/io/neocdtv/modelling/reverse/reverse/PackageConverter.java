package io.neocdtv.modelling.reverse.reverse;

import com.thoughtworks.qdox.model.JavaPackage;
import org.batchjob.uml.io.exception.NotFoundException;
import org.batchjob.uml.io.utils.Uml2Utils;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Transform package paths [com.example, com.example.one, com.example.two] to a
 * package tree:
 * com ->
 * example ->
 * example -> one
 * example -> two
 * <p>
 * Package paths are extracted from QDox-Packages (qPackages)
 * Package tree is constructed as as Eclipse UML2 Model (Model.uPackages)
 *
 * @author xix
 * @since 29.01.19
 */
public class PackageConverter {

  final static Logger LOGGER = Logger.getLogger(PackageConverter.class.getName());
  final static UMLFactory UML_FACTORY = UMLFactory.eINSTANCE;
  final static String UML_PACKAGE_PATH_SEPARATOR = "::";
  final static String JAVA_PACKAGE_PATH_SEPARATOR = ".";
  final static String JAVA_PACKAGE_PATH_SEPARATOR_REGEX = "\\" + JAVA_PACKAGE_PATH_SEPARATOR;
  final static String MODEL_NAME = "model";

  public static Model transform(final Collection<JavaPackage> qPackages) {
    final PackageConverter packageConverter = new PackageConverter();
    return packageConverter.start(
        qPackages.stream()
            .map(javaPackage -> javaPackage.getName()).collect(Collectors.toList()));
  }

  Model start(Collection<String> packagePaths) {
    final Model model = UML_FACTORY.createModel();
    model.setName(MODEL_NAME);
    packagePaths.forEach(packagePath -> {
      getOrCreatePackage(model, packagePath);
    });
    return model;
  }

  Package getOrCreatePackage(final Model model, final String packagePath) {
    return getOrCreatePackage(model, model, packagePath, 0);
  }

  Package getOrCreatePackage(final Model model, final Package parentPackage, final String packagePath, int packagePathThreshold) {
    Package packageToGetOrCreate;
    List<String> packagePathToGetOrCreate = getPackagePathToThreshold(packagePath, packagePathThreshold);

    try {
      String modelName = model.getName();
      String umlPath = convertJavaPackagePath2UmlPath(modelName, packagePathToGetOrCreate);
      packageToGetOrCreate = Uml2Utils.findElement(umlPath, model);
    } catch (NotFoundException notFoundException) {
      packageToGetOrCreate = UML_FACTORY.createPackage();
      int lastPathIndex = packagePathToGetOrCreate.size() - 1;
      packageToGetOrCreate.setName(packagePathToGetOrCreate.get(lastPathIndex));
      parentPackage.getNestedPackages().add(packageToGetOrCreate);
    }
    if (hasSubPackages(packagePath, packagePathToGetOrCreate)) {
      getOrCreatePackage(model, packageToGetOrCreate, packagePath, ++packagePathThreshold);
    }
    return packageToGetOrCreate;
  }

  List<String> getPackagePathToThreshold(final String packagePath, final int packagePathThreshold) {
    List<String> packagePathSplit = splitPackagePath(packagePath);

    List<String> packagePathToThreshold = new ArrayList<>();
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
    String[] packagePathParts = packagePath.split(JAVA_PACKAGE_PATH_SEPARATOR_REGEX);
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
    List<String> packagePathWithModelName = new ArrayList<>();
    packagePathWithModelName.add(modelName);
    packagePathWithModelName.addAll(packagePath);
    return packagePathWithModelName
        .stream()
        .collect(Collectors.joining(UML_PACKAGE_PATH_SEPARATOR));
  }

  private boolean hasSubPackages(String packagePath, List<String> packagePathToGetOrCreate) {
    return splitPackagePath(packagePath).size() > packagePathToGetOrCreate.size();
  }
}
