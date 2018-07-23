package com.intellij.tapestry.core.model.externalizable.documentation.generationchain;

import org.apache.commons.chain.Context;

import java.util.HashMap;

/**
 * The context for all documentation generation commands.
 */
public class DocumentationGenerationContext extends HashMap implements Context {

    private static final long serialVersionUID = -3154671436168988693L;

    private static final String ELEMENT_KEY = "element";
    private static final String RESULT_KEY = "result";

    public DocumentationGenerationContext(Object element) {
        put(ELEMENT_KEY, element);
    }

    public Object getElement() {
        return get(ELEMENT_KEY);
    }

    public void setResult(String result) {
        put(RESULT_KEY, result);
    }

    public String getResult() {
        return (String) get(RESULT_KEY);
    }
}
