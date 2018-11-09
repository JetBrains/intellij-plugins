package com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases;

import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import com.intellij.tapestry.core.model.presentation.valueresolvers.property.AbstractSpecialCaseTest;
import com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases.SpecialCaseRangeIntegersResolver;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class SpecialCaseRangeIntegersResolverTest extends AbstractSpecialCaseTest {
    private SpecialCaseRangeIntegersResolver _specialCaseRangeIntegersResolver;

    @BeforeTest
    public void init() {
        _specialCaseRangeIntegersResolver = new SpecialCaseRangeIntegersResolver();
    }

    @Test
    public void can_resolve() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "prop:1..2", null);
        expectToFindJavaType("java.lang.Iterable", new JavaClassTypeMock("java.lang.Iterable"));

        replay();

        assert _specialCaseRangeIntegersResolver.execute(_valueResolverContext);
        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("java.lang.Iterable");

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, " 12..56 ", null);
        expectToFindJavaType("java.lang.Iterable", new JavaClassTypeMock("java.lang.Iterable"));

        replay();

        assert _specialCaseRangeIntegersResolver.execute(_valueResolverContext);
        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("java.lang.Iterable");
    }

    @Test
    public void cant_resolve() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "1", null);

        replay();

        assert !_specialCaseRangeIntegersResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "1...2", null);

        replay();

        assert !_specialCaseRangeIntegersResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "1,,2", null);

        replay();

        assert !_specialCaseRangeIntegersResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;
    }
}
