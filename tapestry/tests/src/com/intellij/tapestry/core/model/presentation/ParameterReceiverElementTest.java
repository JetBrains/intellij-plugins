package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.mocks.JavaAnnotationMock;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.mocks.JavaFieldMock;
import com.intellij.tapestry.core.model.TapestryLibrary;
import com.intellij.tapestry.core.resource.IResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.easymock.EasyMock.*;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class ParameterReceiverElementTest {

    private JavaClassTypeMock _classInSubComponentsPackageMock;
    private JavaClassTypeMock _classInRootComponentsPackageMock;
    private TapestryLibrary _libraryMock;
    private TapestryProject _tapestryProjectMock;

    @BeforeMethod
    public void initMocks() {
        File builderClassFileMock = org.easymock.EasyMock.createMock(File.class);
        org.easymock.EasyMock.expect(builderClassFileMock.lastModified()).andReturn(Long.MAX_VALUE).anyTimes();
        org.easymock.EasyMock.replay(builderClassFileMock);

        IResource builderClassResourceMock = createMock(IResource.class);
        expect(builderClassResourceMock.getFile()).andReturn(builderClassFileMock).anyTimes();
        replay((builderClassResourceMock));

        _classInRootComponentsPackageMock = new JavaClassTypeMock("com.app.components.SomeClass").setPublic(true).setDefaultConstructor(true).setFile(builderClassResourceMock);

        _classInSubComponentsPackageMock = new JavaClassTypeMock("com.app.components.folder1.SomeClass").setPublic(true).setDefaultConstructor(true).setFile(builderClassResourceMock);

        _tapestryProjectMock = org.easymock.EasyMock.createMock(TapestryProject.class);
        org.easymock.EasyMock.expect(_tapestryProjectMock.getApplicationRootPackage()).andReturn("com.app").anyTimes();
        org.easymock.EasyMock.expect(_tapestryProjectMock.getResourceFinder()).andReturn(null).anyTimes();

        _libraryMock = org.easymock.EasyMock.createMock(TapestryLibrary.class);
        org.easymock.EasyMock.expect(_libraryMock.getBasePackage()).andReturn("com.app").anyTimes();
        org.easymock.EasyMock.expect(_libraryMock.getId()).andReturn("application").anyTimes();


        org.easymock.EasyMock.replay(_tapestryProjectMock, _libraryMock);
    }

    @Test
    public void getParameters_no_parameters() {
        assert new TapestryComponent(_libraryMock, _classInRootComponentsPackageMock, _tapestryProjectMock).getParameters().size() == 0;

        JavaFieldMock publicField = new JavaFieldMock("publicField", false).addAnnotation(new JavaAnnotationMock("org.apache.tapestry5.annotations.Parameter"));
        JavaFieldMock privateField = new JavaFieldMock("privateField", true);

        _classInSubComponentsPackageMock.addField(publicField).addField(privateField);

        assert new TapestryComponent(_libraryMock, _classInSubComponentsPackageMock, _tapestryProjectMock).getParameters().size() == 0;
    }

    @Test
    public void getParameters_with_parameters() {
        JavaFieldMock privateField = new JavaFieldMock("field1", true).addAnnotation(new JavaAnnotationMock("org.apache.tapestry5.annotations.Parameter"));

        _classInSubComponentsPackageMock.addField(privateField);

        assert new TapestryComponent(_libraryMock, _classInSubComponentsPackageMock, _tapestryProjectMock).getParameters().size() == 1;
    }

    @Test
    public void getRequiredParameters_no_parameters() {
        JavaFieldMock privateField = new JavaFieldMock("field1", true).addAnnotation(new JavaAnnotationMock("org.apache.tapestry5.annotations.Parameter"));

        _classInSubComponentsPackageMock.addField(privateField);

        assert new TapestryComponent(_libraryMock, _classInSubComponentsPackageMock, _tapestryProjectMock).getRequiredParameters().size() == 0;
    }

    @Test
    public void getRequiredParameters_with_parameters() {
        JavaFieldMock privateField = new JavaFieldMock("field1", true)
                .addAnnotation(new JavaAnnotationMock("org.apache.tapestry5.annotations.Parameter").addParameter("required", "true"));

        _classInSubComponentsPackageMock.addField(privateField);

        assert new TapestryComponent(_libraryMock, _classInSubComponentsPackageMock, _tapestryProjectMock).getRequiredParameters().size() == 1;
    }

    @Test
    public void getOptionalParameters_with_parameters() {
        JavaFieldMock privateField = new JavaFieldMock("field1", true).addAnnotation(new JavaAnnotationMock("org.apache.tapestry5.annotations.Parameter"));

        _classInSubComponentsPackageMock.addField(privateField);

        assert new TapestryComponent(_libraryMock, _classInSubComponentsPackageMock, _tapestryProjectMock).getOptionalParameters().size() == 1;
    }

    @Test
    public void getOptionalParameters_no_parameters() {
        JavaFieldMock privateField = new JavaFieldMock("field1", true)
                .addAnnotation(new JavaAnnotationMock("org.apache.tapestry5.annotations.Parameter").addParameter("required", "true"));

        _classInSubComponentsPackageMock.addField(privateField);

        assert new TapestryComponent(_libraryMock, _classInSubComponentsPackageMock, _tapestryProjectMock).getOptionalParameters().size() == 0;
    }
}
