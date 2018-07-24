package com.intellij.tapestry.core;

import com.intellij.tapestry.core.java.IJavaTypeFinder;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.core.resource.IResourceFinder;
import com.intellij.tapestry.core.resource.TestableResource;
import static org.easymock.EasyMock.*;
import org.testng.annotations.BeforeTest;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author <a href="mailto:hugo.palma@logical-software.com">Hugo Palma</a>
 */
public class TapestryProjectTest {

    private IJavaTypeFinder _javaTypeFinder;
    private IResourceFinder _web1ResourceFinder;
    private IResourceFinder _web2ResourceFinder;
    private IResourceFinder _web3ResourceFinder;
    private IResourceFinder _meta1ResourceFinder;
    private IResourceFinder _meta2ResourceFinder;
    private IResourceFinder _meta3ResourceFinder;

    @BeforeTest
    public void initMocks() {
        _javaTypeFinder = createMock(IJavaTypeFinder.class);

        _web1ResourceFinder = createMock(IResourceFinder.class);
        expect(_web1ResourceFinder.findContextResource(isA(String.class))).andReturn(new TestableResource("web.xml", "web1.xml")).anyTimes();

        _web2ResourceFinder = createMock(IResourceFinder.class);
        expect(_web2ResourceFinder.findContextResource(isA(String.class))).andReturn(new TestableResource("web.xml", "web2.xml")).anyTimes();

        _web3ResourceFinder = createMock(IResourceFinder.class);
        expect(_web3ResourceFinder.findContextResource(isA(String.class))).andReturn(new TestableResource("web.xml", "web3.xml")).anyTimes();

        _meta1ResourceFinder = createMock(IResourceFinder.class);
        Collection<IResource> manifest1 = new ArrayList<>();
        manifest1.add(new TestableResource("MANIFEST.MF", "MANIFEST1.MF"));
        expect(_meta1ResourceFinder.findClasspathResource(isA(String.class), eq(true))).andReturn(manifest1).anyTimes();
        expect(_meta1ResourceFinder.findContextResource(isA(String.class))).andReturn(new TestableResource("web.xml", "web1.xml")).anyTimes();

        _meta2ResourceFinder = createMock(IResourceFinder.class);
        Collection<IResource> manifest2 = new ArrayList<>();
        manifest2.add(new TestableResource("MANIFEST.MF", "MANIFEST2.MF"));
        expect(_meta2ResourceFinder.findClasspathResource(isA(String.class), eq(true))).andReturn(manifest2).anyTimes();
        expect(_meta2ResourceFinder.findContextResource(isA(String.class))).andReturn(new TestableResource("web.xml", "web1.xml")).anyTimes();

        _meta3ResourceFinder = createMock(IResourceFinder.class);
        Collection<IResource> manifest3 = new ArrayList<>();
        manifest3.add(new TestableResource("MANIFEST.MF", "MANIFEST3.MF"));
        expect(_meta3ResourceFinder.findClasspathResource(isA(String.class), eq(true))).andReturn(manifest3).anyTimes();
        expect(_meta3ResourceFinder.findContextResource(isA(String.class))).andReturn(new TestableResource("web.xml", "web1.xml")).anyTimes();

        replay(_web1ResourceFinder, _web2ResourceFinder, _web3ResourceFinder, _meta1ResourceFinder, _meta2ResourceFinder, _meta3ResourceFinder);
    }

    //@Test
    //public void getApplicationRootPackage_found_it() throws NotFoundException {
    //    TapestryProject project = new TapestryProject(module, _web1ResourceFinder, _javaTypeFinder, null);
    //
    //    assert project.getApplicationRootPackage().equals("org.example.myapp");
    //    assert project.getApplicationRootPackage().equals("org.example.myapp");
    //}
    //
    //@Test
    //public void getApplicationRootPackage_cant_find_it() {
    //    TapestryProject project = new TapestryProject(module, _web2ResourceFinder, _javaTypeFinder, null);
    //
    //    try {
    //        project.getApplicationRootPackage();
    //    } catch (NotFoundException e) {
    //        // success
    //        return;
    //    }
    //
    //    assert false;
    //}
    //
    //@Test
    //public void getApplicationRootPackage_invalid_file() {
    //    TapestryProject project = new TapestryProject(module, _web3ResourceFinder, _javaTypeFinder, null);
    //
    //    try {
    //        project.getApplicationRootPackage();
    //    } catch (NotFoundException e) {
    //        return;
    //    }
    //
    //    assert false;
    //}
    //
    //@Test
    //public void getPagesRootPackage() throws NotFoundException {
    //    TapestryProject project = new TapestryProject(module, _web1ResourceFinder, _javaTypeFinder, null);
    //
    //    assert project.getPagesRootPackage().equals("org.example.myapp.pages");
    //}
    //
    //@Test
    //public void getComponentsRootPackage() throws NotFoundException {
    //    TapestryProject project = new TapestryProject(module, _web1ResourceFinder, _javaTypeFinder, null);
    //
    //    assert project.getComponentsRootPackage().equals("org.example.myapp.components");
    //}
    //
    //@Test
    //public void getLibraries_only_default() {
    //    TapestryProject project = new TapestryProject(module, _meta1ResourceFinder, _javaTypeFinder, null);
    //
    //    assert project.getLibraries().size() == 2;
    //}
    //
    //@Test
    //public void getTapestryFilterName() throws NotFoundException {
    //    TapestryProject project = new TapestryProject(module, _web1ResourceFinder, _javaTypeFinder, null);
    //
    //    assert project.getTapestryFilterName().equals("app");
    //}
}
