package com.intellij.tapestry.core.model;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaTypeFinder;
import com.intellij.tapestry.core.mocks.JavaClassTypeMock;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.easymock.EasyMock.*;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class LibraryModelTest {

    @Test
    public void constructor() {
        TapestryLibrary library = new TapestryLibrary("id", "basepackage", null);

        assert library.getId().equals("id");
        assert library.getBasePackage().equals("basepackage");
    }

    @Test
    public void compareTo() {
        TapestryLibrary library1 = new TapestryLibrary("application", "1", null);
        TapestryLibrary library11 = new TapestryLibrary("application", "1", null);
        TapestryLibrary library2 = new TapestryLibrary("application", "2", null);

        assert library1.compareTo(library11) == 0;

        assert library11.compareTo(library2) < 0;
    }

    @Test
    public void getComponents() {
        IJavaTypeFinder javaTypeFinderMock = createMock(IJavaTypeFinder.class);

        expect(javaTypeFinderMock.findTypesInPackageRecursively("com.app.components", true))
                .andReturn(new ArrayList<>(
                  Arrays.asList(new JavaClassTypeMock("com.app.components.Component1").setPublic(true).setDefaultConstructor(true))));

        replay(javaTypeFinderMock);

        TapestryProject tapestryProjectMock = createMock(TapestryProject.class);
        expect(tapestryProjectMock.getJavaTypeFinder()).andReturn(javaTypeFinderMock);
        replay(tapestryProjectMock);

        TapestryLibrary library = new TapestryLibrary(null, "com.app", tapestryProjectMock);
        assert library.getComponents().size() == 1;
    }

    @Test
    public void getPages() {
        IJavaTypeFinder javaTypeFinderMock = createMock(IJavaTypeFinder.class);

        expect(javaTypeFinderMock.findTypesInPackageRecursively("com.app.pages", true))
                .andReturn(
                  new ArrayList<>(Arrays.asList(new JavaClassTypeMock("com.app.pages.Page1").setPublic(true).setDefaultConstructor(true))));

        replay(javaTypeFinderMock);

        TapestryProject tapestryProjectMock = createMock(TapestryProject.class);
        expect(tapestryProjectMock.getJavaTypeFinder()).andReturn(javaTypeFinderMock);
        replay(tapestryProjectMock);

        TapestryLibrary library = new TapestryLibrary(null, "com.app", tapestryProjectMock);
        assert library.getPages().size() == 1;
    }

    @Test
    public void getMixins() {
        IJavaTypeFinder javaTypeFinderMock = createMock(IJavaTypeFinder.class);

        expect(javaTypeFinderMock.findTypesInPackageRecursively("com.app.mixins", true))
                .andReturn(
                  new ArrayList<>(Arrays.asList(new JavaClassTypeMock("com.app.mixins.Mixin1").setPublic(true).setDefaultConstructor(true))));

        replay(javaTypeFinderMock);

        TapestryProject tapestryProjectMock = createMock(TapestryProject.class);
        expect(tapestryProjectMock.getJavaTypeFinder()).andReturn(javaTypeFinderMock);
        replay(tapestryProjectMock);

        TapestryLibrary library = new TapestryLibrary(null, "com.app", tapestryProjectMock);
        assert library.getMixins().size() == 1;
    }

    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "ConstantConditions"})
    @Test
    public void equals() {
        TapestryLibrary library1 = new TapestryLibrary(null, "com.app1", null);
        TapestryLibrary library2 = new TapestryLibrary(null, "com.app2", null);
        TapestryLibrary library3 = new TapestryLibrary(null, "com.app1", null);

        assert !library1.equals(null);

        assert !library3.equals("hey");

        assert !library1.equals(library2);

        assert library1.equals(library3);
    }

    @Test
    public void hashCode_test() {
        TapestryLibrary library1 = new TapestryLibrary(null, "com.app1", null);

        assert library1.hashCode() == "com.app1".hashCode();
    }
}
