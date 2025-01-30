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
package org.osmorc.maven.inspection;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.dom.MavenDomUtil;
import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.model.MavenConstants;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenArtifactUtil;
import org.jetbrains.osgi.jps.build.CachingBundleInfoProvider;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.i18n.OsmorcBundle;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Inspection which detects non-OSGi dependencies.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
final class NonOsgiMavenDependencyInspection extends XmlSuppressableInspectionTool {
  @Override
  public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean isOnTheFly) {
    boolean isMaven = MavenDomUtil.isMavenFile(problemsHolder.getFile());
    return !isMaven ? PsiElementVisitor.EMPTY_VISITOR : new XmlElementVisitor() {
      @Override
      public void visitXmlTag(@NotNull XmlTag tag) {
        if (OsmorcFacet.hasOsmorcFacet(tag)) {
          MavenDomDependency dependency = getDependency(tag);
          if (dependency != null) {
            String scope = dependency.getScope().getStringValue();
            if (!MavenConstants.SCOPE_TEST.equals(scope)) {
              Path repo = MavenProjectsManager.getInstance(tag.getProject()).getRepositoryPath();
              String groupId = dependency.getGroupId().getStringValue();
              String artifactId = dependency.getArtifactId().getStringValue();
              String version = dependency.getVersion().getStringValue();
              Path artifactFile = MavenArtifactUtil.getArtifactNioPath(repo, groupId, artifactId, version, MavenConstants.TYPE_JAR);
              if (Files.exists(artifactFile) && !CachingBundleInfoProvider.isBundle(artifactFile.toString())) {
                problemsHolder.registerProblem(tag, OsmorcBundle.message("NonOsgiMavenDependencyInspection.message"));
              }
            }
          }
        }
      }
    };
  }

  private static MavenDomDependency getDependency(XmlTag tag) {
    if ("dependency".equals(tag.getName())) {
      PsiElement parent = tag.getParent();
      if (parent != null) {
        PsiElement grand = parent.getParent();
        if (!(grand instanceof XmlTag && "plugin".equals(((XmlTag)grand).getName()))) {
          DomElement dom = DomManager.getDomManager(tag.getProject()).getDomElement(tag);
          if (dom != null) {
            return dom.getParentOfType(MavenDomDependency.class, false);
          }
        }
      }
    }

    return null;
  }
}
