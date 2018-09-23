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

	// example: -r -packages=io.neocdtv.modelling.reverse.domain.customer -sourceDir=C:\Projects\java2uml\src\main\java -output=C:\Reverse\domain.dot
	// TODO: how to render package in dot, can subraphs be configured with shape like nodes and edges
	private static final Logger LOGGER = Logger.getLogger(Main.class.getCanonicalName());

	public static void main(String[] args) throws IOException {

		final JavaProjectBuilder builder = new JavaProjectBuilder();
		final String packagesArg = findCommandArgumentByName(CommandParameterNames.PACKAGES, args);
		final String sourceDir = findCommandArgumentByName(CommandParameterNames.SOURCE_DIR, args);
		final String output = findCommandArgumentByName(CommandParameterNames.OUTPUT, args);

		final boolean recursiveSearch = isCommandArgumentPresent(CommandParameterNames.RECURSIVE_PACKAGE_SEARCH, args);

		// TODO: add the possibility to add just certain java-files, not just whole packages
		// TODO: think about it, how to combine it with -packages
		final String[] packages = packagesArg.split(",");
		for (String aPackage : packages) {
			final String replaceAll = aPackage.replaceAll("\\.", "\\\\");
			final String packageDir = sourceDir + "\\" + replaceAll;
			final File directory = new File(packageDir);
			LOGGER.log(Level.INFO, "adding scanning {0}", directory.getAbsolutePath());
			//final boolean exists = directory.exists();
			if (recursiveSearch) {
				builder.addSourceTree(directory);
			} else {
				Arrays.stream(directory.listFiles()).filter(file -> file.getName().endsWith("java")).forEach(sourceFile -> {
					try {
						builder.addSource(sourceFile); // COMMENT: this way one class can be added
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
		}

		final Model model = ModelBuilder.build(builder.getClasses());

		String rendererName = findCommandArgumentByName(CommandParameterNames.SERIALIZER, args);
		if (rendererName == null) {
			rendererName = SerializerType.DOT.getValue();
		}
		final ModelSerializer modelSerializer = SerializerFactory.buildOrGetByName(SerializerType.valueOfByValue(rendererName));
		final String rendererDiagram = modelSerializer.start(model);
		FileWriter fw = new FileWriter(output);
		fw.write(rendererDiagram);
		fw.flush();
	}

	private static String findCommandArgumentByName(final String argToNameForFind, final String[] args) {
		String argValue = null;
		for (String argToCheck : args) {
			final String[] split = argToCheck.split("=");
			if (split[0].equals(argToNameForFind)) {
				argValue = split[1];
			}
		}
		return argValue;
	}

	private static boolean isCommandArgumentPresent(final String argToNameForFind, final String[] args) {
		for (String argToCheck : args) {
			if (argToCheck.equals(argToNameForFind)) {
				return true;
			}
		}
		return false;
	}
}
