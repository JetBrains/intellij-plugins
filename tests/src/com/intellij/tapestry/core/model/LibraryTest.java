package com.intellij.tapestry.core.model;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaTypeFinder;
import static org.easymock.EasyMock.*;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class LibraryTest {

    @Test
    public void constructor() {
        Library library = new Library("id", "basepackage", null);

        assert library.getId().equals("id");
        assert library.getBasePackage().equals("basepackage");
    }

    @Test
    public void compareTo() {
        Library library1 = new Library("application", "1", null);
        Library library11 = new Library("application", "1", null);
        Library library2 = new Library("application", "2", null);

        assert library1.compareTo(library11) == 0;

        assert library11.compareTo(library2) < 0;
    }

    @Test
    public void getComponents() {
        IJavaTypeFinder javaTypeFinderMock = createMock(IJavaTypeFinder.class);

        expect(javaTypeFinderMock.findTypesInPackageRecursively("com.app.components", true))
                .andReturn(new ArrayList<IJavaClassType>(Arrays.asList(new JavaClassTypeMock("com.app.components.Component1").setPublic(true).setDefaultConstructor(true))));

        replay(javaTypeFinderMock);

        TapestryProject tapestryProjectMock = org.easymock.classextension.EasyMock.createMock(TapestryProject.class);
        org.easymock.classextension.EasyMock.expect(tapestryProjectMock.getJavaTypeFinder()).andReturn(javaTypeFinderMock);
        org.easymock.classextension.EasyMock.replay(tapestryProjectMock);

        Library library = new Library(null, "com.app", tapestryProjectMock);
        assert library.getComponents().size() == 1;
    }

    @Test
    public void getPages() {
        IJavaTypeFinder javaTypeFinderMock = createMock(IJavaTypeFinder.class);

        expect(javaTypeFinderMock.findTypesInPackageRecursively("com.app.pages", true))
                .andReturn(new ArrayList<IJavaClassType>(Arrays.asList(new JavaClassTypeMock("com.app.pages.Page1").setPublic(true).setDefaultConstructor(true))));

        replay(javaTypeFinderMock);

        TapestryProject tapestryProjectMock = org.easymock.classextension.EasyMock.createMock(TapestryProject.class);
        org.easymock.classextension.EasyMock.expect(tapestryProjectMock.getJavaTypeFinder()).andReturn(javaTypeFinderMock);
        org.easymock.classextension.EasyMock.replay(tapestryProjectMock);

        Library library = new Library(null, "com.app", tapestryProjectMock);
        assert library.getPages().size() == 1;
    }

    @Test
    public void getMixins() {
        IJavaTypeFinder javaTypeFinderMock = createMock(IJavaTypeFinder.class);

        expect(javaTypeFinderMock.findTypesInPackageRecursively("com.app.mixins", true))
                .andReturn(new ArrayList<IJavaClassType>(Arrays.asList(new JavaClassTypeMock("com.app.mixins.Mixin1").setPublic(true).setDefaultConstructor(true))));

        replay(javaTypeFinderMock);

        TapestryProject tapestryProjectMock = org.easymock.classextension.EasyMock.createMock(TapestryProject.class);
        org.easymock.classextension.EasyMock.expect(tapestryProjectMock.getJavaTypeFinder()).andReturn(javaTypeFinderMock);
        org.easymock.classextension.EasyMock.replay(tapestryProjectMock);

        Library library = new Library(null, "com.app", tapestryProjectMock);
        assert library.getMixins().size() == 1;
    }

    @Test
    public void equals() {
        Library library1 = new Library(null, "com.app1", null);
        Library library2 = new Library(null, "com.app2", null);
        Library library3 = new Library(null, "com.app1", null);

        assert !library1.equals(null);

        assert !library3.equals("hey");

        assert !library1.equals(library2);

        assert library1.equals(library3);
    }

    @Test
    public void hashCode_test() {
        Library library1 = new Library(null, "com.app1", null);

        assert library1.hashCode() == "com.app1".hashCode();
    }
}
