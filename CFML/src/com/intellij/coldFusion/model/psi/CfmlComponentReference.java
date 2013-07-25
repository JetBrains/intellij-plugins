/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.model.psi;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.coldFusion.UI.config.CfmlMappingsConfig;
import com.intellij.coldFusion.UI.config.CfmlProjectConfiguration;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.psi.stubs.CfmlIndex;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopes;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PlatformIcons;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author vnikolaenko
 */
// TODO: correctly deal with intersecting mappings (or show error?)
public class CfmlComponentReference extends CfmlCompositeElement implements CfmlReference, PlatformIcons {
  private PsiElement myParent = null;

  public CfmlComponentReference(@NotNull ASTNode node) {
    super(node);
  }

  public CfmlComponentReference(@NotNull ASTNode node, PsiElement parent) {
    this(node);
    myParent = parent;
  }


  public String getComponentQualifiedName(String name) {
    return getContainingFile().getComponentQualifiedName(name);
  }

  /*
  private class PrefixSuffix {
    // with dot separators
    public String prefix;
    // with file separators
    public String suffix;

    private PrefixSuffix(String prefix, String suffix) {
      this.prefix = prefix;
      this.suffix = suffix;
    }
  }

  private List<String> getPossiblePathsForComponent(String componentQualifiedName) {
    CfmlMappingsConfig mappings = CfmlProjectConfiguration.getInstance(getProject()).getMappings();
    List<String> result = new LinkedList<String>();
    StringBuilder prefix = new StringBuilder();
    String suffix = File.separatorChar + componentQualifiedName.replace('.', File.separatorChar);

    List<PrefixSuffix> ps = new LinkedList<PrefixSuffix>();
    while (suffix != "") {
      ps.add(new PrefixSuffix(prefix.toString(), suffix));
      // recount prefix and suffix
      int i = suffix.indexOf(File.separatorChar, 1);
      if (i == -1) {
        break;
      }
      String s = suffix.substring(1, i);
      if (prefix.length() != 0) {
        s = File.separatorChar + s;
      }
      prefix.append(s);
      suffix = suffix.substring(i);
    }

    for (PrefixSuffix p : ps) {
      String s = mappings.serverMappings.get(p.prefix);
      if (s != null) {
        result.add(s + p.suffix);
      }
    }
    return result;
  }
  */

