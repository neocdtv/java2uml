/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.java2uml.reverse;

import com.mycompany.java2uml.diagram.Clazz;
import com.mycompany.java2uml.diagram.Direction;
import com.mycompany.java2uml.diagram.Enumeration;
import com.mycompany.java2uml.diagram.IClass;
import com.mycompany.java2uml.diagram.Model;
import com.mycompany.java2uml.diagram.Relation;
import com.mycompany.java2uml.diagram.RelationType;
import com.mycompany.java2uml.diagram.Visibility;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;
import com.thoughtworks.qdox.model.impl.DefaultJavaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author ofco
 */
public class ModelBuilder {

    private static List<String> PACKAGES_TO_OMMIT = new ArrayList<>();

    static {
        PACKAGES_TO_OMMIT.add("java.lang");
        PACKAGES_TO_OMMIT.add("java.time");
        PACKAGES_TO_OMMIT.add("java.security");
        PACKAGES_TO_OMMIT.add("java.util");
        PACKAGES_TO_OMMIT.add("java.io");
        PACKAGES_TO_OMMIT.add("java.math");
    }

    private ModelBuilder() {
    }

    public static Model build(final Collection<JavaClass> classes) {
        final Model model = new Model();
        for (JavaClass clazz : classes) {
            IClass iClass;
            if (clazz.isEnum()) {
                iClass = buildEnum(clazz);
                final String packageName = clazz.getPackageName();
                final com.mycompany.java2uml.diagram.Package aPackage = model.getPackage(packageName);
                aPackage.addEnumeration((Enumeration) iClass);
            } else {
                iClass = buildClass(clazz);
                final String packageName = clazz.getPackageName();
                final com.mycompany.java2uml.diagram.Package aPackage = model.getPackage(packageName);
                aPackage.addClass((Clazz) iClass);
            }
            final Set<Relation> relations = buildRelations(clazz, iClass);
            //updateRelationsDirection(model.getRelations(), relations); 
            // TODO: still in progress
            // TODO: handle scenario: Person to Address relation and back, having two Addresses (first and second), address knowing its person
            model.addRelations(relations);

        }
        return model;
    }

    private static IClass buildEnum(JavaClass javaClass) {
        final Enumeration enumeration = new Enumeration(javaClass.getCanonicalName(), javaClass.getName());

        final List<JavaField> enumConstants = javaClass.getEnumConstants();
        for (JavaField enumConstant : enumConstants) {
            enumeration.addConstat(enumConstant.getName());
        }
        return enumeration;
    }

    private static Clazz buildClass(final JavaClass javaClass) {
        final Clazz clazz = new Clazz(javaClass.getCanonicalName(), javaClass.getName());
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

    private static Set<Relation> buildRelations(final JavaClass clazz, final IClass fromNode) {
        final HashSet<Relation> relations = new HashSet<>();
        final List<JavaClass> implementedInterfaces = clazz.getImplementedInterfaces();
        buildInterfaceImplementation(implementedInterfaces, fromNode, relations);
        buildSuperClass(clazz, fromNode, relations);
        buildDepedencies(clazz, fromNode, relations);
        return relations;
    }

    private static void buildSuperClass(final JavaClass clazz, final IClass fromNode, final HashSet<Relation> relations) {
        final JavaClass superJavaClass = clazz.getSuperJavaClass();
        if (superJavaClass != null && !superJavaClass.isPrimitive() && !isWrapperForPrimitive(superJavaClass) && isNotJavaSuperClass(superJavaClass)) {
            System.out.println("SuperClass: " + superJavaClass.getName());
            if (!PACKAGES_TO_OMMIT.contains(superJavaClass.getPackageName())) {
                final Relation relation = new Relation(fromNode, buildClass(superJavaClass), RelationType.INHERITANCE, Direction.UNI);
                relations.add(relation);
            }
        }
    }

    private static void buildInterfaceImplementation(final List<JavaClass> implementedInterfaces, final IClass fromNode, final HashSet<Relation> relations) {
        for (JavaClass implementedInterface : implementedInterfaces) {
            if (!PACKAGES_TO_OMMIT.contains(implementedInterface.getPackageName())) {
                System.out.println("Interface: " + implementedInterface.getPackageName());
                final Clazz buildClass = buildClass(implementedInterface);
                final Relation relation = new Relation(fromNode, buildClass, RelationType.INTERFACE_REALIZATION, Direction.UNI);
                relations.add(relation);
            }

        }
    }

    private static void buildDepedencies(final JavaClass clazz, final IClass fromNode, final HashSet<Relation> relations) {
        final List<JavaField> fields = clazz.getFields();
        for (JavaField field : fields) {
            if (!PACKAGES_TO_OMMIT.contains(field.getType().getPackageName())) {
                System.out.println("Depedency: " + field.getType().getPackageName());
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
                        relations.add(relation);
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
