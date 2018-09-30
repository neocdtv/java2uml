package io.neocdtv.modelling.reverse;

import io.neocdtv.modelling.reverse.reverse.ModelBuilder;
import io.neocdtv.modelling.reverse.serialization.SerializerFactory;
import io.neocdtv.modelling.reverse.serialization.SerializerType;
import io.neocdtv.modelling.reverse.serialization.ModelSerializer;
import io.neocdtv.modelling.reverse.model.Model;
import com.thoughtworks.qdox.JavaProjectBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author xix
 */
public class Main {

  private static final Logger LOGGER = Logger.getLogger(Main.class.getCanonicalName());

  public static void main(String[] args) throws IOException {

    final String packages = CliUtil.findCommandArgumentByName(CommandParameterNames.PACKAGES, args);
    final String sourceDir = CliUtil.findCommandArgumentByName(CommandParameterNames.SOURCE_DIR, args);
    final String outputFile = CliUtil.findCommandArgumentByName(CommandParameterNames.OUTPUT_FILE, args);

    if (packages == null || sourceDir == null || outputFile == null) {
      System.out.println("usage: java -jar target/java2uml.jar -packages=... -sourceDir=... -outputFile=... [-r]");
      System.exit(1);
    }

    final boolean recursivePackagesEnabled = CliUtil.isCommandArgumentPresent(CommandParameterNames.RECURSIVE_PACKAGE_SEARCH, args);
    final JavaProjectBuilder builder = configureSourceFilesForAnalysis(packages, sourceDir, recursivePackagesEnabled);

    final Model model = ModelBuilder.build(builder.getClasses());

    serialize(args, outputFile, model);
  }

  private static JavaProjectBuilder configureSourceFilesForAnalysis(String argumentPackages, String argumentSourceDir, boolean recursiveSearch) {
    // TODO: add the possibility to add just certain java-files, not just whole packages;think about it, how to combine it with -packages
    final JavaProjectBuilder builder = new JavaProjectBuilder();
    final String[] packages = argumentPackages.split(",");
    for (String aPackage : packages) {
      final String replaceAll = aPackage.replaceAll("\\.", File.separator);
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

  private static void serialize(String[] args, String argumentOutputFile, Model model) throws IOException {
    String rendererName = CliUtil.findCommandArgumentByName(CommandParameterNames.SERIALIZER, args);
    if (rendererName == null) {
      LOGGER.info("defaulting to dot serializer");
      rendererName = SerializerType.DOT.getValue();
    }
    final ModelSerializer modelSerializer = SerializerFactory.buildOrGetByName(SerializerType.valueOfByValue(rendererName));
    final String rendererDiagram = modelSerializer.start(model);
    FileWriter fw = new FileWriter(argumentOutputFile);
    fw.write(rendererDiagram);
    fw.flush();
  }
}
