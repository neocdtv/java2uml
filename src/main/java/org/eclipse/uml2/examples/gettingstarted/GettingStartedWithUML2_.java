/*
 * Copyright (c) 2014, 2018 CEA and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *   Christian W. Damus (CEA) - initial API and implementation
 *   Kenn Hussey - 535301
 *
 */
package org.eclipse.uml2.examples.gettingstarted;

import io.neocdtv.modelling.reverse.serialization.ModelSerializer;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.uml2.common.util.UML2Util;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Generalization;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.util.UMLUtil;

import java.util.Collection;
import java.util.HashMap;

/**
 * A Java program that may be run stand-alone (with the required EMF and UML2
 * bundle JARs on the classpath) to create the example model illustrated in the
 * <em>Getting Started with UML2</em> article on the Wiki.
 *
 * @see http://wiki.eclipse.org/MDT/UML2/Getting_Started_with_UML2
 */
public class GettingStartedWithUML2_ {

  public static boolean DEBUG = true;

  public static final String ECLIPSE_PROJECT_MODEL_PATH = "/home/xix/workspace-eclipse-modeling-current/reversed-models/model/";

  public static void main(String[] args) {
    Model model = buildModel();

    Collection<EPackage> ePackages = UMLUtil.convertToEcore(model, new HashMap<>());

    ModelSerializer.serializeEcore(ePackages,
        ECLIPSE_PROJECT_MODEL_PATH + "model-epo.ecore");
    ModelSerializer.serializeEcoreJson(ePackages,
        ECLIPSE_PROJECT_MODEL_PATH + "model-epo.json");
    ModelSerializer.serializeUml(model,
        ECLIPSE_PROJECT_MODEL_PATH + "model-epo.uml");
  }

  public static Model buildModel() {


    banner("Creating root model package and primitive types.");

    // Create the root package (a model).
    Model epo2Model = createModel("epo2");

    // Create primitive types to be used as types of attributes in our
    // classes.
    PrimitiveType intPrimitiveType = createPrimitiveType(epo2Model, "int");
    PrimitiveType stringPrimitiveType = createPrimitiveType(epo2Model,
        "String");
    PrimitiveType datePrimitiveType = createPrimitiveType(epo2Model, "Date");
    PrimitiveType skuPrimitiveType = createPrimitiveType(epo2Model, "SKU");

    // Create enumerations to be used as types of attributes in our classes.
    Enumeration orderStatusEnumeration = createEnumeration(epo2Model,
        "OrderStatus");
    createEnumerationLiteral(orderStatusEnumeration, "Pending");
    createEnumerationLiteral(orderStatusEnumeration, "BackOrder");
    createEnumerationLiteral(orderStatusEnumeration, "Complete");

    banner("Creating model classes.");

    // Create the classes.
    org.eclipse.uml2.uml.Class supplierClass = createClass(epo2Model,
        "Supplier", false);
    org.eclipse.uml2.uml.Class customerClass = createClass(epo2Model,
        "Customer", false);
    org.eclipse.uml2.uml.Class purchaseOrderClass = createClass(epo2Model,
        "PurchaseOrder", false);
    org.eclipse.uml2.uml.Class itemClass = createClass(epo2Model, "Item",
        false);
    org.eclipse.uml2.uml.Class addressClass = createClass(epo2Model,
        "Address", true);
    org.eclipse.uml2.uml.Class usAddressClass = createClass(epo2Model,
        "USAddress", false);
    org.eclipse.uml2.uml.Class globalAddressClass = createClass(epo2Model,
        "GlobalAddress", false);
    org.eclipse.uml2.uml.Class globalLocationClass = createClass(epo2Model,
        "GlobalLocation", false);

    // Create generalization relationships amongst our classes.
    createGeneralization(usAddressClass, addressClass);
    createGeneralization(globalAddressClass, addressClass);
    createGeneralization(globalAddressClass, globalLocationClass);

    banner("Creating attributes of model classes.");

    // Create attributes in our classes.
    createAttribute(supplierClass, "name", stringPrimitiveType, 0, 1);
    createAttribute(customerClass, "customerID", intPrimitiveType, 1, 1);
    createAttribute(purchaseOrderClass, "comment", stringPrimitiveType, 0,
        1);
    createAttribute(purchaseOrderClass, "orderDate", datePrimitiveType, 1,
        1);
    createAttribute(purchaseOrderClass, "status", orderStatusEnumeration,
        1, 1);
    createAttribute(purchaseOrderClass, "totalAmount", intPrimitiveType, 0,
        1);
    createAttribute(itemClass, "productName", stringPrimitiveType, 0, 1);
    createAttribute(itemClass, "quantity", intPrimitiveType, 0, 1);
    createAttribute(itemClass, "usPrice", intPrimitiveType, 0, 1);
    createAttribute(itemClass, "comment", stringPrimitiveType, 0, 1);
    createAttribute(itemClass, "shipDate", datePrimitiveType, 0, 1);
    createAttribute(itemClass, "partNum", skuPrimitiveType, 1, 1);
    createAttribute(addressClass, "name", stringPrimitiveType, 0, 1);
    createAttribute(addressClass, "country", stringPrimitiveType, 0, 1);
    createAttribute(usAddressClass, "street", stringPrimitiveType, 1, 1);
    createAttribute(usAddressClass, "city", stringPrimitiveType, 1, 1);
    createAttribute(usAddressClass, "state", stringPrimitiveType, 1, 1);
    createAttribute(usAddressClass, "zip", stringPrimitiveType, 1, 1);
    createAttribute(globalAddressClass, "location", stringPrimitiveType, 1,
        1);
    createAttribute(globalLocationClass, "countryCode", intPrimitiveType,
        1, 1);

    banner("Creating associations between model classes.");

    // Create associations between our classes.
    createAssociation(
        supplierClass,
        false,
        AggregationKind.NONE_LITERAL,
        "orders", 0,
        LiteralUnlimitedNatural.UNLIMITED,
        purchaseOrderClass,
        false,
        AggregationKind.NONE_LITERAL,
        "",
        1, 1);

    createAssociation(
        supplierClass,
        true,
        AggregationKind.NONE_LITERAL,
        "pendingOrders",
        0,
        LiteralUnlimitedNatural.UNLIMITED,
        purchaseOrderClass,
        false,
        AggregationKind.NONE_LITERAL,
        "",
        0,
        1);

    createAssociation(supplierClass, true, AggregationKind.NONE_LITERAL,
        "shippedOrders", 0, LiteralUnlimitedNatural.UNLIMITED,
        purchaseOrderClass, false, AggregationKind.NONE_LITERAL, "", 0, 1);
    createAssociation(supplierClass, true,
        AggregationKind.COMPOSITE_LITERAL, "customers", 0,
        LiteralUnlimitedNatural.UNLIMITED, customerClass, false,
        AggregationKind.NONE_LITERAL, "", 1, 1);
    createAssociation(customerClass, true, AggregationKind.NONE_LITERAL,
        "orders", 0, LiteralUnlimitedNatural.UNLIMITED, purchaseOrderClass,
        true, AggregationKind.NONE_LITERAL, "customer", 1, 1);
    createAssociation(purchaseOrderClass, true,
        AggregationKind.NONE_LITERAL, "previousOrder", 0, 1,
        purchaseOrderClass, false, AggregationKind.NONE_LITERAL, "", 0, 1);
    createAssociation(purchaseOrderClass, true,
        AggregationKind.COMPOSITE_LITERAL, "items", 0,
        LiteralUnlimitedNatural.UNLIMITED, itemClass, true,
        AggregationKind.NONE_LITERAL, "order", 1, 1);
    createAssociation(purchaseOrderClass, true,
        AggregationKind.COMPOSITE_LITERAL, "billTo", 1, 1, addressClass,
        false, AggregationKind.NONE_LITERAL, "", 1, 1);
    createAssociation(purchaseOrderClass, true,
        AggregationKind.COMPOSITE_LITERAL, "shipTo", 1, 1, addressClass,
        false, AggregationKind.NONE_LITERAL, "", 1, 1);

    return epo2Model;
  }

