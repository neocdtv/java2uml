/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.java2uml.reverse;

import io.neocdtv.java2uml.renderer.RendererFactory;
import io.neocdtv.java2uml.renderer.RendererType;
import io.neocdtv.java2uml.renderer.Renderer;
import io.neocdtv.java2uml.model.Model;
import com.thoughtworks.qdox.JavaProjectBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author xix
 */
public class Main {

	// example: -packages=com.mycompany.java2uml.domain -sourceDir=C:\Projects\Modelling\java2uml\src\main\java -output=C:\Users\wolfkr\Desktop\Reverse\example.dot
	// example: -packages=com.bmw.ofco.business.businesspartner.entity -sourceDir=C:\Projects\USPPlus\Source\trunk\ofco\ofco-application\src\main\java -output=C:\Users\wolfkr\Desktop\Reverse\bp.dot
	// TODO: how to render package in dot, can subraphs be configured with shape like nodes and edges
	private static final Logger LOGGER = Logger.getLogger(Main.class.getCanonicalName());

	public static void main(String[] args) throws IOException {

		final JavaProjectBuilder builder = new JavaProjectBuilder();
		final String packagesArg = findCommandArgumentByName(CommandParemeterNames.PACKAGES, args);
		final String sourceDir = findCommandArgumentByName(CommandParemeterNames.SOURCE_DIR, args);
		final String output = findCommandArgumentByName(CommandParemeterNames.OUTPUT, args);
		final String[] packages = packagesArg.split(",");
		for (String aPackage : packages) {
			final String replaceAll = aPackage.replaceAll("\\.", "\\\\");
			final String packageDir = sourceDir + "\\" + replaceAll;
			final File file = new File(packageDir);
			final boolean exists = file.exists();
			//builder.addSourceFolder(file);
			builder.addSourceTree(file); // TODO: change to non-recursive by default and enable with -r flag
			//builder.addSource(new File(sourceDir + "\\com\\bmw\\ofco\\business\\offer\\entity\\Offer.java"));
			LOGGER.log(Level.INFO, "adding scanning {0}", file.getAbsolutePath());
		}

		final Model model = ModelBuilder.build(builder.getClasses());

		String rendererName = findCommandArgumentByName(CommandParemeterNames.RENDERER, args);
		if (rendererName == null) {
			rendererName = RendererType.DOT.getValue();
		}
		final Renderer renderer = RendererFactory.buildOrGetByName(RendererType.valueOfByValue(rendererName));
		final String rendererDiagram = renderer.renderer(model);
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
}
