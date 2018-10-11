package io.neocdtv.modelling.reverse;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import io.neocdtv.modelling.reverse.model.Classifier;
import io.neocdtv.modelling.reverse.model.Model;
import io.neocdtv.modelling.reverse.model.Package;
import io.neocdtv.modelling.reverse.model.Relation;
import io.neocdtv.modelling.reverse.reverse.ModelBuilder;
import io.neocdtv.modelling.reverse.serialization.ModelSerializer;
import io.neocdtv.modelling.reverse.serialization.SerializerFactory;
import io.neocdtv.modelling.reverse.serialization.SerializerType;

import javax.persistence.Entity;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
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

		final String packages = CliUtil.findCommandArgumentByName(CommandParameterNames.PACKAGES, args);
		final String sourceDir = CliUtil.findCommandArgumentByName(CommandParameterNames.SOURCE_DIR, args);
		final String outputFile = CliUtil.findCommandArgumentByName(CommandParameterNames.OUTPUT_FILE, args);

		if (packages == null || sourceDir == null || outputFile == null) {
			System.out.println("usage: java -jar target/java2uml.jar -packages=... -sourceDir=... -outputFile=... [-r]");
			System.exit(1);
		}

		final boolean recursivePackagesEnabled = CliUtil.isCommandArgumentPresent(CommandParameterNames.RECURSIVE_PACKAGE_SEARCH, args);
		final JavaProjectBuilder builder = configureSourceFilesForAnalysis(packages, sourceDir, recursivePackagesEnabled);

		Collection<JavaClass> classes = builder.getClasses();

		//classes = filter(classes);

		Model model = ModelBuilder.build(classes);

		// remove class w/o relation of type dependency to and from
		filterOutClassifiersWithOutDependenciesToAndFrom(model);
		serialize(args, outputFile, model);
	}

	private static void filterOutClassifiersWithOutDependenciesToAndFrom(Model model) {
		for (Package aPackage : model.getPackages()) {
			final Iterator<Classifier> iterator = aPackage.getClassifiers().iterator();
			while (iterator.hasNext()) {
				final Classifier classifier = iterator.next();
				final String XXX = classifier.getId();
				if (classifier.getRelations().isEmpty() || hasOnlyRelationsToNotIncludedPackages(classifier, model.getPackages())) {
					boolean shouldBeRemoved = true;
					for (Package aPackage1 : model.getPackages()) {
						for (Classifier classifier1 : aPackage1.getClassifiers()) {
							for (Relation relation : classifier1.getRelations()) {
								if (relation.getToNode().getId().equals(XXX)) {
									shouldBeRemoved = false;
								}
							}
						}
					}
					if (shouldBeRemoved) {
						LOGGER.info("REMOVING " + XXX);
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

	private static Collection<JavaClass> filter(Collection<JavaClass> classes) {
		classes = classes.
				stream().
				filter(javaClass -> shouldClassifierBeProcessed(javaClass)).
				collect(Collectors.toList());
		return classes;
	}

	private static boolean shouldClassifierBeProcessed(final JavaClass javaClass) {
		return isAnnotatedWith(javaClass.getAnnotations(), Entity.class)
				|| javaClass.isInterface()
				|| javaClass.isEnum()
				|| javaClass.isAbstract();
	}

	private static boolean isAnnotatedWith(final List<JavaAnnotation> annotations, final Class<?> type) {
		return !annotations.
				stream().
				filter(annotation -> annotation.getType().getFullyQualifiedName().equals(type.getCanonicalName())).
				collect(Collectors.toList()).isEmpty();
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
