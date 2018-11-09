package com.intellij.tapestry.core.model.presentation;

import org.testng.annotations.Test;

/**
 * Description Class
 *
 * @author <a href="mailto:monica.carvalho@logical-software.com">Monica Carvalho</a>
 */

public class TemplateElementTest {

    @Test
    public void constructor_with_null_values() {
        TemplateElement templateElement = new TemplateElement(null, null);

        assert templateElement.getElement() == null;

        assert templateElement.getTemplate() == null;
    }

    @Test
    public void constructor_with_some_null_values() {
        String templateMock = "template1";
        InjectedElement injectedElement = org.easymock.EasyMock.createMock(InjectedElement.class);

        TemplateElement templateElement = new TemplateElement(null, templateMock);

        assert templateElement.getElement() == null;

        assert templateElement.getTemplate().equals(templateMock);

        TemplateElement templateElement2 = new TemplateElement(injectedElement, null);

        assert templateElement2.getElement().equals(injectedElement);

        assert templateElement2.getTemplate() == null;
    }


    @Test
    public void compareTo() {
        String templateMock = "template1";
        String templateMock2 = "template2";
        InjectedElement injectedElement = org.easymock.EasyMock.createMock(InjectedElement.class);


        assert new TemplateElement(injectedElement, templateMock).compareTo(new TemplateElement(injectedElement, templateMock)) == 0;

        assert new TemplateElement(injectedElement, templateMock).compareTo(new TemplateElement(injectedElement, templateMock2)) < 0;
    }
}
