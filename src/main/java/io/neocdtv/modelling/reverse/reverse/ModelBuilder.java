/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.modelling.reverse.reverse;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;
import com.thoughtworks.qdox.model.impl.DefaultJavaType;
import io.neocdtv.modelling.reverse.model.Classifier;
import io.neocdtv.modelling.reverse.model.Clazz;
import io.neocdtv.modelling.reverse.model.Direction;
import io.neocdtv.modelling.reverse.model.Enumeration;
import io.neocdtv.modelling.reverse.model.Model;
import io.neocdtv.modelling.reverse.model.Package;
import io.neocdtv.modelling.reverse.model.Relation;
import io.neocdtv.modelling.reverse.model.RelationType;
import io.neocdtv.modelling.reverse.model.Visibility;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author xix
 */
public class ModelBuilder {

	private static final Logger LOGGER = Logger.getLogger(ModelBuilder.class.getCanonicalName());

	private ModelBuilder() {
	}

	public static Model build(final Collection<JavaClass> classes) {
		final Model model = new Model();
		for (JavaClass clazz : classes) {
			Classifier modelClass;
			if (clazz.isEnum()) {
				modelClass = buildEnum(clazz);
				final String packageName = clazz.getPackageName();
				final Package aPackage = model.getPackage(packageName);
				aPackage.addClassifier(modelClass);
			} else {
				modelClass = buildClass(clazz);
				final String packageName = clazz.getPackageName();
				final Package aPackage = model.getPackage(packageName);
				aPackage.addClassifier(modelClass);
			}
			// TODO: check if class is in loaded sources/sourceTree

			buildRelations(clazz, modelClass);
			// TODO: updateRelationsDirection(model.getRelations(), relations);
			// TODO: still in progress
			// TODO: handle scenario: Person to Address relation and back, having two Addresses (first and second), address knowing its person
		}
		return model;
	}

	private static Classifier buildEnum(final JavaClass javaClass) {
		final String canonicalName = javaClass.getCanonicalName();
		LOGGER.info("building enum: " + canonicalName);
		final Enumeration enumeration = new Enumeration(canonicalName, javaClass.getName());
		final List<JavaField> enumConstants = javaClass.getEnumConstants();
		for (JavaField enumConstant : enumConstants) {
			final String constantName = enumConstant.getName();
			LOGGER.info("building enum: " + canonicalName + ", adding constant: " + constantName);
			enumeration.addConstant(constantName);
		}
		return enumeration;
	}

	private static Clazz buildClass(final JavaClass javaClass) {
		final String canonicalName = javaClass.getCanonicalName();
		LOGGER.info("building class: " + canonicalName);
		final Clazz clazz = new Clazz(canonicalName, javaClass.getValue());
		for (JavaField field : javaClass.getFields()) {
			final JavaClass fieldsType = field.getType();
			final String fieldName = field.getName();
			if (determineIfFieldShouldTreatedAsAnAttribute(fieldsType)) {
				LOGGER.info("building class: " + canonicalName + ", adding field: " + fieldName + " with type: " + fieldsType.getCanonicalName());
				final boolean constant = field.isStatic() && !fieldName.matches("[a-z]*") && field.isFinal();
				clazz.addAttribute(fieldName, fieldsType.getName(), determineVisibility(field), constant);
			} else {
				LOGGER.info("building class: " + canonicalName + ", not adding field: " + fieldName + " with type: " + fieldsType.getCanonicalName());
			}
		}
		return clazz;
	}

	private static void buildRelations(final JavaClass clazz, final Classifier fromNode) {
		final List<JavaClass> implementedInterfaces = clazz.getInterfaces();
		buildInterfaceImplementation(implementedInterfaces, fromNode);
		buildSuperClass(clazz, fromNode);
		buildDependencies(clazz, fromNode);
		// TODO: buildUsages(clazz, fromNode);
	}

	private static void buildSuperClass(final JavaClass clazz, final Classifier fromNode) {
		final JavaClass superJavaClass = clazz.getSuperJavaClass();
		if (determineIfSuperClassShouldBeIncluded(superJavaClass)) {
			LOGGER.info("building relation to super class: " + superJavaClass.getCanonicalName() + " from: " + fromNode.getClass().getCanonicalName());
			final Clazz toNode = buildClass(superJavaClass);
			fromNode.addRelation(toNode, RelationType.INHERITANCE, Direction.UNI);

		} else {
			LOGGER.info("not building super class: " + superJavaClass != null ? null : superJavaClass.getCanonicalName() + " for: " + fromNode.getClass().getCanonicalName());
		}
	}

