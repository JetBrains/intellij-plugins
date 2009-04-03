package com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases;

import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import com.intellij.tapestry.core.model.presentation.valueresolvers.property.AbstractSpecialCaseTest;
import com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases.SpecialCaseNullResolver;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class SpecialCaseNullResolverTest extends AbstractSpecialCaseTest {
    private SpecialCaseNullResolver _specialCaseNullResolver;

    @BeforeTest
    public void init() {
        _specialCaseNullResolver = new SpecialCaseNullResolver();
    }

    @Test
    public void can_resolve() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "prop:null", null);
        expectToFindJavaType("java.lang.Object", new JavaClassTypeMock("java.lang.Object"));

        replay();

        assert _specialCaseNullResolver.execute(_valueResolverContext);
        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("java.lang.Object");

        reset();

        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, " NULL ", null);
        expectToFindJavaType("java.lang.Object", new JavaClassTypeMock("java.lang.Object"));

        replay();

        assert _specialCaseNullResolver.execute(_valueResolverContext);
        assert ((IJavaClassType) _valueResolverContext.getResultType()).getFullyQualifiedName().equals("java.lang.Object");
    }

    @Test
    public void cant_resolve() throws Exception {
        _valueResolverContext = new ValueResolverContext(_tapestryProjectMock, null, "null1", null);

        replay();

        assert !_specialCaseNullResolver.execute(_valueResolverContext);
        assert _valueResolverContext.getResultType() == null;
    }
}
