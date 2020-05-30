package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.model.TapestryLibrary;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.core.resource.IResourceFinder;
import com.intellij.tapestry.core.resource.TestableResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class ComponentTest {

    private JavaClassTypeMock _classInRootComponentsPackageMock;
    private TapestryProject _tapestryProjectMock;
    private IResourceFinder _resourceFinderMock;
    private TapestryLibrary _libraryMock;

    @BeforeMethod
    public void initMocks() {
        File builderClassFileMock = createMock(File.class);
        expect(builderClassFileMock.lastModified()).andReturn(Long.MAX_VALUE).anyTimes();
        replay(builderClassFileMock);

        IResource builderClassResourceMock = createMock(IResource.class);
        expect(builderClassResourceMock.getFile()).andReturn(builderClassFileMock).anyTimes();
        replay((builderClassResourceMock));

        _classInRootComponentsPackageMock = new JavaClassTypeMock("com.app.components.SomeClass").setPublic(true).setDefaultConstructor(true).setFile(builderClassResourceMock);

        _resourceFinderMock = createMock(IResourceFinder.class);
        _tapestryProjectMock = org.easymock.EasyMock.createMock(TapestryProject.class);
        org.easymock.EasyMock.expect(_tapestryProjectMock.getApplicationRootPackage()).andReturn("com.app").anyTimes();
        org.easymock.EasyMock.expect(_tapestryProjectMock.getResourceFinder()).andReturn(_resourceFinderMock).anyTimes();

        _libraryMock = org.easymock.EasyMock.createMock(TapestryLibrary.class);
        org.easymock.EasyMock.expect(_libraryMock.getBasePackage()).andReturn("com.app").anyTimes();
        org.easymock.EasyMock.expect(_libraryMock.getId()).andReturn("application").anyTimes();


        org.easymock.EasyMock.replay(_tapestryProjectMock, _libraryMock);
    }

    @Test
    public void getTemplate_no_template() {
        expect(_resourceFinderMock.findLocalizedClasspathResource("com/app/components/SomeClass.tml", true)).andReturn(new ArrayList<>()).anyTimes();
        replay(_resourceFinderMock);

        assertEquals(new TapestryComponent(_libraryMock, _classInRootComponentsPackageMock, _tapestryProjectMock).getTemplate().length, 0);

        reset(_resourceFinderMock);
        expect(_resourceFinderMock.findLocalizedClasspathResource("com/app/components/SomeClass.tml", true)).andReturn(new ArrayList<>()).anyTimes();
        expect(_resourceFinderMock.findLocalizedContextResource("SomeClass.tml")).andReturn(new ArrayList<>()).anyTimes();
        replay(_resourceFinderMock);

        assertEquals(new TapestryComponent(_libraryMock, _classInRootComponentsPackageMock, _tapestryProjectMock).getTemplate().length, 0);
    }

    @Test
    public void getTemplate_template_in_classpath() {
        Collection<IResource> web1 = new ArrayList<>();
        web1.add(new TestableResource("SomeClass.tml", "web1.xml"));
        expect(_resourceFinderMock.findLocalizedClasspathResource("com/app/components/SomeClass.tml", true)).andReturn(web1).anyTimes();

        Collection<IResource> templates = new ArrayList<>();
        templates.add(new TestableResource("SomeClass.tml", "web2.xml"));
        expect(_resourceFinderMock.findLocalizedContextResource("SomeClass.tml")).andReturn(templates).anyTimes();
        replay(_resourceFinderMock);

        assertEquals(
          new TapestryComponent(_libraryMock, _classInRootComponentsPackageMock, _tapestryProjectMock).getTemplate()[0].getName(),
          "SomeClass.tml");
    }

    @Test
    public void allowsTemplate() {
        assertTrue(new TapestryComponent(_libraryMock, _classInRootComponentsPackageMock, _tapestryProjectMock).allowsTemplate());
    }
}
