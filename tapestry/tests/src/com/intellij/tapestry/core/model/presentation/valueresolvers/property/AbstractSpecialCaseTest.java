package com.intellij.tapestry.core.model.presentation.valueresolvers.property;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaTypeFinder;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import org.testng.annotations.BeforeMethod;

/**
 * Base class for all special case test classes.
 */
public abstract class AbstractSpecialCaseTest {
    protected TapestryProject _tapestryProjectMock;
    protected IJavaTypeFinder _javaTypeFinderMock;
    protected ValueResolverContext _valueResolverContext;

    @BeforeMethod
    public void initAbstractSpecialCaseTest() {
        _tapestryProjectMock = org.easymock.EasyMock.createMock(TapestryProject.class);
        _javaTypeFinderMock = org.easymock.EasyMock.createMock(IJavaTypeFinder.class);

        org.easymock.EasyMock.expect(_tapestryProjectMock.getJavaTypeFinder()).andReturn(_javaTypeFinderMock).anyTimes();
    }

    protected void replay() {
        org.easymock.EasyMock.replay(_tapestryProjectMock);
        org.easymock.EasyMock.replay(_javaTypeFinderMock);
    }

    protected void reset() {
        org.easymock.EasyMock.reset(_tapestryProjectMock);
        org.easymock.EasyMock.reset(_javaTypeFinderMock);

        org.easymock.EasyMock.expect(_tapestryProjectMock.getJavaTypeFinder()).andReturn(_javaTypeFinderMock).anyTimes();
    }

    protected void expectToFindJavaType(String type, IJavaClassType returnValue) {
        org.easymock.EasyMock.expect(_javaTypeFinderMock.findType(type, true)).andReturn(returnValue);
    }
}
