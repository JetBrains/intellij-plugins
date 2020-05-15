package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.mocks.JavaAnnotationMock;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.mocks.JavaFieldMock;
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
import static org.testng.Assert.*;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class PresentationLibraryElementTest {

    private JavaClassTypeMock _classInBasePackageMock;
    private JavaClassTypeMock _classInSomePackageMock;
    private JavaClassTypeMock _classInComponentsPackageNotPublicMock;
    private JavaClassTypeMock _classInComponentsPackageNoDefaultConstructorMock;
    private JavaClassTypeMock _classInRootComponentsPackageMock;
    private JavaClassTypeMock _classInRootMixinsPackageMock;
    private JavaClassTypeMock _classInSubComponentsPackageMock;
    private JavaClassTypeMock _classInRootPagesPackageMock;
    private JavaClassTypeMock _rootComponentClassMock;
    private TapestryProject _tapestryProjectMock;
    private IResourceFinder _resourceFinderMock;
    private TapestryLibrary _libraryMock;
    private TapestryLibrary _libraryNoRootPackageMock;

    @BeforeMethod
    public void initMocks() {
        File builderClassFileMock = createMock(File.class);
        expect(builderClassFileMock.lastModified()).andReturn(Long.MAX_VALUE).anyTimes();
        replay(builderClassFileMock);

        IResource builderClassResourceMock = createMock(IResource.class);
        expect(builderClassResourceMock.getFile()).andReturn(builderClassFileMock).anyTimes();
        replay((builderClassResourceMock));

        _classInBasePackageMock = new JavaClassTypeMock("com.app.SomeClass").setPublic(true).setDefaultConstructor(true).setFile(builderClassResourceMock);

        _classInComponentsPackageNotPublicMock = new JavaClassTypeMock("com.app.components.SomeClass").setPublic(false).setDefaultConstructor(true).setFile(builderClassResourceMock);

        _classInComponentsPackageNoDefaultConstructorMock = new JavaClassTypeMock("com.app.components.SomeClass").setPublic(true).setDefaultConstructor(false).setFile(builderClassResourceMock);

        _rootComponentClassMock = new JavaClassTypeMock("com.app.components.SomeClass").setPublic(true).setDefaultConstructor(true).setFile(builderClassResourceMock);

        _classInSomePackageMock = new JavaClassTypeMock("com.app.test.components.SomeClass").setPublic(true).setDefaultConstructor(true).setFile(builderClassResourceMock);

        _classInRootComponentsPackageMock = new JavaClassTypeMock("com.app.components.SomeClass").setPublic(true).setDefaultConstructor(true).setFile(builderClassResourceMock);

        _classInRootMixinsPackageMock = new JavaClassTypeMock("com.app.mixins.SomeClass").setPublic(true).setDefaultConstructor(true).setFile(builderClassResourceMock);

        _classInRootPagesPackageMock = new JavaClassTypeMock("com.app.pages.SomeClass").setPublic(true).setDefaultConstructor(true).setFile(builderClassResourceMock);

        _classInSubComponentsPackageMock = new JavaClassTypeMock("com.app.components.folder1.SomeClass").setPublic(true).setDefaultConstructor(true).setFile(builderClassResourceMock);

        _resourceFinderMock = createMock(IResourceFinder.class);
        _tapestryProjectMock = createMock(TapestryProject.class);
        expect(_tapestryProjectMock.getApplicationRootPackage()).andReturn("com.app").anyTimes();
        expect(_tapestryProjectMock.getResourceFinder()).andReturn(_resourceFinderMock).anyTimes();

        _libraryMock = createMock(TapestryLibrary.class);
        expect(_libraryMock.getBasePackage()).andReturn("com.app").anyTimes();
        expect(_libraryMock.getId()).andReturn("application").anyTimes();

        _libraryNoRootPackageMock = createMock(TapestryLibrary.class);
        expect(_libraryNoRootPackageMock.getBasePackage()).andReturn(null).anyTimes();
        expect(_libraryNoRootPackageMock.getId()).andReturn("id").anyTimes();

        replay(_tapestryProjectMock, _libraryMock, _libraryNoRootPackageMock);
    }

    @Test
    public void isValidElement_outside_base_package() {
        try {
            new TestableParameterReceiverElement(_libraryMock, _classInBasePackageMock, _tapestryProjectMock);
        } catch (NotTapestryElementException ex) {
            try {
                new TestableParameterReceiverElement(_libraryMock, _classInSomePackageMock, _tapestryProjectMock);
            } catch (NotTapestryElementException ex2) {
                return;
            }
        }

        fail();
    }

    @Test
    public void isValidElement_not_public() {
        try {
            new TestableParameterReceiverElement(_libraryMock, _classInComponentsPackageNotPublicMock, _tapestryProjectMock);
        } catch (NotTapestryElementException ex) {
            return;
        }

        fail();
    }

    @Test
    public void isValidElement_no_default_constructor() {
        try {
            new TestableParameterReceiverElement(_libraryMock, _classInComponentsPackageNoDefaultConstructorMock, _tapestryProjectMock);
        } catch (NotTapestryElementException ex) {
            return;
        }

        fail();
    }

    @Test
    public void isValidElement_no_app_root_package() {
        try {
            new TestableParameterReceiverElement(_libraryNoRootPackageMock, _rootComponentClassMock, _tapestryProjectMock);
        } catch (NotTapestryElementException ex) {
            return;
        }

        fail();
    }

    @Test
    public void isValidElement_valid() {
        new TestableParameterReceiverElement(_libraryMock, _classInRootComponentsPackageMock, _tapestryProjectMock);

        new TestableParameterReceiverElement(_libraryMock, _classInSubComponentsPackageMock, _tapestryProjectMock);

        new TestableParameterReceiverElement(_libraryMock, _classInRootPagesPackageMock, _tapestryProjectMock);
    }

    @Test
    public void getElementNameFromClass() {
        assertEquals(new TestableParameterReceiverElement(_libraryMock, _classInRootComponentsPackageMock, _tapestryProjectMock).getName(),
                     "SomeClass");

        assertEquals(new TestableParameterReceiverElement(_libraryMock, _classInRootPagesPackageMock, _tapestryProjectMock).getName(),
                     "SomeClass");

        assertEquals(new TestableParameterReceiverElement(_libraryMock, _classInSubComponentsPackageMock, _tapestryProjectMock).getName(),
                     "folder1/SomeClass");
    }

    @Test
    public void getParameters_no_parameters() {
        assertEquals(
          new TestableParameterReceiverElement(_libraryMock, _classInRootComponentsPackageMock, _tapestryProjectMock).getParameters()
            .size(), 0);

        JavaFieldMock publicField = new JavaFieldMock("publicField", false).addAnnotation(new JavaAnnotationMock("org.apache.tapestry5.annotations.Parameter"));
        JavaFieldMock privateField = new JavaFieldMock("privateField", true);

        _classInSubComponentsPackageMock.addField(publicField).addField(privateField);

        assertEquals(
          new TestableParameterReceiverElement(_libraryMock, _classInSubComponentsPackageMock, _tapestryProjectMock).getParameters().size(),
          0);
    }

    @Test
    public void getParameters_with_parameters() {
        JavaFieldMock privateField = new JavaFieldMock("field1", true).addAnnotation(new JavaAnnotationMock("org.apache.tapestry5.annotations.Parameter"));

        _classInSubComponentsPackageMock.addField(privateField);

        assertEquals(
          new TestableParameterReceiverElement(_libraryMock, _classInSubComponentsPackageMock, _tapestryProjectMock).getParameters().size(),
          1);
    }

    @Test
    public void getElementClass() {
        assertEquals(
          new TestableParameterReceiverElement(_libraryMock, _classInRootComponentsPackageMock, _tapestryProjectMock).getElementClass()
            .getFullyQualifiedName(), "com.app.components.SomeClass");
    }

    @Test
    public void getDocumentation() {
        _classInRootComponentsPackageMock.setDocumentation("docs");

        assertEquals(
          new TestableParameterReceiverElement(_libraryMock, _classInRootComponentsPackageMock, _tapestryProjectMock).getDescription(),
          "docs");
    }

    @Test
    public void createElementInstance_component() {
        assertTrue(PresentationLibraryElement.createElementInstance(_libraryMock, _classInRootComponentsPackageMock,
                                                                    _tapestryProjectMock) instanceof TapestryComponent);
    }

    @Test
    public void createElementInstance_page() {
        assertTrue(PresentationLibraryElement
                     .createElementInstance(_libraryMock, _classInRootPagesPackageMock, _tapestryProjectMock) instanceof Page);
    }

    @Test
    public void createElementInstance_mixin() {
        assertTrue(PresentationLibraryElement
                     .createElementInstance(_libraryMock, _classInRootMixinsPackageMock, _tapestryProjectMock) instanceof Mixin);
    }

    @Test
    public void checkAllValidResources() {
        IResource resource1 = new TestableResource("web.xml", "web1.xml");
        IResource resource2 = new TestableResource("web.xml", "web2.xml");
        IResource resource3 = new TestableResource("web.xml", "idontexist1.xml");
        IResource resource4 = new TestableResource("web.xml", "idontexist2.xml");

        assertTrue(PresentationLibraryElement.checkAllValidResources(new IResource[]{resource1, resource2}));

        assertFalse(PresentationLibraryElement.checkAllValidResources(new IResource[]{resource3, resource4}));

        assertFalse(PresentationLibraryElement.checkAllValidResources(new IResource[]{resource1, resource2, resource3}));
    }

    @Test
    public void getMessageCatalog() {
        Collection<IResource> resources1 = new ArrayList<>();
        resources1.add(new TestableResource("SomeClass.properties", "SomeClass.properties"));
        resources1.add(new TestableResource("SomeClass_pt.properties", "SomeClass_pt.properties"));
        expect(_resourceFinderMock.findLocalizedClasspathResource("com/app/pages/SomeClass.properties", true)).andReturn(resources1).anyTimes();

        Collection<IResource> resources2 = new ArrayList<>();
        resources2.add(new TestableResource("SomeClass.properties", "SomeClass.properties"));
        resources2.add(new TestableResource("SomeClass_pt.properties", "SomeClass_pt.properties"));
        expect(_resourceFinderMock.findLocalizedClasspathResource("com/app/components/folder1/SomeClass.properties", true)).andReturn(resources2).anyTimes();

        replay(_resourceFinderMock);

        assertEquals(new Page(_libraryMock, _classInRootPagesPackageMock, _tapestryProjectMock).getMessageCatalog().length, 2);

        resources1.clear();

        assertEquals(new TapestryComponent(_libraryMock, _classInSubComponentsPackageMock, _tapestryProjectMock).getMessageCatalog().length,
                     2);

        resources1.clear();

        assertEquals(new Page(_libraryMock, _classInRootPagesPackageMock, _tapestryProjectMock).getMessageCatalog().length, 0);
    }
}

class TestableParameterReceiverElement extends ParameterReceiverElement {

    TestableParameterReceiverElement(TapestryLibrary library, IJavaClassType elementClass, TapestryProject project) {
        super(library, elementClass, project);
    }

    @Override
    public boolean allowsTemplate() {
        return false;
    }

    @Override
    public IResource[] getTemplate() {
        return null;
    }

    @Override
    public IResource[] getMessageCatalog() {
        return IResource.EMPTY_ARRAY;
    }
}
