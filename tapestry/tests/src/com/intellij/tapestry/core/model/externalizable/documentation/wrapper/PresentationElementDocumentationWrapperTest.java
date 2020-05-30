package com.intellij.tapestry.core.model.externalizable.documentation.wrapper;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class PresentationElementDocumentationWrapperTest {

    @Test
    public void complete() throws Exception {
        PresentationElementDocumentationWrapper wrapper = new PresentationElementDocumentationWrapper(getClass().getResource("/documentation/presentation/Complete.xml"));

        // check description
        assertEquals(wrapper.getDescription(), "Component that triggers an action on the server with a subsequent full page refresh.");

        // check parameters
        assertEquals(wrapper.getParameterDescription("context"), "value1");

        assertEquals(wrapper.getParameterDescription("disabled"), "value2");

        assertEquals(wrapper.getParameterDescription("dontexist"), "");

        // check examples
        assertEquals(wrapper.getExamples(), "Some component examples in HTML.");

        // check notes
        assertEquals(wrapper.getNotes(), "Some component notes in HTML.");
    }

    @Test
    public void empty() throws Exception {
        PresentationElementDocumentationWrapper wrapper = new PresentationElementDocumentationWrapper(getClass().getResource("/documentation/presentation/Empty.xml"));

        // check description
        assertEquals(wrapper.getDescription().length(), 0);

        // check examples
        assertEquals(wrapper.getExamples().length(), 0);

        // check notes
        assertEquals(wrapper.getNotes().length(), 0);
    }

    @Test
    public void no_resource() {
        PresentationElementDocumentationWrapper wrapper = new PresentationElementDocumentationWrapper();

        // check description
        assertEquals(wrapper.getDescription().length(), 0);

        // check examples
        assertEquals(wrapper.getExamples().length(), 0);

        // check notes
        assertEquals(wrapper.getNotes().length(), 0);
    }
}
