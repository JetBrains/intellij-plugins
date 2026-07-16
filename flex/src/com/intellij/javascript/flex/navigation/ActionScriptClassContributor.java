// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.navigation;

import com.intellij.javascript.flex.index.ActionScriptElementFinder;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.navigation.DumbAwareChooseByNameContributor;
import com.intellij.lang.javascript.psi.ecmal4.JSNamespaceDeclaration;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClass;
import com.intellij.lang.javascript.psi.stubs.JSClassIndex;
import com.intellij.navigation.GotoClassContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
import com.intellij.util.Processors;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;

public final class ActionScriptClassContributor extends DumbAwareChooseByNameContributor implements GotoClassContributor {

  @Override
  public void doProcessNames(@NotNull Processor<? super String> processor, @NotNull GlobalSearchScope scope, @Nullable IdFilter filter) {
    FileType type = FlexSupportLoader.getMxmlFileType();
    if (type != null) {
      if (!FileTypeIndex.processFiles(type, Processors.map(processor, VirtualFile::getNameWithoutExtension), scope)) return;
    }
    StubIndex.getInstance().processAllKeys(JSClassIndex.KEY, key -> {
      String adjusted = !key.isEmpty() && key.charAt(0) == JSClassIndex.INTERFACE_MARK ? key.substring(1) : key;
      return processor.process(adjusted.substring(adjusted.lastIndexOf('.') + 1));
    }, scope, filter);
  }

  @Override
  public void doProcessElementsWithName(@NotNull String name,
                                        @NotNull Processor<? super NavigationItem> processor,
                                        @NotNull FindSymbolParameters parameters) {
    Project project = parameters.getProject();
    GlobalSearchScope scope = parameters.getSearchScope();
    Collection<JSQualifiedNamedElement> psiClasses = ActionScriptElementFinder.findElementsByName(name, project, scope);

    for (Iterator<JSQualifiedNamedElement> q = psiClasses.iterator(); q.hasNext(); ) {
      JSQualifiedNamedElement element = q.next();
      if (!(element instanceof XmlBackedJSClass) && !(element instanceof JSNamespaceDeclaration)) {
        q.remove();
      }
    }
    ContainerUtil.process(psiClasses, processor);
  }

  @Override
  public @Nullable String getQualifiedName(@NotNull NavigationItem item) {
    return null;
  }

  @Override
  public @Nullable String getQualifiedNameSeparator() {
    return null;
  }

  @Override
  public @NotNull Language getElementLanguage() {
    return FlexSupportLoader.ECMA_SCRIPT_L4;
  }
}
