package be.webdeb.core.impl.contribution;

import be.webdeb.core.api.contribution.ModelAttributeDescription;
import be.webdeb.core.api.contribution.ModelDescription;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class ConcreteModelDescription implements ModelDescription {

    private String name;
    private Set<ModelAttributeDescription> attributes;

    public ConcreteModelDescription(String name) {
        this.name = name;
        this.attributes = new LinkedHashSet<>();
    }

    @Override
    public String getModelName() {
        return name;
    }

    @Override
    public Set<ModelAttributeDescription> getAttributesMap() {
        return attributes;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "ConcreteModelDescription{" +
                "name='" + name + '\'' +
                '}';
    }
}
