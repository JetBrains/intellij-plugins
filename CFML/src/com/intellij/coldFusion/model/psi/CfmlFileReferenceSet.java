// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi;


import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.coldFusion.UI.config.CfmlMappingsConfig;
import com.intellij.coldFusion.UI.config.CfmlProjectConfiguration;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class CfmlFileReferenceSet extends FileReferenceSet implements PlatformIcons {
  private static class CfmlFileReference extends FileReference {
    CfmlFileReference(final CfmlFileReferenceSet set, final TextRange range, final int index, final String text) {
      super(set, range, index, text);
    }

    @Override
    protected void innerResolveInContext(@NotNull final String text,
                                         @NotNull final PsiFileSystemItem context,
                                         final @NotNull Collection<ResolveResult> result,
                                         final boolean caseSensitive) {

      CfmlProjectConfiguration.State state = CfmlProjectConfiguration.getInstance(getElement().getProject()).getState();
      if (state == null) {
        super.innerResolveInContext(text, context, result, caseSensitive);
        return;
      }
      CfmlMappingsConfig mappingsConfig = state.getMapps();
      Map<String, String> mapDir = mappingsConfig.getServerMappings();
      String textInFile;
      String filePathWithMap;
      String updateLogicalPath;
      int len = getFileReferenceSet().getAllReferences().length;
      for (String logicalPath : mapDir.keySet()) {
        updateLogicalPath = logicalPath.replaceAll("\\\\", "/");
        textInFile = getFileReferenceSet().getReference(0).getText();
        if (updateLogicalPath.contains(text) && (StringUtil.startsWithChar(updateLogicalPath, '/'))) {
          for (int i = 0; updateLogicalPath.contains(textInFile) && i < len; ++i) {
            if (updateLogicalPath.substring(1).equalsIgnoreCase(textInFile)) {
              filePathWithMap = mapDir.get(logicalPath);
              VirtualFile dir = filePathWithMap != null ? findFile(filePathWithMap) : null;
              if (dir != null) {
                PsiFileSystemItem child = context.getManager().findDirectory(dir);
                result.add(new PsiElementResolveResult(getOriginalFile(child)));
              }
            }
            if (i < len - 1) {
              textInFile = textInFile + "/" + getFileReferenceSet().getReference(i + 1).getText();
            }
          }
        }
      }
      if (getIndex() == 0 && getElement().getNode().getText().startsWith("/")) {
        return;
      }
      super.innerResolveInContext(text, context, result, caseSensitive);
    }


    @Override
    public Object @NotNull [] getVariants() {

      Collection<Object> variants = new LinkedList<>();
      String text = getElement().getNode().getText();
      if (StringUtil.startsWithChar(text, '\"') && text.endsWith("\"")) {
        text = text.substring(1, text.length() - 1);
      }
      if (StringUtil.startsWithChar(text, '/')) {
        CfmlProjectConfiguration.State state = CfmlProjectConfiguration.getInstance(getElement().getProject()).getState();
        CfmlMappingsConfig mappings = state != null ? state.getMapps().clone() : new CfmlMappingsConfig();
        for (String value : mappings.getServerMappings().keySet()) {
          if (getIndex() == 0 && (value.startsWith("/") || value.startsWith("\\"))) {
            variants.add(LookupElementBuilder.create(value, value.replace('\\', '/').substring(1)).withCaseSensitivity(false)
                           .withIcon(PlatformIcons.FOLDER_ICON));
          }
        }
        if (getIndex() == 0) {
          return variants.toArray();
        }
      }

      variants.addAll(Arrays.asList(super.getVariants()));
      return variants.toArray();
    }
  }


  public CfmlFileReferenceSet(CfmlCompositeElement element, int shift) {
    super(stripText(element.getText()), element,
          shift, null, element.getContainingFile().getViewProvider().getVirtualFile().isCaseSensitive());
  }

  private static String stripText(String text) {
    final int i1 = text.indexOf('\"') == -1 ? Integer.MAX_VALUE : text.indexOf('\"');
    final int i2 = text.indexOf('\'') == -1 ? Integer.MAX_VALUE : text.indexOf('\'');
    final char quoteCharacter = i1 < i2 ? '\"' : '\'';
    final int i = Math.min(i1, i2);
    if (i == Integer.MAX_VALUE) {
      // no quotes
      return text;
    }
    final int lastIndex = text.lastIndexOf(quoteCharacter);
    if (lastIndex == i) {
      // ex: <cfinclude template='<caret>"'>
      return text;
    }
    return text.substring(i + 1, lastIndex);
  }

  @Override
  public CfmlFileReference createFileReference(final TextRange range, final int index, final String text) {
    return new CfmlFileReference(this, range, index, text);
  }

  @NotNull
  @Override
  public Collection<PsiFileSystemItem> computeDefaultContexts() {
    ArrayList<PsiFileSystemItem> contexts = new ArrayList<>();
    if (StringUtil.startsWithChar(getPathString(), '/') || StringUtil.startsWithChar(getPathString(), '\\')) {
      PsiReference firstFileReference = getElement().getReferences()[0];
      if ((firstFileReference instanceof FileReference)) {
        CfmlProjectConfiguration.State state = CfmlProjectConfiguration.getInstance(getElement().getProject()).getState();
        if (state == null) {
          return super.computeDefaultContexts();
        }
        CfmlMappingsConfig mappingsConfig = state.getMapps();
        Map<String, String> mapDir = mappingsConfig.getServerMappings();
        for (String value : mapDir.keySet()) {
          final String path = mapDir.get(value);
          VirtualFile dir = findFile(path);
          if (dir != null) {
            PsiDirectory psiDirectory =
              getElement().getManager().findDirectory(dir);
            contexts.add(psiDirectory);
          }
        }
      }
    }
    contexts.addAll(super.computeDefaultContexts());
    return contexts;
  }

  private static VirtualFile findFile(String path) {
    VirtualFile dir = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(path));
    if (dir == null) {
      dir = VirtualFileManager.getInstance().findFileByUrl(path);
    }
    return dir;
  }
}
