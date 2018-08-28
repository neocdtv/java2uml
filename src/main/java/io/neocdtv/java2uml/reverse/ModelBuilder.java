/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.neocdtv.java2uml.reverse;

import io.neocdtv.java2uml.model.Clazz;
import io.neocdtv.java2uml.model.Direction;
import io.neocdtv.java2uml.model.Enumeration;
import io.neocdtv.java2uml.model.Classifier;
import io.neocdtv.java2uml.model.Model;
import io.neocdtv.java2uml.model.Relation;
import io.neocdtv.java2uml.model.RelationType;
import io.neocdtv.java2uml.model.Visibility;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;
import com.thoughtworks.qdox.model.impl.DefaultJavaType;
import io.neocdtv.java2uml.model.Package;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author xix
 */
public class ModelBuilder {

	private static List<String> PACKAGES_TO_OMIT = new ArrayList<>();

	static {
		PACKAGES_TO_OMIT.add("java.lang");
		PACKAGES_TO_OMIT.add("java.time");
		PACKAGES_TO_OMIT.add("java.security");
		//PACKAGES_TO_OMIT.add("java.util");
		PACKAGES_TO_OMIT.add("java.io");
		PACKAGES_TO_OMIT.add("java.math");
	}

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
			buildRelations(clazz, modelClass);
			//updateRelationsDirection(model.getRelations(), relations);
			// TODO: still in progress
			// TODO: handle scenario: Person to Address relation and back, having two Addresses (first and second), address knowing its person
		}
		return model;
	}

	private static Classifier buildEnum(JavaClass javaClass) {
		final Enumeration enumeration = new Enumeration(javaClass.getCanonicalName(), javaClass.getName());

		final List<JavaField> enumConstants = javaClass.getEnumConstants();
		for (JavaField enumConstant : enumConstants) {
			enumeration.addConstant(enumConstant.getName());
		}
		return enumeration;
	}

	private static Clazz buildClass(final JavaClass javaClass) {
		System.out.println("Class: " + javaClass.getValue());
		final Clazz clazz = new Clazz(javaClass.getCanonicalName(), javaClass.getValue());
		for (JavaField field : javaClass.getFields()) {
			final JavaClass fieldsClass = field.getType();
			final String fieldName = field.getName();
			final String fieldType = fieldsClass.getName();
			if (fieldsClass.isPrimitive() || isWrapperForPrimitive(fieldsClass) || isSimpleJavaLibraryType(fieldsClass)) {
				clazz.addAttribute(fieldName, fieldType, determineVisiblity(field));
			}
		}
		return clazz;
	}

	private static void buildRelations(final JavaClass clazz, final Classifier fromNode) {
		final List<JavaClass> implementedInterfaces = clazz.getInterfaces();
		buildInterfaceImplementation(implementedInterfaces, fromNode);
		buildSuperClass(clazz, fromNode);
		buildDependencies(clazz, fromNode);
	}

	private static void buildSuperClass(final JavaClass clazz, final Classifier fromNode) {
		final JavaClass superJavaClass = clazz.getSuperJavaClass();
		if (superJavaClass != null && !superJavaClass.isPrimitive() && !isWrapperForPrimitive(superJavaClass) && isNotJavaSuperClass(superJavaClass)) {
			System.out.println("SuperClass: " + superJavaClass.getName());
			if (!PACKAGES_TO_OMIT.contains(superJavaClass.getPackageName())) {
				final Clazz toNode = buildClass(superJavaClass);
				fromNode.addRelation(toNode, RelationType.INHERITANCE, Direction.UNI);
			}
		}
	}

	private static void buildInterfaceImplementation(final List<JavaClass> implementedInterfaces, final Classifier fromNode) {
		for (JavaClass implementedInterface : implementedInterfaces) {
			if (!PACKAGES_TO_OMIT.contains(implementedInterface.getPackageName())) {
				System.out.println("Interface: " + implementedInterface.getPackageName());
				final Clazz toNode = buildClass(implementedInterface);
				fromNode.addRelation(toNode, RelationType.INTERFACE_REALIZATION, Direction.UNI);
			}
		}
	}

	private static void buildDependencies(final JavaClass clazz, final Classifier fromNode) {
		final List<JavaField> fields = clazz.getFields();
		for (JavaField field : fields) {
			if (!PACKAGES_TO_OMIT.contains(field.getType().getPackageName())) {
				System.out.println("Depedency to package: " + field.getType().getPackageName());
				if (!field.isEnumConstant()) {
					final JavaClass fieldClass = field.getType();
					if (!fieldClass.isPrimitive() && !isWrapperForPrimitive(fieldClass) && !isSimpleJavaLibraryType(fieldClass)) {
						String cardinality = null;
						Clazz toNode;
						if (fieldClass.isA(Collection.class.getName())) {
							cardinality = "0..*";
							// TODO: how to deal with maps in uml?
							// type.isA(Map.class.getName()
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
	}

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

	private static Visibility determineVisiblity(final JavaField field) {
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

	private static boolean isWrapperForPrimitive(final JavaClass javaClass) {
		final String value = javaClass.getName();
		return java.lang.Character.class.getSimpleName().equals(value)
				|| java.lang.Byte.class.getSimpleName().equals(value)
				|| java.lang.Long.class.getSimpleName().equals(value)
				|| java.math.BigDecimal.class.getSimpleName().equals(value)
				|| java.math.BigInteger.class.getSimpleName().equals(value)
				|| java.lang.Boolean.class.getSimpleName().equals(value)
				|| java.lang.Short.class.getSimpleName().equals(value)
				|| java.lang.Integer.class.getSimpleName().equals(value)
				|| java.lang.String.class.getSimpleName().equals(value)
				|| java.lang.Float.class.getSimpleName().equals(value)
				|| java.lang.Double.class.getSimpleName().equals(value);
	}

	private static boolean isSimpleJavaLibraryType(final JavaClass javaClass) {
		final String value = javaClass.getName();
		return java.util.Date.class.getSimpleName().equals(value);
	}

	private static boolean isNotJavaSuperClass(final JavaClass superJavaClass) {
		return !java.lang.Object.class.getSimpleName().equals(superJavaClass.getName())
				&& !java.lang.Enum.class.getSimpleName().equals(superJavaClass.getName());
	}

}
