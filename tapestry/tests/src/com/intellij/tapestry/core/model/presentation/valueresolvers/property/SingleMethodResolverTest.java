package com.intellij.tapestry.core.model.presentation.valueresolvers.property;

import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaMethod;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.mocks.JavaMethodMock;
import com.intellij.tapestry.core.mocks.MethodParameterMock;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class SingleMethodResolverTest extends AbstractSpecialCaseTest {
    private SingleMethodResolver _singleMethodResolver;
    private JavaClassTypeMock _contextClassTypeMock;

    @BeforeTest
    public void init() {
        _singleMethodResolver = new SingleMethodResolver();
        _contextClassTypeMock = new JavaClassTypeMock("MyClass");

        // public method that returns a class and has no parameters
        _contextClassTypeMock.addPublicMethod(new JavaMethodMock("method1", new JavaClassTypeMock("method1returntype"), new ArrayList<>()));

        // public method that returns a class and has one parameter
        _contextClassTypeMock
                .addPublicMethod(new JavaMethodMock("method2", new JavaClassTypeMock("method2returntype")).addParameter(new MethodParameterMock("param1", new JavaClassTypeMock("Param1"))));

        // public method that returns void nd has no parameters
        _contextClassTypeMock.addPublicMethod(new JavaMethodMock("method3", null));
    }

    @Test
    public void can_resolve() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, _contextClassTypeMock, "prop:method1()", null);

        replay();

        assert _singleMethodResolver.execute(_valueResolverContext);

        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("method1returntype");

        assert ((IJavaMethod) _valueResolverContext.getResultCodeBind()).getName().equals("method1");
    }

    @Test
    public void cant_resolve() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, _contextClassTypeMock, "prop:methodthatdoesnexist()", null);

        replay();

        assert _singleMethodResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, _contextClassTypeMock, "prop:method2()", null);

        replay();

        assert _singleMethodResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, _contextClassTypeMock, "prop:method3()", null);

        replay();

        assert _singleMethodResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, _contextClassTypeMock, "prop:Method1()", null);

        replay();

        assert _singleMethodResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;
    }
}
