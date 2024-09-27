// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.mxml;

import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSReferenceList;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class FlexXmlBackedClassesIndex extends ScalarIndexExtension<String> {
  private static final int INDEX_VERSION = 4;

  @Override
  public @NotNull DataIndexer<String, Void, FileContent> getIndexer() {
    return new DataIndexer<>() {

      @Override
      public @NotNull Map<String, Void> map(@NotNull FileContent inputData) {
        final XmlFile file = (XmlFile)inputData.getPsiFile();

        final Map<String, Void> result = new HashMap<>();
        for (JSClass clazz : XmlBackedJSClassImpl.getClasses(file)) {
          JSReferenceList supers = getSupers(clazz);
          if (supers != null) {
            final JSExpression[] expressions = supers.getExpressions();
            for (JSExpression expr : expressions) {
              String s = expr instanceof JSReferenceExpression ? ((JSReferenceExpression)expr).getReferenceName() : null;
              if (s != null) result.put(s, null);
            }
          }
        }
        return result;
      }
    };
  }

  protected abstract @Nullable JSReferenceList getSupers(JSClass clazz);

  @Override
  public @NotNull KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }

  @Override
  public @NotNull FileBasedIndex.InputFilter getInputFilter() {
    return new DefaultFileTypeSpecificInputFilter(FlexSupportLoader.getMxmlFileType()) {
      @Override
      public boolean acceptInput(@NotNull VirtualFile file) {
        return FlexSupportLoader.isMxmlOrFxgFile(file);
      }
    };
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return JSFileElementType.getVersion(INDEX_VERSION);
  }

  public static Collection<JSClass> searchClassInheritors(ID<String, Void> indexId, String name, Project project, final GlobalSearchScope scope) {
    final Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(indexId, name, scope);
    final PsiManager psiManager = PsiManager.getInstance(project);
    Collection<JSClass> classes = new ArrayList<>(files.size());
    for (VirtualFile file : files) {
      PsiFile psifile = psiManager.findFile(file);
      if (!(psifile instanceof XmlFile)) continue;
      classes.addAll(XmlBackedJSClassImpl.getClasses((XmlFile)psifile));
    }

    if (FlexNameAlias.CONTAINER_TYPE_NAME.equals(name)) {
      classes.addAll(searchClassInheritors(indexId, FlexNameAlias.COMPONENT_TYPE_NAME, project, scope));
    }
    return classes;
  }
}
