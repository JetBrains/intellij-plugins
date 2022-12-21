// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.psi.css.resolve.CssInclusionContext;
import com.intellij.psi.css.resolve.CssResolveManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.AstLoadingFilter;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.cli.AngularCliUtil;
import org.angular2.cli.config.AngularConfigProvider;
import org.angular2.cli.config.AngularProject;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2ComponentLocator;
import org.angular2.entities.Angular2EntitiesProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.util.ObjectUtils.doIfNotNull;
import static java.util.Arrays.asList;

public class Angular2CssInclusionContext extends CssInclusionContext {

  @NonNls private static final Key<CachedValue<ComponentCssContext>> COMPONENT_CONTEXT_KEY =
    new Key<>("ng.component.context");

  @Override
  public PsiFile @NotNull [] getContextFiles(@NotNull PsiFile current) {
    ComponentCssContext componentContext = getComponentContext(current);
    if (componentContext != null) {
      return componentContext.getCssFiles();
    }
    return PsiFile.EMPTY_ARRAY;
  }

  @Override
  public boolean processAllCssFilesOnResolving(@NotNull PsiElement context) {
    ComponentCssContext componentContext = getComponentContext(context);
    return componentContext != null
           && !componentContext.isAngularCli();
  }

  @Override
  public PsiFile @NotNull [] getLocalUseScope(@NotNull PsiFile file) {
    if (file instanceof StylesheetFile) {
      Angular2Component component = Angular2EntitiesProvider.getComponent(Angular2ComponentLocator.findComponentClass(file));
      if (component != null) {
        List<PsiFile> files = new ArrayList<>(component.getCssFiles());
        doIfNotNull(component.getTemplateFile(), files::add);
        return files.toArray(PsiFile.EMPTY_ARRAY);
      }
    }
    return PsiFile.EMPTY_ARRAY;
  }

  private static @Nullable ComponentCssContext getComponentContext(@NotNull PsiElement context) {
    PsiFile file = context.getContainingFile();
    return CachedValuesManager.getCachedValue(file, COMPONENT_CONTEXT_KEY, () -> {
      Angular2Component component = Angular2EntitiesProvider.getComponent(Angular2ComponentLocator.findComponentClass(file));
      if (component != null) {
        ComponentCssContext componentCssContext = new ComponentCssContext(component, file);
        return CachedValueProvider.Result.create(componentCssContext, componentCssContext.getDependencies());
      }
      return CachedValueProvider.Result.create(
        null,
        PsiModificationTracker.MODIFICATION_COUNT,
        VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS);
    });
  }

  private static final class ComponentCssContext {

    private final Angular2Component myComponent;
    private final VirtualFile myAngularCliJson;

    private ComponentCssContext(Angular2Component component, PsiFile file) {
      myComponent = component;

      final PsiFile original = InjectedLanguageManager.getInstance(file.getProject()).getTopLevelFile(
        CompletionUtil.getOriginalOrSelf(file));
      final VirtualFile angularCliFolder = AngularCliUtil.findAngularCliFolder(
        file.getProject(), original.getOriginalFile().getViewProvider().getVirtualFile());
      myAngularCliJson = AngularCliUtil.findCliJson(angularCliFolder);
    }

    public Object[] getDependencies() {
      return ContainerUtil.packNullables(
        PsiModificationTracker.MODIFICATION_COUNT,
        VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
        myAngularCliJson)
        .toArray();
    }

    public PsiFile[] getCssFiles() {
      final Project project = myComponent.getSourceElement().getProject();
      final List<PsiFile> cssFilesList = new ArrayList<>(myComponent.getCssFiles());
      AngularProject ngProject;
      if (myAngularCliJson != null
          && (ngProject = AngularConfigProvider.getAngularProject(project, myAngularCliJson)) != null) {
        PsiManager psiManager = PsiManager.getInstance(project);
        PsiFile html = doIfNotNull(ngProject.getIndexHtmlFile(), psiManager::findFile);
        if (html instanceof XmlFile) {
          AstLoadingFilter.forceAllowTreeLoading(html, () ->
            cssFilesList.addAll(asList(CssResolveManager.getInstance().getNewResolver()
                                         .resolveStyleSheets((XmlFile)html, null))));
        }
        cssFilesList.addAll(ContainerUtil.mapNotNull(
          ngProject.getGlobalStyleSheets(), file -> ObjectUtils.tryCast(psiManager.findFile(file), StylesheetFile.class)));
      }
      return cssFilesList.toArray(PsiFile.EMPTY_ARRAY);
    }

    public boolean isAngularCli() {
      return myAngularCliJson != null;
    }
  }
}
