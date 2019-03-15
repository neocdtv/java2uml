package io.neocdtv.modelling.reverse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaPackage;
import io.neocdtv.modelling.reverse.reverse.Java2Ecore;
import io.neocdtv.modelling.reverse.reverse.Java2Uml;
import io.neocdtv.modelling.reverse.serialization.Ecore2Dot;
import io.neocdtv.modelling.reverse.serialization.Uml2Dot;
import org.eclipse.emf.ecore.EPackage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author xix
 */
public class Main {

  private static final Logger LOGGER = Logger.getLogger(Main.class.getCanonicalName());

  private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static void main(String[] args) throws IOException {

    final String packageInputConfigsPath = CliUtil.findCommandArgumentByName(CommandParameterNames.PACKAGE_INPUT_CONFIGS, args);
    final String outputDir = CliUtil.findCommandArgumentByName(CommandParameterNames.OUTPUT_DIR, args);
    if (packageInputConfigsPath == null || outputDir == null) {
      printUsageAndExit();
    }

    String content = new String(Files.readAllBytes(Paths.get(packageInputConfigsPath)), "UTF-8");
    List<PackageInputConfig> packageInputConfigs = OBJECT_MAPPER.readValue(content, new TypeReference<List<PackageInputConfig>>() {});

    String outputRelativePath = "output";
    String outputDirectory = outputDir + File.separator + outputRelativePath;
    File file = new File(outputDirectory);
    file.mkdir();

    OBJECT_MAPPER.writeValue(new File(outputDirectory + File.separator + "packages.json") ,"");
  }

  private static String buildOutputFileName(String outputDir, PackageInputConfig packageInputConfig) {
    return outputDir + File.separator + packageInputConfig.getPackageName();
  }

  private static void printUsageAndExit() {
    System.out.println("usage: java -jar target/java2uml.jar -packageInputConfigs=... -sourceDir=... -outputDir=... [-uml] ");
    System.out.println("options:");
    //System.out.println("\t-r\trecursive package scanning");
    System.out.println("\t-uml\tuse Eclipse Uml2 internally instead of Eclipse Ecore (alpha)");
    System.out.println("example: java -jar target/java2uml.jar -packageInputConfigs=\"packageInputConfigs.json\" -outputDir=.");
    System.exit(1);
  }

  private static void generateUsingUmlModel(String outputFile, java.util.Collection<JavaPackage> packages, java.util.Collection<JavaClass> qClasses) throws java.io.IOException {
    final Set<org.eclipse.uml2.uml.Package> uPackages = Java2Uml.toUml(packages);
    serializeUml(outputFile, uPackages, qClasses);
  }

  private static void generateUsingECoreModel(String outputFile, java.util.Collection<JavaPackage> qPackages) throws java.io.IOException {
    final Set<EPackage> ePackages = Java2Ecore.toEcore(qPackages);
    serializeECore(outputFile, ePackages, qPackages);
  }

  private static JavaProjectBuilder configureSourceFilesForAnalysis(String argumentPackages, String argumentSourceDir, boolean recursiveSearch) {
    // TODO: add the possibility to add just certain java-files, not just whole packages;think about it, how to combine it with -packages
    final JavaProjectBuilder builder = new JavaProjectBuilder();
    final String[] packages = argumentPackages.split(",");
    for (String aPackage : packages) {
      String replaceSeparator;
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

  private static void serializeUml(final String outputFile, final Set<org.eclipse.uml2.uml.Package> uPackages, final Collection<JavaClass> qClasses) throws IOException {
    final String rendererDiagram = Uml2Dot.toDot(uPackages, new HashSet<>(qClasses));
    FileWriter fw = new FileWriter(outputFile);
    fw.write(rendererDiagram);
    fw.flush();
  }

  private static void serializeECore(final String outputFilel, final Set<EPackage> ePackages, final Collection<JavaPackage> qPackages) throws IOException {
    final String dot = Ecore2Dot.toDot(ePackages, qPackages);
    FileWriter fw = new FileWriter(outputFilel);
    fw.write(dot);
    fw.flush();
  }
}