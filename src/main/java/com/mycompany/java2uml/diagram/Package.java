/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.java2uml.diagram;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author ofco
 */
public class Package {
    private final String id;
    private final String label;
    private Set<Clazz> classes;
    private Set<Enumeration> enumerations;

    public Package(String name) {
        this.id = "cluster." + name;
        this.label = "Package " + name;
    }
    
    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void addClass(final Clazz clazz) {
        getClasses().add(clazz);
    }

    public Set<Clazz> getClasses() {
        if (classes == null) {
            classes = new HashSet<>();
        }
        return classes;
    } 
    
    public void addEnumeration(final Enumeration enumeration) {
        getEnumerations().add(enumeration);
    }
    
    public Set<Enumeration> getEnumerations() {
        if (enumerations == null) {
            enumerations = new HashSet<>();
        }
        return enumerations;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Package other = (Package) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    
}