  /**
   * @param componentQualifiedName
   * @param originalFile           = getContainingFile().getOriginalFile();
   * @return
   */
  public static Collection<CfmlComponent> resolveFromQualifiedName(String componentQualifiedName, @NotNull CfmlFile originalFile) {
    List<CfmlComponent> result = new ArrayList<CfmlComponent>();

    if (componentQualifiedName == null) {
      return result;
    }

    Project project = originalFile.getProject();
    if (!componentQualifiedName.contains(".")) {
      // resolve with directory scope
      // PsiFile containingFile = getContainingFile();
      // containingFile = containingFile == null ? null : containingFile.getOriginalFile();
      {
        CfmlFile cfmlConteiningFile = originalFile;
        PsiDirectory directory = cfmlConteiningFile.getParent();
        if (directory != null) {
          GlobalSearchScope searchScope = GlobalSearchScopes.directoryScope(directory, false);

          final Collection<CfmlComponent> components = CfmlIndex.getInstance(project).getComponentsByNameInScope(
            componentQualifiedName, searchScope);
          components.addAll(CfmlIndex.getInstance(project).getInterfacesByNameInScope(
            componentQualifiedName, searchScope));
          for (CfmlComponent component : components) {
            result.add(component);
          }
        }
        else {
          final Collection<CfmlComponent> components = CfmlIndex.getInstance(project).getComponentsByName(
            componentQualifiedName);
          components.addAll(CfmlIndex.getInstance(project).getInterfacesByName(componentQualifiedName));
          for (CfmlComponent component : components) {
            result.add(component);
          }
        }
      }
    }

    if (result.isEmpty()) {
      String componentName = getComponentName(componentQualifiedName);

      int i = componentQualifiedName.lastIndexOf(".");
      String directoryName;
      if (i == -1) {
        directoryName = "";
      }
      else {
        directoryName = componentQualifiedName.substring(0, i);
      }

      CfmlProjectConfiguration.State state = CfmlProjectConfiguration.getInstance(project).getState();
      CfmlMappingsConfig mappings = state != null ? state.getMapps().clone() : new CfmlMappingsConfig();
      adjustMappingsIfEmpty(mappings, originalFile.getProject());
      // addFakeMappingsForResolution(mappings);
      List<String> realPossiblePaths = mappings.mapVirtualToReal(directoryName);
      // Collections.sort(realPossiblePaths);

      final Collection<CfmlComponent> components = CfmlIndex.getInstance(project).getComponentsByName(
        componentName);
      components.addAll(CfmlIndex.getInstance(project).getInterfacesByName(
        componentName));

      for (CfmlComponent component : components) {
        PsiDirectory parent = component.getContainingFile().getParent();
        if (parent == null) {
          continue;
        }
        VirtualFile virtualFile = parent.getVirtualFile();
        for (String realPath : realPossiblePaths) {
          if (FileUtil.toSystemIndependentName(realPath).equals(FileUtil.toSystemIndependentName(virtualFile.getPresentableUrl()))) {
            result.add(component);
            break;
          }
        }
      }

      for (String realPath : realPossiblePaths) {
        VirtualFile fileByUrl = LocalFileSystem.getInstance().findFileByPath(realPath);
        if (fileByUrl != null) {
          PsiFile file = PsiManager.getInstance(project).findFile(fileByUrl);
          if (file != null) {
            PsiDirectory directory = file.getParent();
            if (directory != null) {
              GlobalSearchScope searchScope = GlobalSearchScopes.directoryScope(directory, false);

              final Collection<CfmlComponent> componentsFromGlobalScope = CfmlIndex.getInstance(project).getComponentsByNameInScope(
                componentName, searchScope);
              componentsFromGlobalScope.addAll(CfmlIndex.getInstance(project).getInterfacesByNameInScope(
                componentName, searchScope));

              for (CfmlComponent component : componentsFromGlobalScope) {
                result.add(component);
              }
            }
          }
        }
      }
    }

    if (result.isEmpty()) {
      final Pair<String, String> prefixAndName = CfmlUtil.getPrefixAndName(componentQualifiedName);
      final String componentName = prefixAndName.getSecond();
      final CfmlImport cfmlImport = CfmlUtil.getImportByPrefix(originalFile, prefixAndName.getFirst());
      if (cfmlImport != null && !StringUtil.isEmpty(componentName)) {
        String libtag = cfmlImport.getImportString();
        final VirtualFile folder = CfmlUtil.findFileByLibTag(originalFile, libtag);
        if (folder != null && folder.isDirectory()) {
          final GlobalSearchScope scope = GlobalSearchScopes.directoryScope(originalFile.getProject(), folder, true);
          result.addAll(CfmlIndex.getInstance(originalFile.getProject()).getComponentsByNameInScope(componentName, scope));
        }
      }
    }
    return result;
  }

  private final ResolveCache.PolyVariantResolver<CfmlComponentReference> MY_RESOLVER =
    new ResolveCache.PolyVariantResolver<CfmlComponentReference>() {
      @NotNull
      public ResolveResult[] resolve(@NotNull final CfmlComponentReference expression, final boolean incompleteCode) {
        String componentQualifiedName;
        CfmlImport parentOfType = PsiTreeUtil.getParentOfType(expression, CfmlImport.class);
        if (parentOfType != null) {
          componentQualifiedName = getText();
        }
        else {
          componentQualifiedName = getComponentQualifiedName(getText());
        }
        PsiFile containingFile = getContainingFile();
        containingFile = containingFile == null ? null : containingFile.getOriginalFile();
        if (containingFile instanceof CfmlFile) {
          return CfmlResolveResult.create(resolveFromQualifiedName(componentQualifiedName, ((CfmlFile)containingFile)));
        }
        return ResolveResult.EMPTY_ARRAY;
      }
    };

