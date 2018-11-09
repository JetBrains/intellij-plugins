package com.intellij.tapestry.core.model.externalizable.documentation;

import com.intellij.tapestry.core.model.externalizable.ExternalizableToDocumentation;
import com.intellij.tapestry.core.model.externalizable.documentation.generationchain.DocumentationGenerationChain;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class used to generate the documentation home.
 */
public class Home implements ExternalizableToDocumentation {

    private List<String> _tapestryProjects = new ArrayList<>();

    public Home(List<String> tapestryProjects) {
        _tapestryProjects = tapestryProjects;
    }

    @Override
    public String getDocumentation() throws Exception {
        return DocumentationGenerationChain.getInstance().generate(this);
    }

    public List<String> getTapestryProjects() {
        return _tapestryProjects;
    }
}
