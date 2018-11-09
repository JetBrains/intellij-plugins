package com.intellij.tapestry.core.model.presentation.valueresolvers;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaType;
import org.apache.commons.chain.Context;

import java.util.HashMap;

public class ValueResolverContext extends HashMap implements Context {

    private static final long serialVersionUID = 8497451152073605755L;

    private static final String VALUE_KEY = "value";
    private static final String DEFAULT_PREFIX_KEY = "default-prefix";
    private static final String CONTEXT_CLASS_KEY = "context-class";
    private static final String PROJECT_KEY = "project";
    private static final String RESULT_TYPE_KEY = "result-type";
    private static final String RESULT_CODE_BIND_KEY = "result-code-bind";

    public ValueResolverContext(TapestryProject project, IJavaClassType contextClass, String value, String defaultPrefix) {
        put(PROJECT_KEY, project);
        put(CONTEXT_CLASS_KEY, contextClass);
        put(VALUE_KEY, value);
        put(DEFAULT_PREFIX_KEY, defaultPrefix);
    }

    public TapestryProject getProject() {
        return (TapestryProject) get(PROJECT_KEY);
    }

    public IJavaClassType getContextClass() {
        return (IJavaClassType) get(CONTEXT_CLASS_KEY);
    }

    public String getValue() {
        return (String) get(VALUE_KEY);
    }

    public String getDefaultPrefix() {
        return (String) get(DEFAULT_PREFIX_KEY);
    }

    public void setResultType(IJavaType type) {
        put(RESULT_TYPE_KEY, type);
    }

    public IJavaType getResultType() {
        return (IJavaType) get(RESULT_TYPE_KEY);
    }

    public void setResultCodeBind(Object codeBind) {
        put(RESULT_CODE_BIND_KEY, codeBind);
    }

    public Object getResultCodeBind() {
        return get(RESULT_CODE_BIND_KEY);
    }
}
