package org.eclipse.uml2.examples.gettingstarted;

import org.batchjob.uml.io.utils.Uml2Utils;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.util.UMLUtil;

import java.util.Collection;
import java.util.HashMap;

import static org.eclipse.uml2.examples.gettingstarted.GettingStartedWithUML2_.out;

/**
 * @author xix
 * @since 26.01.19
 */
public class BasicReverseSmallestPossible {

  public static final String ECLIPSE_PROJECT_MODEL_PATH = "/home/xix/workspace-eclipse-modeling-current/reversed-models/model/";
  static UMLFactory UML_FACTORY = UMLFactory.eINSTANCE;

  public static void main(String[] args) {
    Model nature = createModel("Nature");

    // add package to model
    Package io = createPackage("io");
    nature.getNestedPackages().add(io);

    // add class to subpackage
    Class human = createClass("Human");

    Collection<EPackage> ePackages = UMLUtil.convertToEcore(io, new HashMap<>());

    serialize(nature, ePackages);
  }

  private static void serialize(Model nature, Collection<EPackage> ePackages) {
    ModelSerializer.serializeEcore(ePackages,
        ECLIPSE_PROJECT_MODEL_PATH + "model-humanity-min.ecore");
    ModelSerializer.serializeEcoreJson(ePackages,
        ECLIPSE_PROJECT_MODEL_PATH + "model-humanity-min.json");
    ModelSerializer.serializeUml(nature,
        ECLIPSE_PROJECT_MODEL_PATH + "model-humanity-min.uml");
  }

  static Model createModel(String name) {
    Model model = UML_FACTORY.createModel();
    model.setName(name);
    model.setURI("http://" + name);

    out("Model '%s' created.", model.getQualifiedName());
    return model;
  }

  static Package createPackage(String name) {
    Package aPackage = UML_FACTORY.createPackage();
    aPackage.setName(name);
    return aPackage;
  }

  static Class createClass(final String name) {
    Class aClass = UML_FACTORY.createClass();
    aClass.setName(name);
    return aClass;
  }

  static Interface createInterface(final String name) {
    Interface anInterface = UML_FACTORY.createInterface();
    anInterface.setName(name);
    return anInterface;
  }

  static PrimitiveType createPrimitiveType(Model model, String packageName, String name) {
    Package uPackage = createPackage(packageName);
    model.getNestedPackages().add(uPackage);
    PrimitiveType primitiveType = uPackage.createOwnedPrimitiveType(name);
    out("Primitive type '%s' created.", primitiveType.getQualifiedName());
    return primitiveType;
  }

  protected static Association createAssociation(Type type1,
                                                 boolean end1IsNavigable,
                                                 AggregationKind end1Aggregation,
                                                 String end1Name,
                                                 int end1LowerBound,
                                                 int end1UpperBound,
                                                 Type type2,
                                                 boolean end2IsNavigable,
                                                 AggregationKind end2Aggregation,
                                                 String end2Name,
                                                 int end2LowerBound,
                                                 int end2UpperBound) {


    Association association = type1.createAssociation(end1IsNavigable,
        end1Aggregation, end1Name, end1LowerBound, end1UpperBound, type2,
        end2IsNavigable, end2Aggregation, end2Name, end2LowerBound,
        end2UpperBound);

    out("Association %s [%s..%s] %s-%s %s [%s..%s] created.", //
        UML2Util.isEmpty(end1Name)
            // compute a placeholder for the name
            ? String.format("{%s}", type1.getQualifiedName()) //
            // user-specified name
            : String.format("'%s::%s'", type1.getQualifiedName(), end1Name), //
        end1LowerBound, // no special case for this
        (end1UpperBound == LiteralUnlimitedNatural.UNLIMITED)
            ? "*" // special case for unlimited upper bound
            : end1UpperBound, // finite upper bound
        end2IsNavigable
            ? "<" // indicate navigability
            : "-", // not navigable
        end1IsNavigable
            ? ">" // indicate navigability
            : "-", // not navigable
        UML2Util.isEmpty(end2Name)
            // compute a placeholder for the name
            ? String.format("{%s}", type2.getQualifiedName()) //
            // user-specified name
            : String.format("'%s::%s'", type2.getQualifiedName(), end2Name), //
        end2LowerBound, // no special case for this
        (end2UpperBound == LiteralUnlimitedNatural.UNLIMITED)
            ? "*" // special case for unlimited upper bound
            : end2UpperBound);

    return association;
  }

  protected static Enumeration createEnumeration(
      Package uPackage, String name) {

    Enumeration enumeration = uPackage.createOwnedEnumeration(name);

    out("Enumeration '%s' created.", enumeration.getQualifiedName());

    return enumeration;
  }

  protected static EnumerationLiteral createEnumerationLiteral(
      Enumeration enumeration, String name) {

    EnumerationLiteral enumerationLiteral = enumeration
        .createOwnedLiteral(name);

    out("Enumeration literal '%s' created.",
        enumerationLiteral.getQualifiedName());

    return enumerationLiteral;
  }

}
