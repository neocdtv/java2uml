package io.neocdtv.modelling.reverse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaPackage;
import io.neocdtv.modelling.reverse.reverse.Java2EclipseUml2_v2;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.uml2.examples.gettingstarted.ModelSerializer;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.util.UMLUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author xix
 */
public class Main {

  private static final Logger LOGGER = Logger.getLogger(Main.class.getCanonicalName());
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static void main(String[] args) throws IOException {

    final String packageInputConfigsPath = CliUtil.findCommandArgumentByName(CommandParameterNames.PACKAGE_INPUT_CONFIGS, args);
    final String outputDir = CliUtil.findCommandArgumentByName(CommandParameterNames.OUTPUT_DIR, args);
    OutputFormat outputFormat = null;
    try {
      outputFormat = OutputFormat.valueOf(CliUtil.findCommandArgumentByName(CommandParameterNames.OUTPUT_FORMAT, args));
    } catch (Exception e) {
      LOGGER.severe(e.getMessage());
    }
    if (packageInputConfigsPath == null || outputDir == null || outputFormat == null) {
      printUsageAndExit();
    }

    final String content = new String(Files.readAllBytes(Paths.get(packageInputConfigsPath)), "UTF-8");
    List<PackageInputConfig> packageInputConfigs = OBJECT_MAPPER.readValue(content, new TypeReference<List<PackageInputConfig>>() {
    });
    final JavaProjectBuilder javaProjectBuilder = configurePackagesForAnalysis(packageInputConfigs);

    final String outputDirectory = outputDir + File.separator;
    final File file = new File(outputDirectory);
    file.mkdir();

    final Collection<JavaPackage> qPackages = javaProjectBuilder.getPackages();
    final Model model = Java2EclipseUml2_v2.toUml(qPackages, "simpleModel");
    final Collection<EPackage> ePackages = UMLUtil.convertToEcore(model.getNearestPackage(), new HashMap<>());

    switch (outputFormat) {
      case UML:
        ModelSerializer.serializeUml(model, buildOutputFormat(outputDirectory, "uml"));
        break;
      case ECORE:
        ModelSerializer.serializeEcore(ePackages, buildOutputFormat(outputDirectory, "ecore"));
        break;
      case ECORE_JSON:
        ModelSerializer.serializeEcoreJson(ePackages, buildOutputFormat(outputDirectory, "json"));
        break;
    }
  }

  private static String buildOutputFormat(final String outputDirectory, final String ending) {
    final String filePath = outputDirectory + "model-main." + ending;
    LOGGER.info("Writing file: " + filePath);
    return filePath;
  }

  private static void printUsageAndExit() {
    System.out.println("usage: java -jar target/java2uml.jar -packageInputConfigs=... -outputDir=... -outputFormat=UML|ECORE|ECORE_JSON");
    System.out.println("options:");
    //System.out.println("\t-r\trecursive package scanning");
    System.out.println("\tUML\t\toutput eclipse uml");
    System.out.println("\tECORE\t\toutput eclipse ecore");
    System.out.println("\tECORE_JSON\toutput eclipse ecore in json format");
    System.out.println("example: java -jar target/java2uml.jar -packageInputConfigs=\"packageInputConfigs.json\" -outputDir=. -outputFormat=UML");
    System.exit(1);
  }

  private static JavaProjectBuilder configurePackagesForAnalysis(List<PackageInputConfig> packageInputConfigs) {
    // TODO: add the possibility to add just certain java-files, not just whole packages;think about it, how to combine it with -packages
    final JavaProjectBuilder builder = new JavaProjectBuilder();

    packageInputConfigs.forEach(packageInputConfig -> {
      final String relativePackageDir = packageInputConfig.getPackageName().replaceAll("\\.", File.separator + File.separator);
      final String absolutePackageDir = packageInputConfig.getSourceDir() + File.separator + relativePackageDir;
      final File packageDir = new File(absolutePackageDir);
      LOGGER.log(Level.INFO, "recursive package scanning {0}", packageDir.getAbsolutePath());
      builder.addSourceTree(packageDir);
    });
    return builder;
  }


  @Deprecated
  private static JavaProjectBuilder configureSourceFilesForAnalysis(String argumentPackages, String argumentSourceDir, boolean recursiveSearch) {
    // TODO: add the possibility to add just certain java-files, not just whole packages;think about it, how to combine it with -packages
    final JavaProjectBuilder builder = new JavaProjectBuilder();
    final String[] packages = argumentPackages.split(",");
    for (String aPackage : packages) {
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
}