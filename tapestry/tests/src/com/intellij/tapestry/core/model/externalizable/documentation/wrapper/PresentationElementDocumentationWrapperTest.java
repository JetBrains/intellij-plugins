package com.intellij.tapestry.core.model.externalizable.documentation.wrapper;

import org.testng.annotations.Test;

public class PresentationElementDocumentationWrapperTest {

    @Test
    public void complete() throws Exception {
        PresentationElementDocumentationWrapper wrapper = new PresentationElementDocumentationWrapper(getClass().getResource("/documentation/presentation/Complete.xml"));

        // check description
        assert wrapper.getDescription().equals("Component that triggers an action on the server with a subsequent full page refresh.");

        // check parameters
        assert wrapper.getParameterDescription("context").equals("value1");

        assert wrapper.getParameterDescription("disabled").equals("value2");

        assert wrapper.getParameterDescription("dontexist").equals("");

        // check examples
        assert wrapper.getExamples().equals("Some component examples in HTML.");

        // check notes
        assert wrapper.getNotes().equals("Some component notes in HTML.");
    }

    @Test
    public void empty() throws Exception {
        PresentationElementDocumentationWrapper wrapper = new PresentationElementDocumentationWrapper(getClass().getResource("/documentation/presentation/Empty.xml"));

        // check description
        assert wrapper.getDescription().length() == 0;

        // check examples
        assert wrapper.getExamples().length() == 0;

        // check notes
        assert wrapper.getNotes().length() == 0;
    }

    @Test
    public void no_resource() {
        PresentationElementDocumentationWrapper wrapper = new PresentationElementDocumentationWrapper();

        // check description
        assert wrapper.getDescription().length() == 0;

        // check examples
        assert wrapper.getExamples().length() == 0;

        // check notes
        assert wrapper.getNotes().length() == 0;
    }
}
