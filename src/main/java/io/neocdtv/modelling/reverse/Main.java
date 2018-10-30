package io.neocdtv.modelling.reverse;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaPackage;
import io.neocdtv.modelling.reverse.model.custom.Classifier;
import io.neocdtv.modelling.reverse.model.custom.Model;
import io.neocdtv.modelling.reverse.model.custom.Package;
import io.neocdtv.modelling.reverse.model.custom.Relation;
import io.neocdtv.modelling.reverse.reverse.ModelBuilder;
import io.neocdtv.modelling.reverse.reverse.ECoreModelBuilder;
import io.neocdtv.modelling.reverse.serialization.DotCustomModelSerializer;
import io.neocdtv.modelling.reverse.serialization.DotECoreModelSerializer;
import io.neocdtv.modelling.reverse.serialization.ModelSerializer;
import org.eclipse.emf.ecore.EPackage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
			System.out.println("usage: java -jar target/java2uml.jar -packages=... -sourceDir=... -outputFile=... [-r]");
			System.exit(1);
		}

		final boolean recursivePackagesEnabled = CliUtil.isCommandArgumentPresent(CommandParameterNames.RECURSIVE_PACKAGE_SEARCH, args);
		final JavaProjectBuilder builder = configureSourceFilesForAnalysis(packages, sourceDir, recursivePackagesEnabled);

		if(CliUtil.isCommandArgumentPresent(CommandParameterNames.USE_ECORE, args)) {
			generateUsingECoreModel(outputFile, builder.getPackages(), builder.getClasses());
		} else {
			generateUsingCustomModel(outputFile, builder.getClasses());
		}
	}

	private static void generateUsingECoreModel(final String outputFile, final Collection<JavaPackage> qPackages ,final Collection<JavaClass> qClasses) throws IOException {
		final Set<EPackage> ePackages = ECoreModelBuilder.build(qPackages);
		serialize(outputFile, ePackages, qClasses);
	}

	private static void generateUsingCustomModel(final String outputFile, final Collection<JavaClass> classes) throws IOException {
		Model model = ModelBuilder.build(classes);

		// remove class w/o relation of type dependency to and from
		removeClassifiersWithOutDependenciesToAndFromIncludedPackages(model);
		serialize(outputFile, model);
	}

	// COMMENT: this is an ugly one method
	// TODO: This method removes lonely classfiers, but this should be changes to render clusters of connected classes
	private static void removeClassifiersWithOutDependenciesToAndFromIncludedPackages(Model model) {
		for (Package aPackage : model.getPackages()) {
			final Iterator<Classifier> iterator = aPackage.getClassifiers().iterator();
			while (iterator.hasNext()) {
				final Classifier classifierToCheck = iterator.next();
				final String classfierId = classifierToCheck.getId();
				if (classifierToCheck.getRelations().isEmpty() || hasOnlyRelationsToNotIncludedPackages(classifierToCheck, model.getPackages())) {
					boolean shouldBeRemoved = true;
					for (Package aPackage1 : model.getPackages()) {
						for (Classifier classifier : aPackage1.getClassifiers()) {
							for (Relation relation : classifier.getRelations()) {
								if (relation.getToNode().getId().equals(classfierId)) {
									shouldBeRemoved = false;
								}
							}
						}
					}
					if (shouldBeRemoved) {
						LOGGER.info("REMOVING " + classfierId);
						iterator.remove();
					}
				}
			}
		}
	}


	private static boolean hasOnlyRelationsToNotIncludedPackages(Classifier classifier, Set<Package> packages) {
		boolean hasOnlyRelationsToNotIncludedPackages = true;
		final Set<String> includedPackages = packages.stream().map(aPackage -> aPackage.getName()).collect(Collectors.toSet());

		for (Relation relation : classifier.getRelations()) {
			if (includedPackages.contains(relation.getToNode().getPackageName())) {
				hasOnlyRelationsToNotIncludedPackages = false;
			}
		}
		return hasOnlyRelationsToNotIncludedPackages;
	}

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

	private static void serialize(final String argumentOutputFile, final Set<EPackage> ePackages, final Collection<JavaClass> qClasses) throws IOException {
		final DotECoreModelSerializer modelSerializer = new DotECoreModelSerializer();
		final String rendererDiagram = modelSerializer.start(ePackages, new HashSet<>(qClasses));
		FileWriter fw = new FileWriter(argumentOutputFile);
		fw.write(rendererDiagram);
		fw.flush();
	}

	private static void serialize(String argumentOutputFile, Model model) throws IOException {
		final ModelSerializer modelSerializer = new DotCustomModelSerializer();
		final String rendererDiagram = modelSerializer.start(model);
		FileWriter fw = new FileWriter(argumentOutputFile);
		fw.write(rendererDiagram);
		fw.flush();
	}
}
