/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.java2uml.reverse;

import com.mycompany.java2uml.renderer.RendererFactory;
import com.mycompany.java2uml.renderer.RendererType;
import com.mycompany.java2uml.renderer.Renderer;
import com.mycompany.java2uml.diagram.Model;
import com.thoughtworks.qdox.JavaProjectBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ofco
 */
public class Main {

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
