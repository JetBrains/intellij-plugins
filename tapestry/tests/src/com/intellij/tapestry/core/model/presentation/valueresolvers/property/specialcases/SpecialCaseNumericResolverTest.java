package com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases;

import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import com.intellij.tapestry.core.model.presentation.valueresolvers.property.AbstractSpecialCaseTest;
import com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases.SpecialCaseNumericResolver;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class SpecialCaseNumericResolverTest extends AbstractSpecialCaseTest {
    private SpecialCaseNumericResolver _specialCaseNumericResolver;

    @BeforeTest
    public void init() {
        _specialCaseNumericResolver = new SpecialCaseNumericResolver();
    }

    @Test
    public void can_resolve_long() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "prop: 1 ", null);
        expectToFindJavaType("java.lang.Long", new JavaClassTypeMock("java.lang.Long"));

        replay();

        assert _specialCaseNumericResolver.execute(_valueResolverContext);
        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("java.lang.Long");

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, " 12345 ", null);
        expectToFindJavaType("java.lang.Long", new JavaClassTypeMock("java.lang.Long"));

        replay();

        assert _specialCaseNumericResolver.execute(_valueResolverContext);
        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("java.lang.Long");
    }

    @Test
    public void can_resolve_double() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "prop: 1.1 ", null);
        expectToFindJavaType("java.lang.Double", new JavaClassTypeMock("java.lang.Double"));

        replay();

        assert _specialCaseNumericResolver.execute(_valueResolverContext);
        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("java.lang.Double");

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, " 1,5 ", null);
        expectToFindJavaType("java.lang.Double", new JavaClassTypeMock("java.lang.Double"));

        replay();

        assert _specialCaseNumericResolver.execute(_valueResolverContext);
        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("java.lang.Double");
    }

    @Test
    public void cant_resolve_long() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "a", null);

        replay();

        assert !_specialCaseNumericResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "1,1.1", null);

        replay();

        assert !_specialCaseNumericResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "1t1", null);

        replay();

        assert !_specialCaseNumericResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;
    }
}
