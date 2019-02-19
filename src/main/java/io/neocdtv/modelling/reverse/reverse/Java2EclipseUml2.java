package io.neocdtv.modelling.reverse.reverse;

import com.thoughtworks.qdox.model.JavaPackage;
import org.batchjob.uml.io.utils.Uml2Utils;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLFactory;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * @author xix
 * @since 24.01.19
 */
public class Java2EclipseUml2 {
  /*
  1. build uml model with 2 classes and one dependency
  2. export this model to uml
  3. build uml model with packages and nested packages, check out how the search works from uml-io
   */

  private final static Logger LOGGER = Logger.getLogger(Java2Uml.class.getSimpleName());

  private final static UMLFactory UML_FACTORY = UMLFactory.eINSTANCE;

  public static Model toUml(final Collection<JavaPackage> qPackages) {
    Java2EclipseUml2 java2EclipseUml2 = new Java2EclipseUml2();

    return null;
  }

  public static void main(String[] args) {

    Model model = UML_FACTORY.createModel();

    Package uPackage = UML_FACTORY.createPackage();
    String packageName = "io.example";
    uPackage.setName(packageName);
    uPackage.setURI(packageName);

    //Class  = UML_FACTORY.createClass();

    Property property = Uml2Utils.findElement("testmodel::testpackage::TestClass::testAttribute", model);
    System.out.println("attribute name: " + property.getName());
  }
}
