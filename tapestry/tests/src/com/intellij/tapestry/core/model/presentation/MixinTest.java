package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.model.TapestryLibrary;
import com.intellij.tapestry.core.resource.IResourceFinder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.createMock;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class MixinTest {

    private JavaClassTypeMock _classInRootPagesPackageMock;
    private TapestryProject _tapestryProjectMock;
    private TapestryLibrary _libraryMock;

    @BeforeMethod
    public void initMocks() {
        _classInRootPagesPackageMock = new JavaClassTypeMock("com.app.pages.SomeClass").setPublic(true).setDefaultConstructor(true);

        IResourceFinder resourceFinderMock = createMock(IResourceFinder.class);
        _tapestryProjectMock = org.easymock.EasyMock.createMock(TapestryProject.class);
        org.easymock.EasyMock.expect(_tapestryProjectMock.getApplicationRootPackage()).andReturn("com.app").anyTimes();
        org.easymock.EasyMock.expect(_tapestryProjectMock.getResourceFinder()).andReturn(resourceFinderMock).anyTimes();

        _libraryMock = org.easymock.EasyMock.createMock(TapestryLibrary.class);
        org.easymock.EasyMock.expect(_libraryMock.getBasePackage()).andReturn("com.app").anyTimes();
        org.easymock.EasyMock.expect(_libraryMock.getId()).andReturn("application").anyTimes();

        org.easymock.EasyMock.replay(_tapestryProjectMock, _libraryMock);
    }

    @Test
    public void allowsTemplate() {
        assert !new Mixin(_libraryMock, _classInRootPagesPackageMock, _tapestryProjectMock).allowsTemplate();
    }

    @Test
    public void getTemplate() {
        assert new Mixin(_libraryMock, _classInRootPagesPackageMock, _tapestryProjectMock).getTemplate().length == 0;
    }

    @Test
    public void getMessageCatalog() {
        assert new Mixin(_libraryMock, _classInRootPagesPackageMock, _tapestryProjectMock).getMessageCatalog().length == 0;
    }
}
