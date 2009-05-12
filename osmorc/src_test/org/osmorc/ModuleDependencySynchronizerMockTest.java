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
import com.intellij.openapi.roots.libraries.Library;
import static org.easymock.classextension.EasyMock.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.osmorc.facet.OsmorcFacetUtil;
import org.osmorc.frameworkintegration.LibraryHandler;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ModuleDependencySynchronizerMockTest
{
  @Before
  public void setUp()
  {
    _bundleManager = createMock(BundleManager.class);
    _moduleRootManager = createMock(ModuleRootManager.class);
    _application = createMock(Application.class);
    _libraryHandler = createMock(LibraryHandler.class);
    _modifiableRootModel = createMock(ModifiableRootModel.class);
    _module = createMock(Module.class);
    _osmorcFacetUtil = createMock(OsmorcFacetUtil.class);

    expect(_moduleRootManager.getModule()).andReturn(_module).anyTimes();

    _module1 = createMock(Module.class);
    _module2 = createMock(Module.class);
    _module3 = createMock(Module.class);
    _module4 = createMock(Module.class);

    _library1 = createMock(Library.class);
    _library2 = createMock(Library.class);
    _library3 = createMock(Library.class);
    _library4 = createMock(Library.class);

    _moduleOrderEntry1 = createMock(ModuleOrderEntry.class);
    expect(_moduleOrderEntry1.getModule()).andReturn(_module1).anyTimes();
    _moduleOrderEntry2 = createMock(ModuleOrderEntry.class);
    expect(_moduleOrderEntry2.getModule()).andReturn(_module2).anyTimes();
    _libraryOrderEntry1 = createMock(LibraryOrderEntry.class);
    expect(_libraryOrderEntry1.getLibrary()).andReturn(_library1).anyTimes();
    _libraryOrderEntry2 = createMock(LibraryOrderEntry.class);
    expect(_libraryOrderEntry2.getLibrary()).andReturn(_library2).anyTimes();
  }

  @Test
  public void testDetermineOldModuleDependencies()
  {
    ModifiableRootModel modelMock = createMock(ModifiableRootModel.class);
    expect(modelMock.getOrderEntries())
        .andReturn(new OrderEntry[]{_moduleOrderEntry1, _libraryOrderEntry1, _moduleOrderEntry2, _libraryOrderEntry2})
        .times(1);
    expect(_osmorcFacetUtil.hasOsmorcFacet(_module1)).andReturn(true).times(1);
    expect(_osmorcFacetUtil.hasOsmorcFacet(_module2)).andReturn(false).times(1);
    expect(_libraryHandler.isFrameworkInstanceLibrary(_libraryOrderEntry1)).andReturn(false);
    expect(_libraryHandler.isFrameworkInstanceLibrary(_libraryOrderEntry2)).andReturn(true);


    replay(_bundleManager, _moduleRootManager, _application, _libraryHandler, _modifiableRootModel, _module,
        _osmorcFacetUtil, _module1, _module2, _module3, _module4, _moduleOrderEntry1, _moduleOrderEntry2, _library1,
        _library2,
        _library3, _library4, _libraryOrderEntry1, _libraryOrderEntry2, modelMock);

    ModuleDependencySynchronizer testObject =
        new ModuleDependencySynchronizer(_bundleManager, _moduleRootManager, _application, _libraryHandler,
            _osmorcFacetUtil);


    List<OrderEntry> oldEntries = testObject.determineOldModuleDependencies(modelMock);

    assertThat(oldEntries.size(), equalTo(2));
    assertThat(oldEntries, hasItem((OrderEntry) _moduleOrderEntry1));
    assertThat(oldEntries, hasItem((OrderEntry) _libraryOrderEntry2));

    verify(_bundleManager, _moduleRootManager, _application, _libraryHandler, _modifiableRootModel, _module,
        _osmorcFacetUtil, _module1, _module2, _module3, _module4, _moduleOrderEntry1, _moduleOrderEntry2, _library1,
        _library2,
        _library3, _library4, _libraryOrderEntry1, _libraryOrderEntry2, modelMock);
  }

  @Test
  public void testDetermineObsoleteModuleDependencies()
  {
    replay(_bundleManager, _moduleRootManager, _application, _libraryHandler, _modifiableRootModel, _module,
        _osmorcFacetUtil, _module1, _module2, _module3, _module4, _moduleOrderEntry1, _moduleOrderEntry2, _library1,
        _library2,
        _library3, _library4, _libraryOrderEntry1, _libraryOrderEntry2);

    List<OrderEntry> oldOrderEntries = new ArrayList<OrderEntry>();
    oldOrderEntries.add(_moduleOrderEntry1);
    oldOrderEntries.add(_moduleOrderEntry2);
    oldOrderEntries.add(_libraryOrderEntry1);
    oldOrderEntries.add(_libraryOrderEntry2);

    List<Object> newBundles = new ArrayList<Object>();
    newBundles.add(_module1);
    newBundles.add(_library4);
    newBundles.add(_module3);
    newBundles.add(_module4);
    newBundles.add(_library2);
    newBundles.add(_library3);

    ModuleDependencySynchronizer testObject =
        new ModuleDependencySynchronizer(_bundleManager, _moduleRootManager, _application, _libraryHandler,
            _osmorcFacetUtil);


    List<OrderEntry> obsoleteEntries = testObject.determineObsoleteModuleDependencies(oldOrderEntries, newBundles);

    assertThat(obsoleteEntries.size(), equalTo(2));
    assertThat(obsoleteEntries, hasItem((OrderEntry) _moduleOrderEntry2));
    assertThat(obsoleteEntries, hasItem((OrderEntry) _libraryOrderEntry1));

    verify(_bundleManager, _moduleRootManager, _application, _libraryHandler, _modifiableRootModel, _module,
        _osmorcFacetUtil, _module1, _module2, _module3, _module4, _moduleOrderEntry1, _moduleOrderEntry2, _library1,
        _library2,
        _library3, _library4, _libraryOrderEntry1, _libraryOrderEntry2);
  }

  @Test
  public void testRemoveObsoleteModuleDependencies()
  {
    ModifiableRootModel modelMock = createMock(ModifiableRootModel.class);
    modelMock.removeOrderEntry(_moduleOrderEntry1);
    modelMock.removeOrderEntry(_libraryOrderEntry1);

    replay(_bundleManager, _moduleRootManager, _application, _libraryHandler, _modifiableRootModel, _module,
        _osmorcFacetUtil, _module1, _module2, _module3, _module4, _moduleOrderEntry1, _moduleOrderEntry2, _library1,
        _library2,
        _library3, _library4, _libraryOrderEntry1, _libraryOrderEntry2, modelMock);

    List<OrderEntry> oldOrderEntries = new ArrayList<OrderEntry>();
    oldOrderEntries.add(_moduleOrderEntry1);
    oldOrderEntries.add(_libraryOrderEntry1);

    ModuleDependencySynchronizer testObject =
        new ModuleDependencySynchronizer(_bundleManager, _moduleRootManager, _application, _libraryHandler,
            _osmorcFacetUtil);


    assertThat(testObject.removeObsoleteModuleDependencies(modelMock, oldOrderEntries), equalTo(true));
    assertThat(testObject.removeObsoleteModuleDependencies(modelMock, new ArrayList<OrderEntry>()), equalTo(false));

    verify(_bundleManager, _moduleRootManager, _application, _libraryHandler, _modifiableRootModel, _module,
        _osmorcFacetUtil, _module1, _module2, _module3, _module4, _moduleOrderEntry1, _moduleOrderEntry2, _library1,
        _library2,
        _library3, _library4, _libraryOrderEntry1, _libraryOrderEntry2, modelMock);
  }

  @Test
  public void testAddNewModuleDependencies()
  {
    ModifiableRootModel modelMock = createMock(ModifiableRootModel.class);
    expect(modelMock.addModuleOrderEntry(_module1)).andReturn(_moduleOrderEntry1).times(1);
    expect(modelMock.addLibraryEntry(_library1)).andReturn(_libraryOrderEntry1).times(1);

    replay(_bundleManager, _moduleRootManager, _application, _libraryHandler, _modifiableRootModel, _module,
        _osmorcFacetUtil, _module1, _module2, _module3, _module4, _moduleOrderEntry1, _moduleOrderEntry2, _library1,
        _library2,
        _library3, _library4, _libraryOrderEntry1, _libraryOrderEntry2, modelMock);

    List<Object> newBundles = new ArrayList<Object>();
    newBundles.add(_module1);
    newBundles.add(_library1);

    ModuleDependencySynchronizer testObject =
        new ModuleDependencySynchronizer(_bundleManager, _moduleRootManager, _application, _libraryHandler,
            _osmorcFacetUtil);


    assertThat(testObject.addNewModuleDependencies(modelMock, newBundles), equalTo(true));
    assertThat(testObject.addNewModuleDependencies(modelMock, new ArrayList<Object>()), equalTo(false));

    newBundles.clear();
    newBundles.add(_module);
    assertThat(testObject.addNewModuleDependencies(modelMock, newBundles), equalTo(false));

    verify(_bundleManager, _moduleRootManager, _application, _libraryHandler, _modifiableRootModel, _module,
        _osmorcFacetUtil, _module1, _module2, _module3, _module4, _moduleOrderEntry1, _moduleOrderEntry2, _library1,
        _library2,
        _library3, _library4, _libraryOrderEntry1, _libraryOrderEntry2, modelMock);
  }

  @Test
  public void testCheckAndSetReexport()
  {
    ModifiableRootModel modelMock = createMock(ModifiableRootModel.class);
    LibraryOrderEntry libraryOrderEntryWithoutLibrary = createMock(LibraryOrderEntry.class);
    expect(libraryOrderEntryWithoutLibrary.getLibrary()).andReturn(null).anyTimes();
    expect(libraryOrderEntryWithoutLibrary.getLibraryName()).andReturn("test").anyTimes();
    expect(_library2.getName()).andReturn("test").anyTimes();
    expect(_osmorcFacetUtil.hasOsmorcFacet(_module1)).andReturn(true).anyTimes();
    expect(_osmorcFacetUtil.hasOsmorcFacet(_module2)).andReturn(true).anyTimes();
    expect(_libraryHandler.isFrameworkInstanceLibrary(_libraryOrderEntry1)).andReturn(true).anyTimes();
    expect(_libraryHandler.isFrameworkInstanceLibrary(libraryOrderEntryWithoutLibrary)).andReturn(true).anyTimes();
    expect(modelMock.getOrderEntries()).andReturn(new OrderEntry[]{_moduleOrderEntry1, _moduleOrderEntry2, _libraryOrderEntry1, libraryOrderEntryWithoutLibrary});
    expect(_bundleManager.isReexported(_module1, _module)).andReturn(true).anyTimes();
    expect(_bundleManager.isReexported(_module2, _module)).andReturn(false).anyTimes();
    expect(_bundleManager.isReexported(_library1, _module)).andReturn(true).anyTimes();
    expect(_bundleManager.isReexported(_library2, _module)).andReturn(false).anyTimes();
    expect(_moduleOrderEntry1.isExported()).andReturn(false);
    _moduleOrderEntry1.setExported(true);
    expect(_moduleOrderEntry2.isExported()).andReturn(false);
    expect(_libraryOrderEntry1.isExported()).andReturn(false);
    _libraryOrderEntry1.setExported(true);
    expect(libraryOrderEntryWithoutLibrary.isExported()).andReturn(false);

    replay(_bundleManager, _moduleRootManager, _application, _libraryHandler, _modifiableRootModel, _module,
        _osmorcFacetUtil, _module1, _module2, _module3, _module4, _moduleOrderEntry1, _moduleOrderEntry2, _library1,
        _library2,
        _library3, _library4, _libraryOrderEntry1, _libraryOrderEntry2, modelMock, libraryOrderEntryWithoutLibrary);

    List<Object> newBundles = new ArrayList<Object>();
    newBundles.add(_library2);

    ModuleDependencySynchronizer testObject =
        new ModuleDependencySynchronizer(_bundleManager, _moduleRootManager, _application, _libraryHandler,
            _osmorcFacetUtil);


    assertThat(testObject.checkAndSetReexport(modelMock, newBundles), equalTo(true));

    verify(_bundleManager, _moduleRootManager, _application, _libraryHandler, _modifiableRootModel, _module,
        _osmorcFacetUtil, _module1, _module2, _module3, _module4, _moduleOrderEntry1, _moduleOrderEntry2, _library1,
        _library2,
        _library3, _library4, _libraryOrderEntry1, _libraryOrderEntry2, modelMock, libraryOrderEntryWithoutLibrary);
  }



  private BundleManager _bundleManager;
  private ModuleRootManager _moduleRootManager;
  private Application _application;
  private LibraryHandler _libraryHandler;
  private ModifiableRootModel _modifiableRootModel;
  private Module _module;
  private OsmorcFacetUtil _osmorcFacetUtil;
  private Module _module1;
  private Module _module2;
  private Module _module3;
  private Module _module4;
  private Library _library1;
  private Library _library2;
  private Library _library3;
  private Library _library4;
  private ModuleOrderEntry _moduleOrderEntry1;
  private ModuleOrderEntry _moduleOrderEntry2;
  private LibraryOrderEntry _libraryOrderEntry1;
  private LibraryOrderEntry _libraryOrderEntry2;
}
