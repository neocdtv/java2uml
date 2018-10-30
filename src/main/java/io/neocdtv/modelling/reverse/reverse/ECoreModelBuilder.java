package io.neocdtv.modelling.reverse.reverse;

import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;
import com.thoughtworks.qdox.model.impl.DefaultJavaType;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author xix
 */
public class ECoreModelBuilder {

	private final static Logger LOGGER = Logger.getLogger(ECoreModelBuilder.class.getSimpleName());

	private final static EcoreFactory ECORE_FACTORY = EcoreFactory.eINSTANCE;
	private final static EcorePackage ECORE_PACKAGE = EcorePackage.eINSTANCE;

	public static Set<EPackage> build(final Collection<JavaPackage> qPackages) {

		final Set<EPackage> ePackages = new HashSet<>();
		for (JavaPackage qPackage : qPackages) {
			EPackage ePackage = ECORE_FACTORY.createEPackage();
			ePackage.setName(qPackage.getName());
			ePackage.setNsPrefix(buildPrefix(qPackage));
			ePackage.setNsURI("http://" + qPackage.getName());

			final Collection<JavaClass> qClasses = qPackage.getClasses();
			for (JavaClass qClass : qClasses) {
				EClassifier eClassifier;
				if (qClass.isEnum()) {
					eClassifier = buildEEnum(qClass);
				} else {
					eClassifier = buildEClass(qClass);
					buildRelations((EClass)eClassifier, qClass);

				}
				ePackage.getEClassifiers().add(eClassifier);
			}
			ePackages.add(ePackage);
		}

		return ePackages;
	}

	private static void buildRelations(final EClass eClass, final JavaClass qClass) {
		//buildInterfaceImplementation(implementedInterfaces, fromNode);
		//buildSuperClass(clazz, fromNode);
		buildDependencies(eClass, qClass);
		// TODO: buildUsages(clazz, fromNode);
	}

	private static void buildDependencies(final EClass eClass, final JavaClass qClass) {
		final List<JavaField> fields = qClass.getFields();
		for (JavaField field : fields) {
			if (!field.isEnumConstant()) { // COMMENT: omit dependency from enum constants to the same enum
				final JavaClass fieldType = field.getType();
				if (!determineIfFieldShouldTreatedAsAnAttribute(fieldType)) {
					final EReference eReference = ECORE_FACTORY.createEReference();
					eReference.setName(field.getName());

					EClass referenced;
					// TODO: handle array
					// TODO: handle maps; maps in uml?
					// TODO: handle EClass/EEnum
					if (fieldType.isA(Collection.class.getName())) {
						final List<JavaType> actualTypeArguments = ((DefaultJavaParameterizedType) fieldType).getActualTypeArguments();
						final DefaultJavaType genericTypeVariable = (DefaultJavaType) actualTypeArguments.get(0);
						referenced = buildEClass(genericTypeVariable);
						eReference.setContainment(true);
						eReference.setLowerBound(0);
						eReference.setUpperBound(EStructuralFeature.UNBOUNDED_MULTIPLICITY);
					} else {
						referenced = buildEClass(fieldType);
					}
					eReference.setEType(referenced);
					eClass.getEStructuralFeatures().add(eReference);
				}
			}
		}
	}

	private static EClass buildEClass(JavaClass qClass) {
		final EClass eClass = ECORE_FACTORY.createEClass();
		eClass.setName(qClass.getSimpleName());
		eClass.setInstanceClassName(qClass.getName());
		for (JavaField javaField : qClass.getFields()) {
			if (determineIfFieldShouldTreatedAsAnAttribute(javaField.getType())) 	{
				final EAttribute eAttribute = ECORE_FACTORY.createEAttribute();

				eAttribute.setName(javaField.getName());
				eAttribute.setEType(mapPrimitiveType(javaField.getType()));
				eClass.getEStructuralFeatures().add(eAttribute);
			}
		}

		return eClass;
	}

	private static EDataType mapPrimitiveType(final JavaClass type) {
		final String name = type.getName();
		if (name.equals("boolean")) {
			return ECORE_PACKAGE.getEBoolean();
		} else if (name.equals("int")) {
			return ECORE_PACKAGE.getEInt();
		}
		LOGGER.log(Level.WARNING, "Mapping for primitive type {0} not available, defaulting to EString", name);
		return ECORE_PACKAGE.getEString();
	}

	private static EClassifier buildEEnum(JavaClass qClass) {
		final EEnum eEnum = ECORE_FACTORY.createEEnum();
		final String name = qClass.getSimpleName();
		eEnum.setName(name);
		eEnum.setInstanceClassName(qClass.getName());
		final List<JavaField> enumConstants = qClass.getEnumConstants();
		for (JavaField enumConstant : enumConstants) {
			final EEnumLiteral eEnumLiteral = ECORE_FACTORY.createEEnumLiteral();
			eEnumLiteral.setLiteral(enumConstant.getName());
			eEnumLiteral.setName(name + "." + enumConstant.getName());
			eEnum.getELiterals().add(eEnumLiteral);
		}
		return eEnum;
	}

	private static String buildPrefix(JavaPackage qPackage) {
		final String[] split = qPackage.getName().split("\\.");
		return split[split.length-1];
	}

	private static boolean determineIfFieldShouldTreatedAsAnAttribute(JavaClass fieldsType) {
		return fieldsType.isPrimitive(); // CHECK: || isJavaLibraryType(fieldsType) && !fieldsType.isA(Collection.class.getName());
	}
}
