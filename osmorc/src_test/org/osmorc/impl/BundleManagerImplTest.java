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

package org.osmorc.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.psi.PsiFile;
import static org.easymock.classextension.EasyMock.*;
import org.junit.Test;
import org.osmorc.manifest.BundleManifest;
import org.osmorc.manifest.ManifestHolder;
import org.osmorc.manifest.ManifestHolderRegistry;
import org.osmorc.settings.ProjectSettings;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class BundleManagerImplTest {
    /**
     * This test reproduces the bug described in issue IDEADEV-37879.
     */
    @Test
    public void testInvalidImportPackageBundleVersion() {
        ProjectSettings projectSettings = new ProjectSettings();
        projectSettings.setFrameworkInstanceName(null);

        Module module = createMock(Module.class);
        ManifestHolderRegistry holderRegistry = createMock(ManifestHolderRegistry.class);
        ManifestHolder holder = createMock(ManifestHolder.class);
        BundleManifest bundleManifest = createMock(BundleManifest.class);
        PsiFile manifestFile = createMock(PsiFile.class);
        ModuleManager moduleManager = createMock(ModuleManager.class);

        expect(moduleManager.getModules()).andReturn(new Module[0]).anyTimes();
        expect(holderRegistry.getManifestHolder(same(module))).andReturn(holder).anyTimes();
        expect(holder.getBundleManifest()).andReturn(bundleManifest).anyTimes();
        expect(holder.getBundleID()).andReturn(0L).anyTimes();
        expect(bundleManifest.getManifestFile()).andReturn(manifestFile).anyTimes();
        expect(manifestFile.getText()).andReturn("Manifest-Version: 1.0\n" +
                "Bundle-Name: Test\n" +
                "Import-Package: org.osgi.framework, services.myservices; bundle-symbolic-name = service0; bundle-version = 1.\n" +
                "Bundle-SymbolicName: test\n" +
                "Bundle-Version: 1.0.0 \n" +
                "Bundle-ManifestVersion: 2\n");
        BundleManagerImpl testObject = new BundleManagerImpl(moduleManager, holderRegistry, null, projectSettings, null);

        replay(module, holderRegistry, holder, bundleManifest, manifestFile, moduleManager);

        testObject.addOrUpdateBundle(module);

        verify(module, holderRegistry, holder, bundleManifest, manifestFile, moduleManager);
    }

    /**
     * This test reproduces the bug described in issue OSMORC-127.
     */
    @Test
    public void testInvalidManifestVersion() {
        ProjectSettings projectSettings = new ProjectSettings();
        projectSettings.setFrameworkInstanceName(null);

        Module module = createMock(Module.class);
        ManifestHolderRegistry holderRegistry = createMock(ManifestHolderRegistry.class);
        ManifestHolder holder = createMock(ManifestHolder.class);
        BundleManifest bundleManifest = createMock(BundleManifest.class);
        PsiFile manifestFile = createMock(PsiFile.class);
        ModuleManager moduleManager = createMock(ModuleManager.class);

        expect(moduleManager.getModules()).andReturn(new Module[0]).anyTimes();
        expect(holderRegistry.getManifestHolder(same(module))).andReturn(holder).anyTimes();
        expect(holder.getBundleManifest()).andReturn(bundleManifest).anyTimes();
        expect(holder.getBundleID()).andReturn(0L).anyTimes();
        expect(bundleManifest.getManifestFile()).andReturn(manifestFile).anyTimes();
        expect(manifestFile.getText()).andReturn("Manifest-Version: 1.0\n" +
                "Bundle-Name: Test\n" +
                "Bundle-SymbolicName: test\n" +
                "Bundle-Version: 1.0.0\n" +
                "Bundle-ManifestVersion: 2 \n");
        BundleManagerImpl testObject = new BundleManagerImpl(moduleManager, holderRegistry, null, projectSettings, null);

        replay(module, holderRegistry, holder, bundleManifest, manifestFile, moduleManager);

        testObject.addOrUpdateBundle(module);

        verify(module, holderRegistry, holder, bundleManifest, manifestFile, moduleManager);
    }
}