  @NotNull
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    // incompleteCode = true, when autocompletion is executed,
    // in this case, containingFile is not physical and there is no way to get parent directory
    return MY_RESOLVER.resolve(this, incompleteCode);
    // if(incompleteCode)
    // return (getManager()).getResolveCache().resolveWithCaching(this, MY_RESOLVER, true, false);
    /*
    else
      return MY_RESOLVER.resolve(this, incompleteCode);
    */
  }

  private static String getComponentName(@NotNull String componentName) {
    int i = componentName.lastIndexOf('.');

    if (i == -1) {
      return componentName;
    }

    if (i == componentName.length() - 1) {
      return "";
    }

    return componentName.substring(i + 1);
  }

  public PsiElement getElement() {
    return myParent != null ? myParent : this;
  }

  public TextRange getRangeInElement() {
    int offset = 0;
    if (myParent != null) {
      final int parentOffset = myParent.getTextRange().getStartOffset();
      offset = getTextRange().getStartOffset() - parentOffset;
    }
    /*
    final String referenceText = getCanonicalText();
    final int index = referenceText.lastIndexOf(".");
    if (index != -1) {
      return new TextRange(index + 1, getTextLength()).shiftRight(offset);
    }
    */

    return new TextRange(0, getTextLength()).shiftRight(offset);
  }

  public PsiElement resolve() {
    ResolveResult[] results = multiResolve(false);
    if (results.length == 1) {
      return results[0].getElement();
    }
    return null;
  }

  @NotNull
  public String getCanonicalText() {
    return getText();
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    throw new IncorrectOperationException("Not implemented yet");
  }

  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    throw new IncorrectOperationException("Not implemented yet");
  }

  public boolean isReferenceTo(PsiElement element) {
    // TODO: replace with fully qualified names

    if (element instanceof CfmlComponent && getCanonicalText().equals(((CfmlComponent)element).getName())) {
      return true;
    }
    return false;
  }

  @NotNull
  public Object[] getVariants() {
    // final CfmlIndex cfmlIndex = CfmlIndex.getInstance(getProject());
    Collection<Object> variants = new LinkedList<Object>();

    String text = getText();

    String directoryName = "";
    if (text.contains(".")) {
      int i = text.lastIndexOf(".");
      directoryName = text.substring(0, i);
    }
    CfmlProjectConfiguration.State state = CfmlProjectConfiguration.getInstance(getProject()).getState();
    CfmlMappingsConfig mappings = state != null ? state.getMapps().clone() : new CfmlMappingsConfig();

    adjustMappingsIfEmpty(mappings, getProject());
    addFakeMappingsForImports(mappings);

    List<String> realPossiblePaths = mappings.mapVirtualToReal(directoryName);

    for (String realPath : realPossiblePaths) {
      addVariantsFromPath(variants, directoryName, realPath);
    }
    for (String value : mappings.getServerMappings().keySet()) {
      if (value.startsWith(directoryName) && !value.isEmpty() && (StringUtil.startsWithChar(value, '/') ||
                                                                  StringUtil.startsWithChar(value, '\\'))) {
        variants.add(value.replace('\\', '.').replace('/', '.').substring(1));
      }
    }
    PsiFile containingFile = getContainingFile();
    containingFile = containingFile == null ? null : containingFile.getOriginalFile();
    if (containingFile != null && containingFile instanceof CfmlFile) {
      CfmlFile cfmlContainingFile = (CfmlFile)containingFile;
      if (directoryName.length() == 0) {
        PsiDirectory directory = cfmlContainingFile.getParent();
        if (directory != null) {
          addVariantsFromPath(variants, "", directory.getVirtualFile().getPresentableUrl());
        }
      }
      else {
        String dirPath = cfmlContainingFile.getParent().getVirtualFile().getPath() + "/" + directoryName.replaceAll("[./]+", "/");
        addVariantsFromPath(variants, directoryName, dirPath);
      }
    }

    final String finalDirectoryName = directoryName;

    final Project project = getProject();
    return ContainerUtil.map2Array(variants, new Function<Object, Object>() {
      class DotInsertHandler implements InsertHandler<LookupElement> {
        @Override
        public void handleInsert(InsertionContext context, LookupElement item) {
          Document document = context.getDocument();
          int offset = context.getEditor().getCaretModel().getOffset();
          document.insertString(offset, ".");
          context.getEditor().getCaretModel().moveToOffset(offset + 1);
        }
      }

      public Object fun(final Object object) {
        if (object instanceof VirtualFile) {
          VirtualFile element = (VirtualFile)object;
          String name = finalDirectoryName + (finalDirectoryName.length() == 0 ? "" : ".") + element.getNameWithoutExtension();
          if (element.isDirectory()) {
            return LookupElementBuilder.create(name).withIcon(DIRECTORY_CLOSED_ICON)
              .withInsertHandler(new DotInsertHandler()).withCaseSensitivity(false);
          }
          else {
            Icon icon = CLASS_ICON;
            // choosing correct icon (class or interface)
            if (CfmlIndex.getInstance(project).getComponentsByNameInScope(element.getNameWithoutExtension(), GlobalSearchScope
              .fileScope(project, element)).size() == 1) {
              icon = CLASS_ICON;
            }
            else if (CfmlIndex.getInstance(project).getInterfacesByNameInScope(element.getNameWithoutExtension(), GlobalSearchScope
              .fileScope(project, element)).size() == 1) {
              icon = INTERFACE_ICON;
            }
            return LookupElementBuilder.create(name).withIcon(icon).withCaseSensitivity(false);
          }
        }
        else if (object instanceof String) {
          return LookupElementBuilder.create((String)object).withInsertHandler(new DotInsertHandler()).withCaseSensitivity(false);
        }
        return object;
      }
    });
  }

  private static void adjustMappingsIfEmpty(CfmlMappingsConfig mappings, Project project) {
    if (mappings.getServerMappings().size() != 0) {
      return;
    }

    for (VirtualFile root : ProjectRootManager.getInstance(project).getContentRoots()) {
      mappings.putToServerMappings("", root.getPresentableUrl());
    }
  }

  private void addFakeMappingsForImports(CfmlMappingsConfig mappings) {
    if (PsiTreeUtil.getParentOfType(this, CfmlImport.class) != null) {
      // create fake mappings for imports
      Collection<String> importStrings = getContainingFile().getImportStrings();
      for (String importString : importStrings) {
        final int index = importString.lastIndexOf('.');
        if (index == -1) {
          continue;
        }
        final String leftMapping = getComponentQualifiedName(importString).substring(0, index);
        if (!StringUtil.isEmpty(leftMapping)) {
          mappings.putToServerMappings("", leftMapping);
        }
      }
    }
  }

  private void addVariantsFromPath(Collection<Object> variants, String directoryName, String realPath) {
    VirtualFile fileByUrl = LocalFileSystem.getInstance().findFileByPath(realPath);
    if (fileByUrl != null) {
      if (fileByUrl.isDirectory()) {
        VirtualFile[] children = fileByUrl.getChildren();
        for (VirtualFile child : children) {
          if (child.isDirectory() || "cfc".equals(child.getExtension())) {
            variants.add(child);
          }
        }
      }
    }
  }

  public boolean isSoft() {
    // TODO: now - show no error if resolve failed, change when resolve will be fully implemented
    return true;
  }

  @Override
  public String getName() {
    final String referenceText = getCanonicalText();
    final int index = referenceText.lastIndexOf(".");
    return referenceText != null ? referenceText.substring(index >= 0 ? (index + 1) : 0) : "";
  }

  @Override
  public PsiType getPsiType() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public PsiReference getReference() {
    return this;
  }
}
