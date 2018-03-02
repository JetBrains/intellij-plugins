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

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.XmlSuppressableInspectionTool;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.dom.MavenDomUtil;
import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.dom.model.MavenDomRepository;
import org.jetbrains.idea.maven.model.MavenConstants;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenArtifactUtil;
import org.jetbrains.osgi.jps.build.CachingBundleInfoProvider;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.i18n.OsmorcBundle;
import org.osmorc.inspection.AbstractOsgiQuickFix;
import org.osmorc.obrimport.MavenRepository;
import org.osmorc.obrimport.ObrSearchDialog;
import org.osmorc.obrimport.springsource.ObrMavenResult;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Inspection which detects non-OSGi dependencies.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public class NonOsgiMavenDependencyInspection extends XmlSuppressableInspectionTool {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder problemsHolder, boolean isOnTheFly) {
    boolean isMaven = MavenDomUtil.isMavenFile(problemsHolder.getFile());
    return !isMaven ? PsiElementVisitor.EMPTY_VISITOR : new XmlElementVisitor() {
      @Override
      public void visitXmlTag(XmlTag tag) {
        if (OsmorcFacet.hasOsmorcFacet(tag)) {
          MavenDomDependency dependency = getDependency(tag);
          if (dependency != null) {
            String scope = dependency.getScope().getStringValue();
            if (!MavenConstants.SCOPE_TEST.equals(scope)) {
              File repo = MavenProjectsManager.getInstance(tag.getProject()).getLocalRepository();
              String groupId = dependency.getGroupId().getStringValue();
              String artifactId = dependency.getArtifactId().getStringValue();
              String version = dependency.getVersion().getStringValue();
              File artifactFile = MavenArtifactUtil.getArtifactFile(repo, groupId, artifactId, version, MavenConstants.TYPE_JAR);
              if (artifactFile.exists() && !CachingBundleInfoProvider.isBundle(artifactFile.getPath())) {
                problemsHolder.registerProblem(tag, OsmorcBundle.message("NonOsgiMavenDependencyInspection.message"), new FindOsgiCapableMavenDependencyQuickFix());
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

  /**
   * Fix which tries to find a compatible OSGi-ready version of a Maven dependency.
   */
  private static class FindOsgiCapableMavenDependencyQuickFix extends AbstractOsgiQuickFix {
    @NotNull
    @Override
    public String getName() {
      return OsmorcBundle.message("NonOsgiMavenDependencyInspection.fix");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
      final MavenDomDependency dependency = getDependency((XmlTag)problemDescriptor.getPsiElement());
      if (dependency == null) return;
      final ObrMavenResult result = ObrSearchDialog.queryForMavenArtifact(project, dependency.getArtifactId().toString());
      if (result == null) return;

      final PsiFile psiFile = problemDescriptor.getPsiElement().getContainingFile();
      WriteCommandAction.writeCommandAction(project, psiFile).run(() -> {
        MavenDomProjectModel model = MavenDomUtil.getMavenDomProjectModel(project, psiFile.getVirtualFile());
        if (model == null) return;

        // replace dependency element

        MavenDomDependency dummy = model.getDependencies().addDependency();
        dummy.getGroupId().setStringValue(result.getGroupId());
        dummy.getArtifactId().setStringValue(result.getArtifactId());
        dummy.getVersion().setStringValue(result.getVersion());
        String scope = dependency.getScope().getStringValue();
        if (!StringUtil.isEmpty(scope)) {
          dummy.getScope().setStringValue(scope);
        }

        PsiElement newDep = dummy.getXmlElement();
        PsiElement oldDep = dependency.getXmlElement();
        assert newDep != null : dummy;
        assert oldDep != null : dependency;
        oldDep.replace(newDep.copy());
        newDep.delete();

        // add new repository if needed

        Set<String> projectRepositoryUrls =
          ContainerUtil.map2Set(model.getRepositories().getRepositories(), repository -> repository.getUrl().getStringValue());

        List<MavenRepository> newRepositories = ContainerUtil.newSmartList(result.getBundleRepository().getMavenRepositories());

        for (Iterator<MavenRepository> i = newRepositories.iterator(); i.hasNext(); ) {
          MavenRepository repository = i.next();
          if (projectRepositoryUrls.contains(repository.getRepositoryUrl())) {
            i.remove();
          }
        }

        for (MavenRepository repository : newRepositories) {
          MavenDomRepository added = model.getRepositories().addRepository();
          added.getId().setStringValue(repository.getRepositoryId());
          added.getUrl().setStringValue(repository.getRepositoryUrl());
          added.getName().setStringValue(repository.getRepositoryDescription());
        }
      });
    }
  }
}