	private static void buildInterfaceImplementation(final List<JavaClass> implementedInterfaces, final Classifier fromNode) {
		for (JavaClass implementedInterface : implementedInterfaces) {
			if (determineIfInterfaceShouldBeIncluded(implementedInterface)) {
				final String canonicalName = implementedInterface.getCanonicalName();
				LOGGER.info("building interface: " + canonicalName + " for: " + fromNode.getClass().getCanonicalName());
				LOGGER.info("building interface realization relation to interface: " + canonicalName + " from: " + fromNode.getClass().getCanonicalName());
				final Clazz toNode = buildClass(implementedInterface);
				fromNode.addRelation(toNode, RelationType.INTERFACE_REALIZATION, Direction.UNI);
			}
		}
	}

	private static void buildDependencies(final JavaClass clazz, final Classifier fromNode) {
		final List<JavaField> fields = clazz.getFields();
		for (JavaField field : fields) {
			if (!field.isEnumConstant()) { // COMMENT: omit dependency from enum constants to the enum
				final JavaClass fieldClass = field.getType();
				if (!fieldClass.isPrimitive()) {
					String cardinality = null;
					Clazz toNode;
					// TODO: handle array
					// TODO: handle map
					// TODO: maps in uml?
					if (fieldClass.isA(Collection.class.getName())) {
						cardinality = "0..*"; // COMMENT: defaulting to 0..* and not to * since the * is easily overshadowed by other elements during rendering
						final List<JavaType> actualTypeArguments = ((DefaultJavaParameterizedType) fieldClass).getActualTypeArguments();
						final DefaultJavaType genericTypeVariable = (DefaultJavaType) actualTypeArguments.get(0);
						toNode = buildClass(genericTypeVariable);
					} else {
						toNode = buildClass(fieldClass);
					}
					// TODO: add cardinality to the relation type.isArray()
					final Relation relation = new Relation(fromNode, toNode, RelationType.DEPEDENCY, Direction.UNI);
					relation.setToNodeLabel(field.getName());
					relation.setToNodeCardinality(cardinality);
					fromNode.addRelation(toNode, RelationType.DEPEDENCY, Direction.UNI, field.getName(), cardinality);
				}
			}
		}
	}

	// TODO: complete and use it
	private static void updateRelationsDirection(final Set<Relation> relationsInGraph, final Set<Relation> relationsToBeAddedToGraph) {
		for (Relation relationToBeAddedToGraph : relationsToBeAddedToGraph) {
			if (RelationType.DEPEDENCY.equals(relationToBeAddedToGraph.getRelationType())) {
				updateRelationDirection(relationsInGraph, relationToBeAddedToGraph);
			}
		}
	}

	private static void updateRelationDirection(final Set<Relation> relationsInGraph, final Relation relationToBeAdded) {
		for (Relation relationInGraph : relationsInGraph) {
			if (RelationType.DEPEDENCY.equals(relationInGraph.getRelationType())) {
				if (relationToBeAdded.getToNode().equals(relationInGraph.getFromNode())
						&& relationToBeAdded.getFromNode().equals(relationInGraph.getToNode())) {
					relationInGraph.setDirection(Direction.BI);
					relationInGraph.setFromNodeLabel(relationToBeAdded.getToNodeLabel());
				}
			}
		}
	}

	private static Visibility determineVisibility(final JavaField field) {
		if (field.isPrivate()) {
			return Visibility.PRIVATE;
		}
		if (field.isProtected()) {
			return Visibility.PROTECTED;
		}
		if (field.isPublic()) {
			return Visibility.PUBLIC;
		}
		return Visibility.PROTECTED;
	}

	private static boolean determineIfSuperClassShouldBeIncluded(final JavaClass superJavaClass) {
		return superJavaClass != null && !superJavaClass.isPrimitive(); // CHECK: && !isJavaLibraryType(superJavaClass);
	}

	private static boolean determineIfInterfaceShouldBeIncluded(final JavaClass superJavaClass) {
		return true; // CHECK:		return !isJavaLibraryType(superJavaClass);
	}

	private static boolean determineIfFieldShouldTreatedAsAnAttribute(JavaClass fieldsType) {
		return fieldsType.isPrimitive(); // CHECK: || isJavaLibraryType(fieldsType) && !fieldsType.isA(Collection.class.getName());
	}

	private static boolean isJavaLibraryType(final JavaClass javaClass) {
		return javaClass.getPackageName().startsWith("java");
	}
}
