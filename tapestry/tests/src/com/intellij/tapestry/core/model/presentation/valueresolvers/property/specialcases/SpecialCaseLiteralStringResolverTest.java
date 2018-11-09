package com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases;

import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import com.intellij.tapestry.core.model.presentation.valueresolvers.property.AbstractSpecialCaseTest;
import com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases.SpecialCaseLiteralStringResolver;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class SpecialCaseLiteralStringResolverTest extends AbstractSpecialCaseTest {
    private SpecialCaseLiteralStringResolver _specialCaseLiteralStringResolver;

    @BeforeTest
    public void init() {
        _specialCaseLiteralStringResolver = new SpecialCaseLiteralStringResolver();
    }

    @Test
    public void can_resolve() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "prop:'hey'", null);
        expectToFindJavaType("java.lang.String", new JavaClassTypeMock("java.lang.String"));

        replay();

        assert _specialCaseLiteralStringResolver.execute(_valueResolverContext);
        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("java.lang.String");

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, " ' hey ' ", null);
        expectToFindJavaType("java.lang.String", new JavaClassTypeMock("java.lang.String"));

        replay();

        assert _specialCaseLiteralStringResolver.execute(_valueResolverContext);
        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("java.lang.String");
    }

    @Test
    public void cant_resolve() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "1", null);

        replay();

        assert !_specialCaseLiteralStringResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "1'hey'", null);

        replay();

        assert !_specialCaseLiteralStringResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;
    }
}
