// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.IconManager;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PlatformIcons;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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
   * @param originalFile           = getContainingFile().getOriginalFile();
   */
  public static Collection<CfmlComponent> resolveFromQualifiedName(String componentQualifiedName, @NotNull CfmlFile originalFile) {
    List<CfmlComponent> result = new ArrayList<>();

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
          GlobalSearchScope searchScope = GlobalSearchScopesCore.directoryScope(directory, false);

          final Collection<CfmlComponent> components = CfmlIndex.getInstance(project).getComponentsByNameInScope(
            componentQualifiedName, searchScope);
          components.addAll(CfmlIndex.getInstance(project).getInterfacesByNameInScope(
            componentQualifiedName, searchScope));
          result.addAll(components);
        }
        else {
          final Collection<CfmlComponent> components = CfmlIndex.getInstance(project).getComponentsByName(
            componentQualifiedName);
          components.addAll(CfmlIndex.getInstance(project).getInterfacesByName(componentQualifiedName));
          result.addAll(components);
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
              GlobalSearchScope searchScope = GlobalSearchScopesCore.directoryScope(directory, false);

              final Collection<CfmlComponent> componentsFromGlobalScope = CfmlIndex.getInstance(project).getComponentsByNameInScope(
                componentName, searchScope);
              componentsFromGlobalScope.addAll(CfmlIndex.getInstance(project).getInterfacesByNameInScope(
                componentName, searchScope));

              result.addAll(componentsFromGlobalScope);
            }
          }
        }
      }
    }

    if (result.isEmpty()) {
      final Couple<String> prefixAndName = CfmlUtil.getPrefixAndName(componentQualifiedName);
      final String componentName = prefixAndName.getSecond();
      final CfmlImport cfmlImport = CfmlUtil.getImportByPrefix(originalFile, prefixAndName.getFirst());
      if (cfmlImport != null && !StringUtil.isEmpty(componentName)) {
        String libtag = cfmlImport.getImportString();
        final VirtualFile folder = CfmlUtil.findFileByLibTag(originalFile, libtag);
        if (folder != null && folder.isDirectory()) {
          final GlobalSearchScope scope = GlobalSearchScopesCore.directoryScope(originalFile.getProject(), folder, true);
          result.addAll(CfmlIndex.getInstance(originalFile.getProject()).getComponentsByNameInScope(componentName, scope));
        }
      }
    }
    return result;
  }

  private final ResolveCache.PolyVariantResolver<CfmlComponentReference> MY_RESOLVER =
    (expression, incompleteCode) -> {
      String componentQualifiedName;
      CfmlImport parentOfType = PsiTreeUtil.getParentOfType(expression, CfmlImport.class);
      if (parentOfType != null) {
        componentQualifiedName = getText();
      }
      else {
        componentQualifiedName = getComponentQualifiedName(getText());
      }
      PsiFile containingFile = getContainingFile().getOriginalFile();
      if (containingFile instanceof CfmlFile) {
        return CfmlResolveResult.create(resolveFromQualifiedName(componentQualifiedName, ((CfmlFile)containingFile)));
      }
      return ResolveResult.EMPTY_ARRAY;
    };

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
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

  @Override
  public @NotNull PsiElement getElement() {
    return myParent != null ? myParent : this;
  }

  @Override
  public @NotNull TextRange getRangeInElement() {
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

  @Override
  public PsiElement resolve() {
    ResolveResult[] results = multiResolve(false);
    if (results.length == 1) {
      return results[0].getElement();
    }
    return null;
  }

  @Override
  public @NotNull String getCanonicalText() {
    return getText();
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    throw new IncorrectOperationException("Not implemented yet");
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    throw new IncorrectOperationException("Not implemented yet");
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    // TODO: replace with fully qualified names

    if (element instanceof CfmlComponent && getCanonicalText().equals(((CfmlComponent)element).getName())) {
      return true;
    }
    return false;
  }

  @Override
  public Object @NotNull [] getVariants() {
    // final CfmlIndex cfmlIndex = CfmlIndex.getInstance(getProject());
    return buildVariants(getText(), getContainingFile(), getProject(), this, true);
  }

  public static Object @NotNull [] buildVariants(String text, PsiFile containingFile, final Project project,
                                                 @Nullable CfmlComponentReference reference,
                                                 final boolean forceQualify
                                       ) {
    Collection<Object> variants = new HashSet<>();

    String directoryName = "";
    if (text.contains(".")) {
      int i = text.lastIndexOf(".");
      directoryName = text.substring(0, i);
    }
    CfmlProjectConfiguration.State state = CfmlProjectConfiguration.getInstance(project).getState();
    CfmlMappingsConfig mappings = state != null ? state.getMapps().clone() : new CfmlMappingsConfig();

    adjustMappingsIfEmpty(mappings, project);
    if (reference != null) addFakeMappingsForImports(reference, mappings);

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
    containingFile = containingFile == null ? null : containingFile.getOriginalFile();
    if (containingFile instanceof CfmlFile cfmlContainingFile) {
      if (directoryName.isEmpty()) {
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

    return ContainerUtil.map2Array(variants, new Function<>() {
      static class DotInsertHandler implements InsertHandler<LookupElement> {
        @Override
        public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
          Document document = context.getDocument();
          int offset = context.getEditor().getCaretModel().getOffset();
          document.insertString(offset, ".");
          context.getEditor().getCaretModel().moveToOffset(offset + 1);
        }
      }

      @Override
      public Object fun(final Object object) {
        if (object instanceof VirtualFile element) {
          String elementNameWithoutExtension = element.getNameWithoutExtension();
          String name = forceQualify ?
                        finalDirectoryName + (finalDirectoryName.isEmpty() ? "" : ".") + elementNameWithoutExtension :
                        elementNameWithoutExtension;
          if (name.isEmpty()) name = element.getName();
          if (element.isDirectory()) {
            return LookupElementBuilder.create(name).withIcon(FOLDER_ICON)
              .withInsertHandler(new DotInsertHandler()).withCaseSensitivity(false);
          }
          else {
            Icon icon = IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Class);
            // choosing correct icon (class or interface)
            if (CfmlIndex.getInstance(project).getComponentsByNameInScope(elementNameWithoutExtension, GlobalSearchScope
              .fileScope(project, element)).size() == 1) {
              icon = IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Class);
            }
            else if (CfmlIndex.getInstance(project).getInterfacesByNameInScope(elementNameWithoutExtension, GlobalSearchScope
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
    if (!mappings.getServerMappings().isEmpty()) {
      return;
    }

    for (VirtualFile root : ProjectRootManager.getInstance(project).getContentRoots()) {
      mappings.putToServerMappings("", root.getPresentableUrl());
    }
  }

  private static void addFakeMappingsForImports(CfmlComponentReference ref, CfmlMappingsConfig mappings) {
    if (PsiTreeUtil.getParentOfType(ref, CfmlImport.class) != null) {
      // create fake mappings for imports
      CfmlFile file = ref.getContainingFile();
      Collection<String> importStrings = file.getImportStrings();
      for (String importString : importStrings) {
        final int index = importString.lastIndexOf('.');
        if (index == -1) {
          continue;
        }
        final String leftMapping = file.getComponentQualifiedName(importString).substring(0, index);
        if (!StringUtil.isEmpty(leftMapping)) {
          mappings.putToServerMappings("", leftMapping);
        }
      }
    }
  }

  private static void addVariantsFromPath(Collection<Object> variants, String directoryName, String realPath) {
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

  @Override
  public boolean isSoft() {
    // TODO: now - show no error if resolve failed, change when resolve will be fully implemented
    return true;
  }

  @Override
  public String getName() {
    final String referenceText = getCanonicalText();
    final int index = referenceText.lastIndexOf(".");
    return referenceText.substring(index >= 0 ? (index + 1) : 0);
  }

  @Override
  public PsiType getPsiType() {
    return null;
  }

  @Override
  public PsiReference getReference() {
    return this;
  }
}
