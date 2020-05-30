package com.intellij.tapestry.core.model.presentation.valueresolvers.property;

import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaMethod;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.mocks.JavaMethodMock;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class SinglePropertyResolverTest extends AbstractSpecialCaseTest {
    private SinglePropertyResolver _singlePropertyResolver;
    private JavaClassTypeMock _contextClassTypeMock;

    @BeforeTest
    public void init() {
        _singlePropertyResolver = new SinglePropertyResolver();
        _contextClassTypeMock = new JavaClassTypeMock("MyClass");

        // public method that returns a class and has no parameters
        _contextClassTypeMock.addPublicMethod(new JavaMethodMock("getProp1", new JavaClassTypeMock("prop1returntype"), new ArrayList<>()));
    }

    @Test
    public void can_resolve() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, _contextClassTypeMock, "prop:prop1", null);

        replay();

        assert _singlePropertyResolver.execute(_valueResolverContext);

        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("prop1returntype");

        assert ((IJavaMethod) _valueResolverContext.getResultCodeBind()).getName().equals("getProp1");

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, _contextClassTypeMock, "prop:pRoP1", null);

        replay();

        assert _singlePropertyResolver.execute(_valueResolverContext);

        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("prop1returntype");

        assert ((IJavaMethod) _valueResolverContext.getResultCodeBind()).getName().equals("getProp1");
    }

    @Test
    public void cant_resolve() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, _contextClassTypeMock, "prop:propthatdoesnexist", null);

        replay();

        assert _singlePropertyResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;
    }
}
