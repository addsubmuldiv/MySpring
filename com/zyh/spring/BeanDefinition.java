package com.zyh.spring;

/**
 * 这个
 */
public class BeanDefinition {
    private Class type;
    private String scpoe;

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getScpoe() {
        return scpoe;
    }

    public void setScpoe(String scpoe) {
        this.scpoe = scpoe;
    }
}
