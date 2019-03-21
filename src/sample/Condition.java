package sample;

import javafx.beans.property.SimpleStringProperty;

public class Condition {
    private String criteria;
    private String value;
    private String includeOrExclude;

    public Condition(String criteria, String value, String includeOrExclude) {
        this.criteria = criteria;
        this.value = value;
        this.includeOrExclude = includeOrExclude;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getIncludeOrExclude() {
        return includeOrExclude;
    }

    public void setIncludeOrExclude(String includeOrExclude) {
        this.includeOrExclude = includeOrExclude;
    }
}
