package com.intellij.tapestry.core.model.externalizable.totemplatechain;

import org.apache.commons.chain.Context;

import java.util.HashMap;

public class ExternalizeToTemplateContext extends HashMap implements Context {

    private static final long serialVersionUID = 4033040901249978207L;

    private static final String ELEMENT_KEY = "element";
    private static final String NAMESPACE_PREFIX_KEY = "tnamespace-prefix";
    private static final String RESULT_KEY = "result";

    public ExternalizeToTemplateContext(Object element, String namespacePrefix) {
        put(ELEMENT_KEY, element);
        put(NAMESPACE_PREFIX_KEY, namespacePrefix);
    }

    public Object getElement() {
        return get(ELEMENT_KEY);
    }

    public String getNamespacePrefix() {
        return (String) get(NAMESPACE_PREFIX_KEY);
    }

    public void setResult(String result) {
        put(RESULT_KEY, result);
    }

    public String getResult() {
        return (String) get(RESULT_KEY);
    }
}
