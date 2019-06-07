package io.neocdtv.modelling.reverse.serialization;

import org.batchjob.uml.io.utils.Uml2Utils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.uml2.uml.Model;
import org.emfjson.jackson.resource.JsonResourceFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * @author xix
 * @since 27.01.19
 */
public class ModelSerializer {

  public static void serializeEcoreJson(final Collection<EPackage> ePackages, String path) {
    ResourceSet resourceSet = new ResourceSetImpl();
    resourceSet.getResourceFactoryRegistry()
        .getExtensionToFactoryMap()
        .put("json", new JsonResourceFactory());
    Resource resource = resourceSet.createResource(URI
        .createURI(path));

    // Add ePackages to contents list of the resource
    resource.getContents().addAll(ePackages);

    try {
      // Save the resource
      resource.save(null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void serializeUml(Model model, String path) {
    Uml2Utils.write(model, new File(path));
  }

  public static void serializeEcore(final Collection<EPackage> ePackages, String path) {

    ResourceSet resourceSet = new ResourceSetImpl();
    // Register XML Factory implementation to handle .ecore files
    resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
        .put("ecore", new XMLResourceFactoryImpl());
    // Create empty resource with the given URI
    Resource resource = resourceSet.createResource(URI
        .createURI(path));
    // Add ePackages to contents list of the resource
    resource.getContents().addAll(ePackages);

    try {
      // Save the resource
      resource.save(null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
