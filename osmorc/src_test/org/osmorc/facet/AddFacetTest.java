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
package org.osmorc.facet;

import com.intellij.facet.FacetManager;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import org.jetbrains.osgi.jps.model.ManifestGenerationMode;

import java.io.IOException;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class AddFacetTest extends JavaCodeInsightFixtureTestCase {
  public void testAddFacetAfterCreatingManifest() throws IOException {
    WriteAction.runAndWait(() -> {
      VirtualFile[] roots = ModuleRootManager.getInstance(getModule()).getContentRoots();
      VirtualFile metaInf = roots[0].createChildDirectory(this, "META-INF");
      VirtualFile manifest = metaInf.createChildData(this, "MANIFEST.MF");
      VfsUtil.saveText(manifest, """
        Manifest-Version: 1.0
        Bundle-ManifestVersion: 2
        Bundle-Name: Test
        Bundle-SymbolicName: test
        Bundle-Version: 1.0.0
        """);
      PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
    });

    WriteAction.runAndWait(() -> {
      ModifiableFacetModel model = FacetManager.getInstance(getModule()).createModifiableModel();
      OsmorcFacet facet = new OsmorcFacet(getModule());
      facet.getConfiguration().setManifestGenerationMode(ManifestGenerationMode.Manually);
      model.addFacet(facet);
      model.commit();
    });
  }
}