  //
  // Model-building utilities
  //

  protected static Model createModel(String name) {
    Model model = UMLFactory.eINSTANCE.createModel();
    model.setName(name);

    out("Model '%s' created.", model.getQualifiedName());

    return model;
  }

  protected static org.eclipse.uml2.uml.Package createPackage(
      org.eclipse.uml2.uml.Package nestingPackage, String name) {

    org.eclipse.uml2.uml.Package package_ = nestingPackage
        .createNestedPackage(name);

    out("Package '%s' created.", package_.getQualifiedName());

    return package_;
  }

  protected static PrimitiveType createPrimitiveType(
      org.eclipse.uml2.uml.Package package_, String name) {

    PrimitiveType primitiveType = package_.createOwnedPrimitiveType(name);

    out("Primitive type '%s' created.", primitiveType.getQualifiedName());

    return primitiveType;
  }

  protected static Enumeration createEnumeration(
      org.eclipse.uml2.uml.Package package_, String name) {

    Enumeration enumeration = package_.createOwnedEnumeration(name);

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

  protected static org.eclipse.uml2.uml.Class createClass(
      org.eclipse.uml2.uml.Package package_, String name,
      boolean isAbstract) {

    org.eclipse.uml2.uml.Class class_ = package_.createOwnedClass(name,
        isAbstract);

    out("Class '%s' created.", class_.getQualifiedName());

    return class_;
  }

  protected static Generalization createGeneralization(
      Classifier specificClassifier, Classifier generalClassifier) {

    Generalization generalization = specificClassifier
        .createGeneralization(generalClassifier);

    out("Generalization %s --|> %s created.",
        specificClassifier.getQualifiedName(),
        generalClassifier.getQualifiedName());

    return generalization;
  }

  protected static Property createAttribute(
      org.eclipse.uml2.uml.Class class_, String name, Type type,
      int lowerBound, int upperBound) {

    Property attribute = class_.createOwnedAttribute(name, type,
        lowerBound, upperBound);

    out("Attribute '%s' : %s [%s..%s] created.", //
        attribute.getQualifiedName(), // age name
        type.getQualifiedName(), // type name
        lowerBound, // no special case for multiplicity lower bound
        (upperBound == LiteralUnlimitedNatural.UNLIMITED)
            ? "*" // special case for unlimited bound
            : upperBound);

    return attribute;
  }

  protected static Association createAssociation(Type type1,
                                                 boolean end1IsNavigable, AggregationKind end1Aggregation,
                                                 String end1Name, int end1LowerBound, int end1UpperBound,
                                                 Type type2, boolean end2IsNavigable,
                                                 AggregationKind end2Aggregation, String end2Name,
                                                 int end2LowerBound, int end2UpperBound) {

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

  protected static void banner(String format, Object... args) {
    System.out.println();
    hrule();

    System.out.printf(format, args);
    if (!format.endsWith("%n")) {
      System.out.println();
    }

    hrule();
    System.out.println();
  }

  protected static void hrule() {
    System.out.println("------------------------------------");
  }

  protected static void out(String format, Object... args) {
    if (DEBUG) {
      System.out.printf(format, args);
      if (!format.endsWith("%n")) {
        System.out.println();
      }
    }
  }

}
