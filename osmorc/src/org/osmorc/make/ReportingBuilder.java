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

package org.osmorc.make;

import aQute.lib.osgi.Builder;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.osmorc.BundleManager;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetConfiguration;
import org.osmorc.manifest.BundleManifest;

import java.text.MessageFormat;

/**
 * Created by IntelliJ IDEA. User: kork Date: Jul 20, 2009 Time: 9:51:18 PM To change this template use File | Settings
* | File Templates.
*/
class ReportingBuilder extends Builder
{
  private final CompileContext myContext;
  private String mySourceFileName;
  private String myMessagePrefix;

  public ReportingBuilder(CompileContext context, String sourceFileName, Module module)
  {
    super();
    myContext = context;
    OsmorcFacet facet = OsmorcFacet.getInstance(module);
    mySourceFileName = sourceFileName;
    // link back to the original manifest if it's manually edited
    if ( facet != null ) {
      OsmorcFacetConfiguration configuration = facet.getConfiguration();
      if (configuration.isManifestManuallyEdited()) {
        BundleManager bundleManager = ServiceManager.getService(module.getProject(), BundleManager.class);
        BundleManifest bundleManifest = bundleManager.getBundleManifest(module);
        if (bundleManifest != null) {
            PsiFile manifestFile = bundleManifest.getManifestFile();
          VirtualFile virtualFile = manifestFile.getVirtualFile();
          if ( virtualFile != null )
          mySourceFileName = VfsUtil.pathToUrl(virtualFile.getPath());
        }
      }else {
          // try if module was imported from maven.
          MavenProjectsManager projectsManager = MavenProjectsManager.getInstance(module.getProject());
          MavenProject project = projectsManager.findProject(module);
          if ( project != null ) {
            MavenPlugin plugin = project.findPlugin("org.apache.felix", "maven-bundle-plugin");
            if ( plugin != null ) {
              // ok it's imported from maven, link warnings/errors back to pom.xml
              mySourceFileName = VfsUtil.pathToUrl(project.getPath());
            }
          }
        }
    }
    myMessagePrefix = "[" + module.getName() + "] ";
  }


  @Override
  public void error(String s, Object... objects)
  {
    myContext.addMessage(CompilerMessageCategory.ERROR, MessageFormat.format(myMessagePrefix +s, objects), mySourceFileName, 0,0);
  }

  @Override
  public void error(String s, Throwable throwable, Object... objects)
  {
    myContext.addMessage(CompilerMessageCategory.ERROR,
                         MessageFormat.format(myMessagePrefix + s, objects) + "(" + throwable.getMessage() + ")",
                         mySourceFileName, 0, 0);
  }

  @Override
  public void warning(String s, Object... objects)
  {
    myContext.addMessage(CompilerMessageCategory.WARNING, MessageFormat.format(myMessagePrefix + s, objects), mySourceFileName, 0,0);

  }

  @Override
  public void progress(String s, Object... objects)
  {
    myContext.addMessage(CompilerMessageCategory.INFORMATION, MessageFormat.format(myMessagePrefix + s, objects), mySourceFileName, 0,0);

  }

  /**
   * Overridden to make it public.
   */
  public void begin() {
    super.begin();
  }

}
