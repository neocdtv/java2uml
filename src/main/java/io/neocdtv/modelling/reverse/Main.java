package io.neocdtv.modelling.reverse;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaPackage;
import io.neocdtv.modelling.reverse.reverse.ECoreModelBuilder;
import io.neocdtv.modelling.reverse.reverse.UmlModelBuilder;
import io.neocdtv.modelling.reverse.serialization.DotECoreModelSerializer;
import io.neocdtv.modelling.reverse.serialization.DotUmlModelSerializer;
import org.eclipse.emf.ecore.EPackage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author xix
 */
public class Main {

  private static final Logger LOGGER = Logger.getLogger(Main.class.getCanonicalName());

  public static void main(String[] args) throws IOException {

    // TODO: collect multiple packages
    final String packages = CliUtil.findCommandArgumentByName(CommandParameterNames.PACKAGES, args);
    // TODO: multiple sourceDirs?
    final String sourceDir = CliUtil.findCommandArgumentByName(CommandParameterNames.SOURCE_DIR, args);
    // TODO: change file to dir, to write multiple packages
    // TODO: how to determine name of the files, it can't be the package, cos you can have multiple packages separated by a comma.
    final String outputFile = CliUtil.findCommandArgumentByName(CommandParameterNames.OUTPUT_FILE, args);

    if (packages == null || sourceDir == null || outputFile == null) {
      System.out.println("usage: java -jar target/java2uml.jar -packages=... -sourceDir=... -outputFile=... [-r] [-uml] ");
      System.out.println("options:");
      System.out.println("\t-r\trecursive package scanning");
      System.out.println("\t-uml\tuse Eclipse Uml2 internally instead of Eclipse Ecore(alpha)");
      System.out.println("example: java -jar target/java2uml.jar -packages=io.neocdtv.modelling.reverse.domain -sourceDir=src/main/java -outputFile=output.dot -r");
      System.exit(1);
    }

    final boolean recursivePackagesEnabled = CliUtil.isCommandArgumentPresent(CommandParameterNames.RECURSIVE_PACKAGE_SEARCH, args);
    final JavaProjectBuilder builder = configureSourceFilesForAnalysis(packages, sourceDir, recursivePackagesEnabled);

    if (CliUtil.isCommandArgumentPresent(CommandParameterNames.USE_ECLIPSE_UML, args)) {
      generateUsingUmlModel(outputFile, builder.getPackages(), builder.getClasses());
    } else {
      generateUsingECoreModel(outputFile, builder.getPackages());
    }
  }

  private static void generateUsingUmlModel(String outputFile, java.util.Collection<JavaPackage> packages, java.util.Collection<JavaClass> qClasses) throws java.io.IOException {
    final Set<org.eclipse.uml2.uml.Package> uPackages = UmlModelBuilder.build(packages);
    serializeUml(outputFile, uPackages, qClasses);
  }

  private static void generateUsingECoreModel(String outputFile, java.util.Collection<JavaPackage> qPackages) throws java.io.IOException {
    final Set<EPackage> ePackages = ECoreModelBuilder.build(qPackages);
    serializeECore(outputFile, ePackages, qPackages);
  }

  private static JavaProjectBuilder configureSourceFilesForAnalysis(String argumentPackages, String argumentSourceDir, boolean recursiveSearch) {
    // TODO: add the possibility to add just certain java-files, not just whole packages;think about it, how to combine it with -packages
    final JavaProjectBuilder builder = new JavaProjectBuilder();
    final String[] packages = argumentPackages.split(",");
    for (String aPackage : packages) {
      String replaceSeparator;
      // TODO: OS specific aPackage.replaceAll, add support for unix-like OS
      final String replaceAll = aPackage.replaceAll("\\.", File.separator + File.separator);
      final String packageDir = argumentSourceDir + File.separator + replaceAll;
      final File directory = new File(packageDir);
      if (recursiveSearch) {
        LOGGER.log(Level.INFO, "recursive package scanning {0}", directory.getAbsolutePath());
        builder.addSourceTree(directory);
      } else {
        LOGGER.log(Level.INFO, "non-recursive package scanning {0}", directory.getAbsolutePath());
        Arrays.stream(directory.listFiles()).filter(file -> file.getName().endsWith("java")).forEach(sourceFile -> {
          try {
            builder.addSource(sourceFile); // COMMENT: this way one class can be added
          } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
          }
        });
      }
    }
    return builder;
  }

  private static void serializeUml(final String argumentOutputFile, final Set<org.eclipse.uml2.uml.Package> uPackages, final Collection<JavaClass> qClasses) throws IOException {
    final DotUmlModelSerializer dotUmlModelSerializer = new DotUmlModelSerializer();
    final String rendererDiagram = dotUmlModelSerializer.start(uPackages, new HashSet<>(qClasses));
    FileWriter fw = new FileWriter(argumentOutputFile);
    fw.write(rendererDiagram);
    fw.flush();
  }

  private static void serializeECore(final String argumentOutputFile, final Set<EPackage> ePackages, final Collection<JavaPackage> qPackages) throws IOException {
    DotECoreModelSerializer dotECoreModelSerializer = new DotECoreModelSerializer();
    final String rendererDiagram = dotECoreModelSerializer.start(ePackages, qPackages);
    FileWriter fw = new FileWriter(argumentOutputFile);
    fw.write(rendererDiagram);
    fw.flush();
  }
}
