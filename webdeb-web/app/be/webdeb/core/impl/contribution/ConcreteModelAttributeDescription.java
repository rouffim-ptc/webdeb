package be.webdeb.core.impl.contribution;

import be.webdeb.core.api.contribution.ModelAttributeDescription;
import be.webdeb.core.api.contribution.EDBRelationType;
import be.webdeb.core.api.contribution.ModelDescription;

public class ConcreteModelAttributeDescription implements ModelAttributeDescription {

    private String name;
    private String technicalName;
    private boolean isId;
    private EDBRelationType type;

    public ConcreteModelAttributeDescription(String name, String technicalName, boolean isId, EDBRelationType type) {
        this.name = name;
        this.technicalName = technicalName;
        this.isId = isId;
        this.type = type;
    }

    @Override
    public String getAttributeTechName() {
        return technicalName;
    }

    @Override
    public String getAttributeName() {
        return name;
    }

    @Override
    public boolean isId() {
        return isId;
    }

    @Override
    public EDBRelationType getRelationType() {
        return type;
    }

    @Override
    public String toString() {
        return "ConcreteModelAttributeDescription{" +
                "name='" + name + '\'' +
                ", technicalName='" + technicalName + '\'' +
                ", isId='" + isId + '\'' +
                ", type=" + type +
                '}';
    }

    @Override
    public int hashCode() {
        return technicalName.hashCode();
    }
}
