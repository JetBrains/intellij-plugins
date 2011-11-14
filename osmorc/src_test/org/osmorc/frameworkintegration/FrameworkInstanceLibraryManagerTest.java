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

package org.osmorc.frameworkintegration;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmorc.SwingRunner;
import org.osmorc.facet.OsmorcFacetUtil;
import org.osmorc.settings.ApplicationSettings;
import org.osmorc.settings.ProjectSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 * @deprecated
 */
@SuppressWarnings({"ConstantConditions"})
@RunWith(SwingRunner.class)
@Ignore 
public class FrameworkInstanceLibraryManagerTest {
    private Module module;
    private TempDirTestFixture myTempDirFixture;
    private OsmorcFacetUtil osmorcFacetUtil;

    @Before
    public void setUp() throws Exception {
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder =
                JavaTestFixtureFactory.createFixtureBuilder();
        final JavaModuleFixtureBuilder moduleBuilder = fixtureBuilder.addModule(JavaModuleFixtureBuilder.class);

        myTempDirFixture = IdeaTestFixtureFactory.getFixtureFactory().createTempDirTestFixture();

        myTempDirFixture.setUp();
        moduleBuilder.addContentRoot(myTempDirFixture.getTempDirPath());

        fixture = fixtureBuilder.getFixture();
        fixture.setUp();

        osmorcFacetUtil = createMock(OsmorcFacetUtil.class);

        Application application = ApplicationManager.getApplication();
        projectSettings = new ProjectSettings();
        myTestObject = new FrameworkInstanceLibraryManager(ApplicationSettings.getInstance(),projectSettings,
                application, fixture.getProject(),
                ModuleManager.getInstance(fixture.getProject()));
        project = fixture.getProject();
        module = fixture.getModule();
        application.runWriteAction(new Runnable() {
            public void run() {
                LibraryTable.ModifiableModel modifiableModel =
                        LibraryTablesRegistrar.getInstance().getLibraryTable().getModifiableModel();
                libraryA1 = modifiableModel.createLibrary("Osmorc/an Instance/libA1_1.0.0");
                libraryA2 = modifiableModel.createLibrary("Osmorc/an Instance/libA2_1.0.0");
                libraryB1 = modifiableModel.createLibrary("Osmorc/another Instance/libB1_1.0.0");
                libraryB2 = modifiableModel.createLibrary("Osmorc/another Instance/libB2_1.0.0");
                libraryB3 = modifiableModel.createLibrary("Osmorc/another Instance/libB3_1.0.0");
                modifiableModel.commit();
            }
        });

    }

    @After
    public void tearDown() throws Exception {
        fixture.tearDown();
    }

    @Test
    public void testUpdateFrameworkInstanceModuleCreatesAndUpdatesModule() {
        expect(osmorcFacetUtil.hasOsmorcFacet(EasyMock.not(same(module)))).andReturn(false).anyTimes();
        expect(osmorcFacetUtil.hasOsmorcFacet(same(module))).andReturn(true).times(2);
        replay(osmorcFacetUtil);

        projectSettings.setCreateFrameworkInstanceModule(true);
        projectSettings.setFrameworkInstanceName("an Instance");
        myTestObject.updateFrameworkInstanceLibraries();
        Module module = ModuleManager.getInstance(project)
                .findModuleByName(FrameworkInstanceLibraryManager.OsmorcControlledLibrariesPrefix);
        OrderEntry[] orderEntries = ModuleRootManager.getInstance(module).getOrderEntries();
        assertContainsOnlyGivenLibraries(orderEntries, libraryA1, libraryA2);

        projectSettings.setFrameworkInstanceName("another Instance");
        myTestObject.updateFrameworkInstanceLibraries();
        orderEntries = ModuleRootManager.getInstance(module).getOrderEntries();
        assertContainsOnlyGivenLibraries(orderEntries, libraryB1, libraryB2, libraryB3);

        projectSettings.setCreateFrameworkInstanceModule(false);
        projectSettings.setFrameworkInstanceName("an Instance");
        myTestObject.updateFrameworkInstanceLibraries();
        module = ModuleManager.getInstance(project)
                .findModuleByName(FrameworkInstanceLibraryManager.OsmorcControlledLibrariesPrefix);
        assertThat(module, nullValue());

        verify(osmorcFacetUtil);
    }

    @Test
    public void testUpdateFrameworkInstanceModuleDoesntCreateModule() {
        expect(osmorcFacetUtil.hasOsmorcFacet(EasyMock.not(same(module)))).andReturn(false).anyTimes();
        expect(osmorcFacetUtil.hasOsmorcFacet(same(module))).andReturn(false).once();
        replay(osmorcFacetUtil);

        projectSettings.setCreateFrameworkInstanceModule(true);
        projectSettings.setFrameworkInstanceName("an Instance");
        myTestObject.updateFrameworkInstanceLibraries();
        Module module = ModuleManager.getInstance(project)
                .findModuleByName(FrameworkInstanceLibraryManager.OsmorcControlledLibrariesPrefix);
        assertThat(module, nullValue());

        verify(osmorcFacetUtil);
    }

    @Test
    public void testUpdateFrameworkInstanceModuleRemovesModule() {
        expect(osmorcFacetUtil.hasOsmorcFacet(EasyMock.not(same(module)))).andReturn(false).anyTimes();
        expect(osmorcFacetUtil.hasOsmorcFacet(same(module))).andReturn(true).once();
        expect(osmorcFacetUtil.hasOsmorcFacet(same(module))).andReturn(false).once();
        replay(osmorcFacetUtil);

        projectSettings.setCreateFrameworkInstanceModule(true);
        projectSettings.setFrameworkInstanceName("an Instance");
        myTestObject.updateFrameworkInstanceLibraries();
        module = ModuleManager.getInstance(project)
                .findModuleByName(FrameworkInstanceLibraryManager.OsmorcControlledLibrariesPrefix);
        assertThat(module, notNullValue());

        myTestObject.updateFrameworkInstanceLibraries();
        Module module = ModuleManager.getInstance(project)
                .findModuleByName(FrameworkInstanceLibraryManager.OsmorcControlledLibrariesPrefix);
        assertThat(module, nullValue());

        verify(osmorcFacetUtil);
    }

    public void assertContainsOnlyGivenLibraries(OrderEntry[] orderEntries, Library... libraries) {
        List<Library> libs = new ArrayList<Library>(Arrays.asList(libraries));
        int findCount = 0;

        for (OrderEntry orderEntry : orderEntries) {
            if (orderEntry instanceof LibraryOrderEntry) {
                assertTrue(libs.remove(((LibraryOrderEntry) orderEntry).getLibrary()));
                findCount++;
            }
        }

        assertThat(libraries.length, equalTo(findCount));
    }

    private IdeaProjectTestFixture fixture;
    private FrameworkInstanceLibraryManager myTestObject;
    private Project project;

    private Library libraryA1;
    private Library libraryA2;
    private Library libraryB1;
    private Library libraryB2;
    private Library libraryB3;
    private ProjectSettings projectSettings;
}
