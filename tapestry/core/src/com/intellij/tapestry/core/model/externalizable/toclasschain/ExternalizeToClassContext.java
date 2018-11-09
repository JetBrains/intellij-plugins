package com.intellij.tapestry.core.model.externalizable.toclasschain;

import com.intellij.tapestry.core.java.IJavaClassType;
import org.apache.commons.chain.Context;

import java.util.HashMap;

public class ExternalizeToClassContext extends HashMap implements Context {

    private static final long serialVersionUID = 3216416767982081657L;

    private static final String ELEMENT_KEY = "element";
    private static final String TARGET_CLASS_KEY = "target-class";
    private static final String RESULT_KEY = "result";

    public ExternalizeToClassContext(Object element, IJavaClassType targetClass) {
        put(ELEMENT_KEY, element);
        put(TARGET_CLASS_KEY, targetClass);
    }

    public Object getElement() {
        return get(ELEMENT_KEY);
    }

    public IJavaClassType getTargetClass() {
        return (IJavaClassType) get(TARGET_CLASS_KEY);
    }

    public void setResult(String result) {
        put(RESULT_KEY, result);
    }

    public String getResult() {
        return (String) get(RESULT_KEY);
    }
}
