package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.model.TapestryLibrary;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.core.resource.IResourceFinder;
import com.intellij.tapestry.core.resource.TestableResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.assertEquals;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class PageTest {

    private JavaClassTypeMock _classInRootPagesPackageMock;
    private TapestryProject _tapestryProjectMock;
    private IResourceFinder _resourceFinderMock;
    private TapestryLibrary _libraryMock;

    @BeforeMethod
    public void initMocks() {
        _classInRootPagesPackageMock = new JavaClassTypeMock("com.app.pages.SomeClass").setPublic(true).setDefaultConstructor(true);

        _resourceFinderMock = createMock(IResourceFinder.class);
        _tapestryProjectMock = createMock(TapestryProject.class);
        expect(_tapestryProjectMock.getApplicationRootPackage()).andReturn("com.app").anyTimes();
        expect(_tapestryProjectMock.getResourceFinder()).andReturn(_resourceFinderMock).anyTimes();

        _libraryMock = createMock(TapestryLibrary.class);
        expect(_libraryMock.getBasePackage()).andReturn("com.app").anyTimes();
        expect(_libraryMock.getId()).andReturn("application").anyTimes();

        replay(_tapestryProjectMock, _libraryMock);
    }

    @Test
    public void getTemplate_no_template() {
        expect(_resourceFinderMock.findLocalizedClasspathResource("com/app/pages/SomeClass.tml", true)).andReturn(new ArrayList<>()).anyTimes();
        expect(_resourceFinderMock.findLocalizedContextResource("SomeClass.tml")).andReturn(new ArrayList<>()).anyTimes();
        replay(_resourceFinderMock);

        assertEquals(new Page(_libraryMock, _classInRootPagesPackageMock, _tapestryProjectMock).getTemplate().length, 0);
    }

    @Test
    public void getTemplate_template_in_classpath() {
        Collection<IResource> web1 = new ArrayList<>();
        web1.add(new TestableResource("SomeClass.tml", "web1.xml"));
        expect(_resourceFinderMock.findLocalizedClasspathResource("com/app/pages/SomeClass.tml", true)).andReturn(web1).anyTimes();
        expect(_resourceFinderMock.findLocalizedContextResource("SomeClass.tml")).andReturn(new ArrayList<>()).anyTimes();
        replay(_resourceFinderMock);

        assert new Page(_libraryMock, _classInRootPagesPackageMock, _tapestryProjectMock).getTemplate()[0].getName().equals("SomeClass.tml");
    }

    @Test
    public void getTemplate_template_in_context() {
        expect(_resourceFinderMock.findLocalizedClasspathResource("com/app/pages/SomeClass.tml", true)).andReturn(new ArrayList<>()).anyTimes();

        Collection<IResource> templates = new ArrayList<>();
        templates.add(new TestableResource("SomeClass.tml", "web2.xml"));
        expect(_resourceFinderMock.findLocalizedContextResource("SomeClass.tml")).andReturn(templates).anyTimes();
        replay(_resourceFinderMock);

        assertEquals(new Page(_libraryMock, _classInRootPagesPackageMock, _tapestryProjectMock).getTemplate()[0].getName(),
                     "SomeClass.tml");
    }

    @Test
    public void getTemplate_template_in_both() {
        Collection<IResource> web1 = new ArrayList<>();
        web1.add(new TestableResource("SomeClass.tml", "web1.xml"));
        expect(_resourceFinderMock.findLocalizedClasspathResource("com/app/pages/SomeClass.tml", true)).andReturn(web1).anyTimes();
        expect(_resourceFinderMock.findLocalizedContextResource("SomeClass.tml")).andReturn(web1).anyTimes();
        replay(_resourceFinderMock);

        assertEquals(new Page(_libraryMock, _classInRootPagesPackageMock, _tapestryProjectMock).getTemplate().length, 2);
    }

    @Test
    public void allowsTemplate() {
        assert new Page(_libraryMock, _classInRootPagesPackageMock, _tapestryProjectMock).allowsTemplate();
    }
}
