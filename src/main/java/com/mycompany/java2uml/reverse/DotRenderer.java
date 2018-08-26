/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.java2uml.reverse;

import com.mycompany.java2uml.renderer.Renderer;
import com.mycompany.java2uml.diagram.Attribute;
import com.mycompany.java2uml.diagram.Clazz;
import com.mycompany.java2uml.diagram.Enumeration;
import com.mycompany.java2uml.diagram.Model;
import com.mycompany.java2uml.diagram.Relation;
import com.mycompany.java2uml.diagram.Visibility;
import java.util.Set;

public class DotRenderer implements Renderer {

    private final boolean renderPackages = false;

    @Override
    public String renderer(final Model model) {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph G {\n");
        configureLayout(dot);
        for (final com.mycompany.java2uml.diagram.Package packageToRender : model.getPackages()) {
            renderSubGraph(dot, packageToRender);
        }
        final Set<Relation> relations = model.getRelations();
        for (final Relation relation : relations) {
            rendererRelation(dot, relation);
        }
        dot.append("}");
        return dot.toString();
    }

    private void configureLayout(StringBuilder dot) {
        dot.append("\tfontname  = \"Courier\"\n");
        dot.append("\tfontsize  = 8\n");
        dot.append("\tnodesep=0.9\n");
        dot.append("\tranksep=0.9\n");
        dot.append("\tsplines=polyline\n");
        //dot.append("\tsep=\"+50,50\"\n"); // meaning ??
        dot.append("\toverlap=scalexy\n");
        dot.append("\tnode [\n");
        dot.append("\t\tfontname = \"Courier\"\n");
        dot.append("\t\tfontsize  = 8\n");
        dot.append("\t\tshape  = \"record\"\n");
        dot.append("\t]\n");
        dot.append("\tedge [\n");
        dot.append("\t\tfontname = \"Courier\"\n");
        dot.append("\t\tfontsize  = 8\n");
        dot.append("\t]\n");
    }

    private void renderSubGraph(final StringBuilder dot, final com.mycompany.java2uml.diagram.Package packageToRender) {
        if (renderPackages) {
            dot.append("\t");
            dot.append("subgraph ");
            dot.append("\"").append(packageToRender.getId()).append("\"");
            dot.append(" {\n");
            dot.append("\t\t" + "label = \"").append(packageToRender.getLabel()).append("\"\n");
        }
        for (final Clazz clazz : packageToRender.getClasses()) {
            renderClass(dot, clazz);
        }
        for (final Enumeration enumeration : packageToRender.getEnumerations()) {
            renderEnumeration(dot, enumeration);
        }
        if (renderPackages) {
            dot.append("\t}\n");
        }
    }

    private void renderEnumeration(final StringBuilder dot, final Enumeration enumeration) {
        dot.append("\t\t");
        dot.append("\"").append(enumeration.getId()).append("\"");
        dot.append(" [\n");
        dot.append("\t\t\tlabel = ");
        dot.append("\"{");
        dot.append(enumeration.getLabel());
        dot.append("|");
        for (String enumConstant : enumeration.getConstants()) {
            dot.append(enumConstant);
            dot.append("\\l");
        }
        dot.append("|");
        dot.append("}\"\n\t\t]\n");
    }

    private void renderClass(final StringBuilder dot, final Clazz node) {
        dot.append("\t\t");
        dot.append("\"").append(node.getId()).append("\"");
        dot.append(" [\n");
        dot.append("\t\t\tlabel = ");
        dot.append("\"{");
        dot.append(node.getLabel());
        dot.append("|");
        for (Attribute attribute : node.getAttributes()) {
            dot.append(rendererVisibility(attribute.getVisibility()));
            dot.append(" ");
            dot.append(attribute.getName());
            dot.append(" : ");
            dot.append(attribute.getType());
            dot.append("\\l");
        }
        dot.append("|");
        dot.append("}\"\n\t\t]\n");
    }

    private void rendererRelation(final StringBuilder dot, final Relation relation) {
        dot.append("\t");
        dot.append("\"").append(relation.getToNode().getId()).append("\"");
        dot.append(" -> ");
        dot.append("\"").append(relation.getFromNode().getId()).append("\"");
        switch (relation.getRelationType()) {
            case INTERFACE_REALIZATION:
                rendererInterfaceImplemenatation(dot);
                break;
            case INHERITANCE:
                rendererInheritance(dot);
                break;
            case DEPEDENCY:
                rendererDepedency(relation, dot);
                break;
        }
        dot.append("\n");
    }

    private String rendererVisibility(final Visibility visibility) {
        switch (visibility) {
            case PRIVATE:
                return "-";
            case PROTECTED:
                return "#";
            case PUBLIC:
                return "+";
        }
        return "";
    }

    private void rendererInterfaceImplemenatation(StringBuilder dot) {
        dot.append(" [dir=back, style=dashed, arrowtail=empty]");
    }

    private void rendererInheritance(StringBuilder dot) {
        dot.append(" [dir=back, arrowtail=empty]");
    }

    private void rendererDepedency(Relation relation, StringBuilder dot) {
        switch (relation.getDirection()) {
            case BI: // still in progress see: method updateRelationsDirection
                final String toNodeLabel = relation.getToNodeLabel();
                final String fromNodeLabel = relation.getFromNodeLabel();
                dot.append(String.format(" [dir=none, arrowtail=empty, taillabel=\" %s\", headlabel=\" %s \"]", toNodeLabel, fromNodeLabel));
                break;
            case UNI:
                String label = relation.getToNodeLabel();
                if (relation.getToNodeCardinality() != null) {
                    label = label + " \n" + relation.getToNodeCardinality();
                }
                dot.append(String.format(" [dir=back, arrowtail=open ,taillabel=\" %s \"]", label)); // play also with labelangle=\"-7\"
                break;
        }
    }

}
