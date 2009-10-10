/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.Library;
import static org.easymock.classextension.EasyMock.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.osmorc.facet.OsmorcFacetUtil;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetType;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.frameworkintegration.LibraryHandler;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ModuleDependencySynchronizerMockTest {
    @Before
    public void setUp() {
        bundleManager = createMock(BundleManager.class);
        moduleRootManager = createMock(ModuleRootManager.class);
        application = createMock(Application.class);
        libraryHandler = createMock(LibraryHandler.class);
        modifiableRootModel = createMock(ModifiableRootModel.class);
        module = createMock(Module.class);
        osmorcFacetUtil = createMock(OsmorcFacetUtil.class);

        expect(moduleRootManager.getModule()).andReturn(module).anyTimes();

        module1 = createMock(Module.class);
        module2 = createMock(Module.class);
        module3 = createMock(Module.class);
        module4 = createMock(Module.class);

        library1 = createMock(LibraryEx.class);
        library2 = createMock(Library.class);
        library3 = createMock(Library.class);
        library4 = createMock(Library.class);

        moduleOrderEntry1 = createMock(ModuleOrderEntry.class);
        expect(moduleOrderEntry1.getModule()).andReturn(module1).anyTimes();
        moduleOrderEntry2 = createMock(ModuleOrderEntry.class);
        expect(moduleOrderEntry2.getModule()).andReturn(module2).anyTimes();
        libraryOrderEntry1 = createMock(LibraryOrderEntry.class);
        expect(libraryOrderEntry1.getLibrary()).andReturn(library1).anyTimes();
        libraryOrderEntry2 = createMock(LibraryOrderEntry.class);
        expect(libraryOrderEntry2.getLibrary()).andReturn(library2).anyTimes();
    }

    @Test
    public void testDetermineOldModuleDependenciesWithManualEditing() {
        ModifiableRootModel modelMock = createMock(ModifiableRootModel.class);
        expect(modelMock.getOrderEntries())
                .andReturn(new OrderEntry[]{moduleOrderEntry1, libraryOrderEntry1, moduleOrderEntry2, libraryOrderEntry2})
                .times(1);
        expect(osmorcFacetUtil.hasOsmorcFacet(module1)).andReturn(true).times(1);

        OsmorcFacetConfiguration configuration = new OsmorcFacetConfiguration();
        configuration.setOsmorcControlsManifest(false);
        OsmorcFacet facet = new OsmorcFacet(OsmorcFacetType.INSTANCE, module1, configuration, null, "OSGi");
        expect(osmorcFacetUtil.getOsmorcFacet(module1)).andReturn(facet).times(1);
        expect(osmorcFacetUtil.hasOsmorcFacet(module2)).andReturn(false).times(1);
        expect(libraryHandler.isFrameworkInstanceLibrary(libraryOrderEntry1)).andReturn(false);
        expect(libraryHandler.isFrameworkInstanceLibrary(libraryOrderEntry2)).andReturn(true);


        replay(bundleManager, moduleRootManager, application, libraryHandler, modifiableRootModel, module,
                osmorcFacetUtil, module1, module2, module3, module4, moduleOrderEntry1, moduleOrderEntry2, library1,
                library2,
                library3, library4, libraryOrderEntry1, libraryOrderEntry2, modelMock);

        ModuleDependencySynchronizer testObject =
                new ModuleDependencySynchronizer(bundleManager, moduleRootManager, application, libraryHandler,
                        osmorcFacetUtil);


        List<OrderEntry> oldEntries = testObject.determineOldModuleDependencies(modelMock);

        assertThat(oldEntries.size(), equalTo(2));
        assertThat(oldEntries, hasItem((OrderEntry) moduleOrderEntry1));
        assertThat(oldEntries, hasItem((OrderEntry) libraryOrderEntry2));

        verify(bundleManager, moduleRootManager, application, libraryHandler, modifiableRootModel, module,
                osmorcFacetUtil, module1, module2, module3, module4, moduleOrderEntry1, moduleOrderEntry2, library1,
                library2,
                library3, library4, libraryOrderEntry1, libraryOrderEntry2, modelMock);
    }

    @Test
    public void testDetermineOldModuleDependenciesWithoutManualEditing() {
        ModifiableRootModel modelMock = createMock(ModifiableRootModel.class);
        expect(modelMock.getOrderEntries())
                .andReturn(new OrderEntry[]{moduleOrderEntry1, libraryOrderEntry1, moduleOrderEntry2, libraryOrderEntry2})
                .times(1);
        expect(osmorcFacetUtil.hasOsmorcFacet(module1)).andReturn(true).times(1);

        OsmorcFacetConfiguration configuration = new OsmorcFacetConfiguration();
        configuration.setOsmorcControlsManifest(true);
        OsmorcFacet facet = new OsmorcFacet(OsmorcFacetType.INSTANCE, module1, configuration, null, "OSGi");
        expect(osmorcFacetUtil.getOsmorcFacet(module1)).andReturn(facet).times(1);
        expect(osmorcFacetUtil.hasOsmorcFacet(module2)).andReturn(false).times(1);
        expect(libraryHandler.isFrameworkInstanceLibrary(libraryOrderEntry1)).andReturn(false);
        expect(libraryHandler.isFrameworkInstanceLibrary(libraryOrderEntry2)).andReturn(true);


        replay(bundleManager, moduleRootManager, application, libraryHandler, modifiableRootModel, module,
                osmorcFacetUtil, module1, module2, module3, module4, moduleOrderEntry1, moduleOrderEntry2, library1,
                library2,
                library3, library4, libraryOrderEntry1, libraryOrderEntry2, modelMock);

        ModuleDependencySynchronizer testObject =
                new ModuleDependencySynchronizer(bundleManager, moduleRootManager, application, libraryHandler,
                        osmorcFacetUtil);


        List<OrderEntry> oldEntries = testObject.determineOldModuleDependencies(modelMock);

        assertThat(oldEntries.size(), equalTo(1));
        assertThat(oldEntries, hasItem((OrderEntry) libraryOrderEntry2));

        verify(bundleManager, moduleRootManager, application, libraryHandler, modifiableRootModel, module,
                osmorcFacetUtil, module1, module2, module3, module4, moduleOrderEntry1, moduleOrderEntry2, library1,
                library2,
                library3, library4, libraryOrderEntry1, libraryOrderEntry2, modelMock);
    }

    @Test
    public void testDetermineObsoleteModuleDependencies() {
        replay(bundleManager, moduleRootManager, application, libraryHandler, modifiableRootModel, module,
                osmorcFacetUtil, module1, module2, module3, module4, moduleOrderEntry1, moduleOrderEntry2, library1,
                library2,
                library3, library4, libraryOrderEntry1, libraryOrderEntry2);

        List<OrderEntry> oldOrderEntries = new ArrayList<OrderEntry>();
        oldOrderEntries.add(moduleOrderEntry1);
        oldOrderEntries.add(moduleOrderEntry2);
        oldOrderEntries.add(libraryOrderEntry1);
        oldOrderEntries.add(libraryOrderEntry2);

        List<Object> newBundles = new ArrayList<Object>();
        newBundles.add(module1);
        newBundles.add(library4);
        newBundles.add(module3);
        newBundles.add(module4);
        newBundles.add(library2);
        newBundles.add(library3);

        ModuleDependencySynchronizer testObject =
                new ModuleDependencySynchronizer(bundleManager, moduleRootManager, application, libraryHandler,
                        osmorcFacetUtil);


        List<OrderEntry> obsoleteEntries = testObject.determineObsoleteModuleDependencies(oldOrderEntries, newBundles);

        assertThat(obsoleteEntries.size(), equalTo(2));
        assertThat(obsoleteEntries, hasItem((OrderEntry) moduleOrderEntry2));
        assertThat(obsoleteEntries, hasItem((OrderEntry) libraryOrderEntry1));

        verify(bundleManager, moduleRootManager, application, libraryHandler, modifiableRootModel, module,
                osmorcFacetUtil, module1, module2, module3, module4, moduleOrderEntry1, moduleOrderEntry2, library1,
                library2,
                library3, library4, libraryOrderEntry1, libraryOrderEntry2);
    }

    @Test
    public void testRemoveObsoleteModuleDependencies() {
        ModifiableRootModel modelMock = createMock(ModifiableRootModel.class);
        modelMock.removeOrderEntry(moduleOrderEntry1);
        modelMock.removeOrderEntry(libraryOrderEntry1);

        replay(bundleManager, moduleRootManager, application, libraryHandler, modifiableRootModel, module,
                osmorcFacetUtil, module1, module2, module3, module4, moduleOrderEntry1, moduleOrderEntry2, library1,
                library2,
                library3, library4, libraryOrderEntry1, libraryOrderEntry2, modelMock);

        List<OrderEntry> oldOrderEntries = new ArrayList<OrderEntry>();
        oldOrderEntries.add(moduleOrderEntry1);
        oldOrderEntries.add(libraryOrderEntry1);

        ModuleDependencySynchronizer testObject =
                new ModuleDependencySynchronizer(bundleManager, moduleRootManager, application, libraryHandler,
                        osmorcFacetUtil);


        assertThat(testObject.removeObsoleteModuleDependencies(modelMock, oldOrderEntries), equalTo(true));
        assertThat(testObject.removeObsoleteModuleDependencies(modelMock, new ArrayList<OrderEntry>()), equalTo(false));

        verify(bundleManager, moduleRootManager, application, libraryHandler, modifiableRootModel, module,
                osmorcFacetUtil, module1, module2, module3, module4, moduleOrderEntry1, moduleOrderEntry2, library1,
                library2,
                library3, library4, libraryOrderEntry1, libraryOrderEntry2, modelMock);
    }

    @Test
    public void testAddNewModuleDependencies() {
        ModifiableRootModel modelMock = createMock(ModifiableRootModel.class);
        expect(modelMock.addModuleOrderEntry(module1)).andReturn(moduleOrderEntry1).times(1);
        expect(library1.isDisposed()).andReturn(false).times(1);
        expect(modelMock.addLibraryEntry(library1)).andReturn(libraryOrderEntry1).times(1);

        replay(bundleManager, moduleRootManager, application, libraryHandler, modifiableRootModel, module,
                osmorcFacetUtil, module1, module2, module3, module4, moduleOrderEntry1, moduleOrderEntry2, library1,
                library2,
                library3, library4, libraryOrderEntry1, libraryOrderEntry2, modelMock);

        List<Object> newBundles = new ArrayList<Object>();
        newBundles.add(module1);
        newBundles.add(library1);

        ModuleDependencySynchronizer testObject =
                new ModuleDependencySynchronizer(bundleManager, moduleRootManager, application, libraryHandler,
                        osmorcFacetUtil);


        assertThat(testObject.addNewModuleDependencies(modelMock, newBundles), equalTo(true));
        assertThat(testObject.addNewModuleDependencies(modelMock, new ArrayList<Object>()), equalTo(false));

        newBundles.clear();
        newBundles.add(module);
        assertThat(testObject.addNewModuleDependencies(modelMock, newBundles), equalTo(false));

        verify(bundleManager, moduleRootManager, application, libraryHandler, modifiableRootModel, module,
                osmorcFacetUtil, module1, module2, module3, module4, moduleOrderEntry1, moduleOrderEntry2, library1,
                library2,
                library3, library4, libraryOrderEntry1, libraryOrderEntry2, modelMock);
    }

    @Test
    public void testAddNewModuleDependenciesWithDisposedLibrary() {
        ModifiableRootModel modelMock = createMock(ModifiableRootModel.class);
        expect(modelMock.addModuleOrderEntry(module1)).andReturn(moduleOrderEntry1).times(1);
        expect(library1.isDisposed()).andReturn(true).times(1);

        replay(bundleManager, moduleRootManager, application, libraryHandler, modifiableRootModel, module,
                osmorcFacetUtil, module1, module2, module3, module4, moduleOrderEntry1, moduleOrderEntry2, library1,
                library2,
                library3, library4, libraryOrderEntry1, libraryOrderEntry2, modelMock);

        List<Object> newBundles = new ArrayList<Object>();
        newBundles.add(module1);
        newBundles.add(library1);

        ModuleDependencySynchronizer testObject =
                new ModuleDependencySynchronizer(bundleManager, moduleRootManager, application, libraryHandler,
                        osmorcFacetUtil);


        assertThat(testObject.addNewModuleDependencies(modelMock, newBundles), equalTo(true));
        assertThat(testObject.addNewModuleDependencies(modelMock, new ArrayList<Object>()), equalTo(false));

        newBundles.clear();
        newBundles.add(module);
        assertThat(testObject.addNewModuleDependencies(modelMock, newBundles), equalTo(false));

        verify(bundleManager, moduleRootManager, application, libraryHandler, modifiableRootModel, module,
                osmorcFacetUtil, module1, module2, module3, module4, moduleOrderEntry1, moduleOrderEntry2, library1,
                library2,
                library3, library4, libraryOrderEntry1, libraryOrderEntry2, modelMock);
    }

    @Test
    public void testCheckAndSetReexport() {
        ModifiableRootModel modelMock = createMock(ModifiableRootModel.class);
        LibraryOrderEntry libraryOrderEntryWithoutLibrary = createMock(LibraryOrderEntry.class);
        expect(libraryOrderEntryWithoutLibrary.getLibrary()).andReturn(null).anyTimes();
        expect(libraryOrderEntryWithoutLibrary.getLibraryName()).andReturn("test").anyTimes();
        expect(library2.getName()).andReturn("test").anyTimes();
        expect(osmorcFacetUtil.hasOsmorcFacet(module1)).andReturn(true).anyTimes();
        expect(osmorcFacetUtil.hasOsmorcFacet(module2)).andReturn(true).anyTimes();
        expect(libraryHandler.isFrameworkInstanceLibrary(libraryOrderEntry1)).andReturn(true).anyTimes();
        expect(libraryHandler.isFrameworkInstanceLibrary(libraryOrderEntryWithoutLibrary)).andReturn(true).anyTimes();
        expect(modelMock.getOrderEntries()).andReturn(new OrderEntry[]{moduleOrderEntry1, moduleOrderEntry2, libraryOrderEntry1, libraryOrderEntryWithoutLibrary});
        expect(bundleManager.isReexported(module1, module)).andReturn(true).anyTimes();
        expect(bundleManager.isReexported(module2, module)).andReturn(false).anyTimes();
        expect(bundleManager.isReexported(library1, module)).andReturn(true).anyTimes();
        expect(bundleManager.isReexported(library2, module)).andReturn(false).anyTimes();
        expect(moduleOrderEntry1.isExported()).andReturn(false);
        moduleOrderEntry1.setExported(true);
        expect(moduleOrderEntry2.isExported()).andReturn(false);
        expect(libraryOrderEntry1.isExported()).andReturn(false);
        libraryOrderEntry1.setExported(true);
        expect(libraryOrderEntryWithoutLibrary.isExported()).andReturn(false);

        replay(bundleManager, moduleRootManager, application, libraryHandler, modifiableRootModel, module,
                osmorcFacetUtil, module1, module2, module3, module4, moduleOrderEntry1, moduleOrderEntry2, library1,
                library2,
                library3, library4, libraryOrderEntry1, libraryOrderEntry2, modelMock, libraryOrderEntryWithoutLibrary);

        List<Object> newBundles = new ArrayList<Object>();
        newBundles.add(library2);

        ModuleDependencySynchronizer testObject =
                new ModuleDependencySynchronizer(bundleManager, moduleRootManager, application, libraryHandler,
                        osmorcFacetUtil);


        assertThat(testObject.checkAndSetReexport(modelMock, newBundles), equalTo(true));

        verify(bundleManager, moduleRootManager, application, libraryHandler, modifiableRootModel, module,
                osmorcFacetUtil, module1, module2, module3, module4, moduleOrderEntry1, moduleOrderEntry2, library1,
                library2,
                library3, library4, libraryOrderEntry1, libraryOrderEntry2, modelMock, libraryOrderEntryWithoutLibrary);
    }


    private BundleManager bundleManager;
    private ModuleRootManager moduleRootManager;
    private Application application;
    private LibraryHandler libraryHandler;
    private ModifiableRootModel modifiableRootModel;
    private Module module;
    private OsmorcFacetUtil osmorcFacetUtil;
    private Module module1;
    private Module module2;
    private Module module3;
    private Module module4;
    private LibraryEx library1;
    private Library library2;
    private Library library3;
    private Library library4;
    private ModuleOrderEntry moduleOrderEntry1;
    private ModuleOrderEntry moduleOrderEntry2;
    private LibraryOrderEntry libraryOrderEntry1;
    private LibraryOrderEntry libraryOrderEntry2;
}
