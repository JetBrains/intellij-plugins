package com.intellij.tapestry.core.model.ioc;

import com.intellij.tapestry.core.java.IJavaMethod;
import com.intellij.tapestry.core.mocks.JavaAnnotationMock;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.resource.IResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static org.easymock.EasyMock.*;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class ModuleBuilderTest {

    private JavaClassTypeMock _builderClassWithBuildMethodsMock;
    private JavaClassTypeMock _returnTypeMock;
    private IJavaMethod _buildMethodMock;
    private IJavaMethod _buildMethodWithSuffixMock;

    @BeforeMethod
    public void initMocks() {
        File builderClassFileMock = org.easymock.EasyMock.createMock(File.class);
        org.easymock.EasyMock.expect(builderClassFileMock.lastModified()).andReturn(Long.MAX_VALUE);
        org.easymock.EasyMock.replay(builderClassFileMock);

        IResource builderClassResourceMock = createMock(IResource.class);
        expect(builderClassResourceMock.getFile()).andReturn(builderClassFileMock);
        replay((builderClassResourceMock));

        _builderClassWithBuildMethodsMock = new JavaClassTypeMock().setFile(builderClassResourceMock);

        _returnTypeMock = new JavaClassTypeMock("MyService");

        _buildMethodMock = createMock(IJavaMethod.class);
        expect(_buildMethodMock.getName()).andReturn("build").anyTimes();
        expect(_buildMethodMock.getParameters()).andReturn(new ArrayList<>()).anyTimes();
        expect(_buildMethodMock.getReturnType()).andReturn(_returnTypeMock).anyTimes();
        expect(_buildMethodMock.getContainingClass()).andReturn(_builderClassWithBuildMethodsMock).anyTimes();

        _buildMethodWithSuffixMock = createMock(IJavaMethod.class);
        expect(_buildMethodWithSuffixMock.getName()).andReturn("buildSomeService").anyTimes();
        expect(_buildMethodWithSuffixMock.getParameters()).andReturn(new ArrayList<>()).anyTimes();
        expect(_buildMethodWithSuffixMock.getReturnType()).andReturn(_returnTypeMock).anyTimes();
        expect(_buildMethodWithSuffixMock.getContainingClass()).andReturn(_builderClassWithBuildMethodsMock).anyTimes();
    }

    @Test
    public void getServices_default_service_build_no_suffix() {
        expect(_buildMethodMock.getAnnotation(startsWith("org.apache.tapestry5.ioc.annotations"))).andReturn(null).anyTimes();
        replay(_buildMethodMock);

        _builderClassWithBuildMethodsMock.addPublicMethod(_buildMethodMock);

        Collection<Service> services = new ModuleBuilder(_builderClassWithBuildMethodsMock, null).getServices();

        assert services.size() == 1;

        assert ((Service) services.toArray()[0]).getId().equals(_returnTypeMock.getName());
    }

    @Test
    public void getServices_default_service_build_no_suffix_and_annotations() {
        JavaAnnotationMock scopeAnnotationMock = new JavaAnnotationMock().addParameter("value", "myscope");

        JavaAnnotationMock eagerLoadAnnotationMock = new JavaAnnotationMock();

        expect(_buildMethodMock.getAnnotation(matches("org.apache.tapestry5.ioc.annotations.Scope"))).andReturn(scopeAnnotationMock).anyTimes();
        expect(_buildMethodMock.getAnnotation(matches("org.apache.tapestry5.ioc.annotations.EagerLoad"))).andReturn(eagerLoadAnnotationMock).anyTimes();
        replay(_buildMethodMock);

        _builderClassWithBuildMethodsMock.addPublicMethod(_buildMethodMock);

        Collection<Service> services = new ModuleBuilder(_builderClassWithBuildMethodsMock, null).getServices();

        assert services.size() == 1;

        assert ((Service) services.toArray()[0]).getId().equals(_returnTypeMock.getName());

        assert ((Service) services.toArray()[0]).getScope().equals("myscope");

        assert ((Service) services.toArray()[0]).isEagerLoad();
    }

    @Test
    public void getServices_default_service_build_with_suffix() {
        expect(_buildMethodWithSuffixMock.getAnnotation(startsWith("org.apache.tapestry5.ioc.annotations"))).andReturn(null).anyTimes();
        replay(_buildMethodWithSuffixMock);

        _builderClassWithBuildMethodsMock.addPublicMethod(_buildMethodWithSuffixMock);

        Collection<Service> services = new ModuleBuilder(_builderClassWithBuildMethodsMock, null).getServices();

        assert services.size() == 1;

        assert ((Service) services.toArray()[0]).getId().equals("SomeService");
    }

    @Test
    public void getServices_default_service_build_with_suffix_and_annotations() {
        JavaAnnotationMock scopeAnnotationMock = new JavaAnnotationMock().addParameter("value", "myscope");

        JavaAnnotationMock eagerLoadAnnotationMock = new JavaAnnotationMock();

        expect(_buildMethodWithSuffixMock.getAnnotation(matches("org.apache.tapestry5.ioc.annotations.Scope"))).andReturn(scopeAnnotationMock).anyTimes();
        expect(_buildMethodWithSuffixMock.getAnnotation(matches("org.apache.tapestry5.ioc.annotations.EagerLoad"))).andReturn(eagerLoadAnnotationMock).anyTimes();
        replay(_buildMethodWithSuffixMock);

        _builderClassWithBuildMethodsMock.addPublicMethod(_buildMethodWithSuffixMock);

        Collection<Service> services = new ModuleBuilder(_builderClassWithBuildMethodsMock, null).getServices();

        assert services.size() == 1;

        assert ((Service) services.toArray()[0]).getId().equals("SomeService");

        assert ((Service) services.toArray()[0]).getScope().equals("myscope");

        assert ((Service) services.toArray()[0]).isEagerLoad();
    }
}
