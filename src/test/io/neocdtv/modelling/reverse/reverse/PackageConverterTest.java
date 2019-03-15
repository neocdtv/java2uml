package io.neocdtv.modelling.reverse.reverse;

import org.batchjob.uml.io.utils.Uml2Utils;
import org.eclipse.uml2.uml.Model;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.neocdtv.modelling.reverse.reverse.PackageConverter.JAVA_PACKAGE_PATH_SEPARATOR;
import static io.neocdtv.modelling.reverse.reverse.PackageConverter.JAVA_PACKAGE_PATH_SEPARATOR_REGEX;
import static io.neocdtv.modelling.reverse.reverse.PackageConverter.MODEL_NAME;
import static io.neocdtv.modelling.reverse.reverse.PackageConverter.UML_PACKAGE_PATH_SEPARATOR;
import static org.junit.Assert.assertEquals;

/**
 * @author xix
 * @since 14.02.19
 */
public class PackageConverterTest {

  private static final String LEVEL_ONE = "io";
  private static final String LEVEL_TWO = "neocdtv";
  private static final String LEVEL_THREE = "modelling";
  private static final String ONE_PATH_PACKAGE = LEVEL_ONE;
  private static final String TWO_PATH_PACKAGE = ONE_PATH_PACKAGE + JAVA_PACKAGE_PATH_SEPARATOR + LEVEL_TWO;
  private static final String THREE_PATH_PACKAGE = TWO_PATH_PACKAGE + JAVA_PACKAGE_PATH_SEPARATOR + LEVEL_THREE;

  private static final String ONE_PATH_PACKAGE_UML = PackageConverter.LOGGER + UML_PACKAGE_PATH_SEPARATOR + LEVEL_ONE;
  private static final String TWO_PATH_PACKAGE_UML = MODEL_NAME + UML_PACKAGE_PATH_SEPARATOR + ONE_PATH_PACKAGE + UML_PACKAGE_PATH_SEPARATOR + LEVEL_TWO;

  private static final String THREE_PATH_PACKAGE_UML = TWO_PATH_PACKAGE_UML + UML_PACKAGE_PATH_SEPARATOR + LEVEL_THREE;

  private PackageConverter packageConverter = new PackageConverter();

  @Test
  public void transform() {
    // given
    final List<String> packagePaths = Arrays.asList(TWO_PATH_PACKAGE, ONE_PATH_PACKAGE, THREE_PATH_PACKAGE);
    // when
    Model model = packageConverter.start(packagePaths);
    // then
    packagePaths.forEach(packagePath -> {
      String qualifiedName = packageConverter.convertJavaPackagePath2UmlPath(MODEL_NAME, packageConverter.splitPackagePath(packagePath));
      Uml2Utils.findElement(qualifiedName, model);
    });
  }

  @Test
  public void convertJavaPackagePath2UmlQualifiedName() {
    String qualifiedName = packageConverter.convertJavaPackagePath2UmlPath(MODEL_NAME, Arrays.asList(THREE_PATH_PACKAGE.split(JAVA_PACKAGE_PATH_SEPARATOR_REGEX)));
    assertEquals(THREE_PATH_PACKAGE_UML, qualifiedName);
  }

  @Test
  public void getPackagePathToThreshold() {
    List<String> packagePathToThreshold = packageConverter.getPackagePathToThreshold(THREE_PATH_PACKAGE, 1);
    assertEquals(packagePathToThreshold.stream().collect(Collectors.joining(JAVA_PACKAGE_PATH_SEPARATOR)), TWO_PATH_PACKAGE);
  }

  @Test
  public void getParentPackageName_one_path_package() {
    String actual = packageConverter.getParentPackageName(ONE_PATH_PACKAGE);
    assertEquals(ONE_PATH_PACKAGE, actual);
  }

  @Test
  public void getParentPackageName_two_path_package() {
    String actual = packageConverter.getParentPackageName(TWO_PATH_PACKAGE);
    assertEquals(ONE_PATH_PACKAGE, actual);
  }

  @Test
  public void getChildPackagePath_no_path() {
    String actual = packageConverter.getChildPackagePath(ONE_PATH_PACKAGE);
    assertEquals(null, actual);
  }

  @Test
  public void getChildPackagePath_one_path_child() {
    String actual = packageConverter.getChildPackagePath(TWO_PATH_PACKAGE);
    assertEquals(LEVEL_TWO, actual);
  }

  @Test
  public void getChildPackagePath_two_path_child() {
    String actual = packageConverter.getChildPackagePath(THREE_PATH_PACKAGE);
    assertEquals(LEVEL_TWO + JAVA_PACKAGE_PATH_SEPARATOR + LEVEL_THREE, actual);
  }
}