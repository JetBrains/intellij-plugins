package com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases;

import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import com.intellij.tapestry.core.model.presentation.valueresolvers.property.AbstractSpecialCaseTest;
import com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases.SpecialCaseBooleanResolver;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class SpecialCaseBooleanResolverTest extends AbstractSpecialCaseTest {
    private SpecialCaseBooleanResolver _specialCaseBooleanResolver;

    @BeforeTest
    public void init() {
        _specialCaseBooleanResolver = new SpecialCaseBooleanResolver();
    }

    @Test
    public void can_resolve() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "true", null);
        expectToFindJavaType("java.lang.Boolean", new JavaClassTypeMock("java.lang.Boolean"));

        replay();

        assert _specialCaseBooleanResolver.execute(_valueResolverContext);
        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("java.lang.Boolean");

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "prop:false", null);
        expectToFindJavaType("java.lang.Boolean", new JavaClassTypeMock("java.lang.Boolean"));

        replay();

        assert _specialCaseBooleanResolver.execute(_valueResolverContext);
        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("java.lang.Boolean");

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, " FALSE ", null);
        expectToFindJavaType("java.lang.Boolean", new JavaClassTypeMock("java.lang.Boolean"));

        replay();

        assert _specialCaseBooleanResolver.execute(_valueResolverContext);
        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("java.lang.Boolean");
    }

    @Test
    public void cant_resolve() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "true1", null);

        replay();

        assert !_specialCaseBooleanResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;
    }
}
