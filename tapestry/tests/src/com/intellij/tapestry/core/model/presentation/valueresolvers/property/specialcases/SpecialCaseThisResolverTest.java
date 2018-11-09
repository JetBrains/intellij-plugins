package com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases;

import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import com.intellij.tapestry.core.model.presentation.valueresolvers.property.AbstractSpecialCaseTest;
import com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases.SpecialCaseThisResolver;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class SpecialCaseThisResolverTest extends AbstractSpecialCaseTest {
    private SpecialCaseThisResolver _specialCaseThisResolver;

    @BeforeTest
    public void init() {
        _specialCaseThisResolver = new SpecialCaseThisResolver();
    }

    @Test
    public void can_resolve() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, new JavaClassTypeMock("myComponentName"), "prop:this", null);

        replay();

        assert _specialCaseThisResolver.execute(_valueResolverContext);
        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("myComponentName");

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, new JavaClassTypeMock("myComponentName"), " THIS ", null);

        replay();

        assert _specialCaseThisResolver.execute(_valueResolverContext);
        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("myComponentName");
    }

    @Test
    public void cant_resolve() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "this1", null);

        replay();

        assert !_specialCaseThisResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;
    }
}
