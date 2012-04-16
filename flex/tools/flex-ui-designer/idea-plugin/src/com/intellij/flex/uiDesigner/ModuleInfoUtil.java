package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.css.LocalCssWriter;
import com.intellij.flex.uiDesigner.io.StringRegistry.StringWriter;
import com.intellij.flex.uiDesigner.libraries.Library;
import com.intellij.flex.uiDesigner.mxml.MxmlUtil;
import com.intellij.flex.uiDesigner.mxml.ProjectComponentReferenceCounter;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.search.JSClassSearch;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.css.CssFile;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Processor;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ModuleInfoUtil {
  public static boolean isApp(Module module) {
    return FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration().getNature().isApp();
  }

  public static void collectLocalStyle(final ModuleInfo moduleInfo, final String flexSdkVersion,
                                       final StringWriter stringWriter, final ProblemsHolder problemsHolder,
                                       ProjectComponentReferenceCounter projectComponentReferenceCounter, AssetCounter assetCounter) {
    final AccessToken token = ReadAction.start();
    try {
      if (moduleInfo.isApp()) {
        collectApplicationLocalStyle(moduleInfo, flexSdkVersion, problemsHolder, stringWriter, projectComponentReferenceCounter,
                                     assetCounter);
      }
      else {
        collectLibraryLocalStyle(moduleInfo, stringWriter, problemsHolder, projectComponentReferenceCounter, assetCounter);
      }
    }
    finally {
      token.finish();
    }
  }

  private static void collectLibraryLocalStyle(ModuleInfo moduleInfo,
                                               StringWriter stringWriter,
                                               ProblemsHolder problemsHolder,
                                               ProjectComponentReferenceCounter unregisteredComponentReferences,
                                               AssetCounter assetCounter) {
    VirtualFile defaultsCss = null;
    for (VirtualFile sourceRoot : ModuleRootManager.getInstance(moduleInfo.getModule()).getSourceRoots(false)) {
      if ((defaultsCss = sourceRoot.findChild(Library.DEFAULTS_CSS)) != null) {
        break;
      }
    }

    if (defaultsCss != null) {
      final LocalCssWriter cssWriter = new LocalCssWriter(stringWriter, problemsHolder, unregisteredComponentReferences, assetCounter);
      moduleInfo.addLocalStyleHolder(new LocalStyleHolder(defaultsCss, cssWriter.write(defaultsCss, moduleInfo.getModule())));
    }
  }

  private static void collectApplicationLocalStyle(final ModuleInfo moduleInfo, String flexSdkVersion, final ProblemsHolder problemsHolder,
                                                   StringWriter stringWriter, ProjectComponentReferenceCounter projectComponentReferenceCounter,
                                                   final AssetCounter assetCounter) {
    final GlobalSearchScope moduleWithDependenciesAndLibrariesScope =
        moduleInfo.getModule().getModuleWithDependenciesAndLibrariesScope(false);

    final List<JSClass> holders = new ArrayList<JSClass>(2);
    if (flexSdkVersion.charAt(0) > '3') {
      JSClass clazz = ((JSClass)JSResolveUtil.findClassByQName(FlexCommonTypeNames.SPARK_APPLICATION, moduleWithDependenciesAndLibrariesScope));
      // it is not legal case, but user can use patched/modified Flex SDK
      if (clazz != null) {
        holders.add(clazz);
      }
    }

    JSClass mxApplicationClass = ((JSClass)JSResolveUtil.findClassByQName(FlexCommonTypeNames.MX_APPLICATION, moduleWithDependenciesAndLibrariesScope));
    // if null, mx.swc is not added to module dependencies
    if (mxApplicationClass != null) {
      holders.add(mxApplicationClass);
    }

    if (holders.isEmpty()) {
      return;
    }

    final StyleTagWriter styleTagWriter = new StyleTagWriter(new LocalCssWriter(stringWriter, problemsHolder,
                                                                                projectComponentReferenceCounter, assetCounter));
    final Processor<JSClass> processor = new Processor<JSClass>() {
      @Override
      public boolean process(JSClass jsClass) {
        PsiFile psiFile = jsClass.getNavigationElement().getContainingFile();
        if (!(psiFile instanceof XmlFile)) {
          return true;
        }

        XmlTag rootTag = ((XmlFile)psiFile).getRootTag();
        if (rootTag == null) {
          return true;
        }

        // injection works only for commited documents
        MxmlUtil.getDocumentAndWaitIfNotComitted(psiFile);

        final VirtualFile virtualFile = psiFile.getVirtualFile();
        problemsHolder.setCurrentFile(virtualFile);
        try {
          // IDEA-73558
          for (final XmlTag subTag : rootTag.getSubTags()) {
            if (subTag.getNamespace().equals(JavaScriptSupportLoader.MXML_URI3) &&
                subTag.getLocalName().equals(FlexPredefinedTagNames.STYLE)) {
              try {
                LocalStyleHolder localStyleHolder = styleTagWriter.write(subTag, moduleInfo.getModule(), virtualFile);
                if (localStyleHolder != null) {
                  moduleInfo.addLocalStyleHolder(localStyleHolder);
                }
              }
              catch (InvalidPropertyException e) {
                problemsHolder.add(e);
              }
            }
          }
        }
        finally {
          problemsHolder.setCurrentFile(null);
        }
        return true;
      }
    };

    final GlobalSearchScope moduleScope = moduleInfo.getModule().getModuleScope(false);
    for (JSClass holder : holders) {
      JSClassSearch.searchClassInheritors(new JSClassSearch.SearchParameters(holder, true, moduleScope)).forEach(processor);
    }
  }

  private static class StyleTagWriter {
    private final LocalCssWriter cssWriter;
    private final THashMap<VirtualFile, ExternalLocalStyleHolder> externalLocalStyleHolders = new THashMap<VirtualFile, ExternalLocalStyleHolder>();

    StyleTagWriter(LocalCssWriter localCssWriter) {
      cssWriter = localCssWriter;
    }

    @Nullable
    public LocalStyleHolder write(XmlTag tag, Module module, VirtualFile userVirtualFile) throws InvalidPropertyException {
      CssFile cssFile = null;
      XmlAttribute source = tag.getAttribute("source");
      if (source != null) {
        XmlAttributeValue valueElement = source.getValueElement();
        if (valueElement != null) {
          final PsiFileSystemItem psiFile = InjectionUtil.getReferencedPsiFile(valueElement);
          if (psiFile instanceof CssFile) {
            cssFile = (CssFile)psiFile;
          }
          else {
            throw new InvalidPropertyException(valueElement, "embed.source.is.not.css.file", psiFile.getName());
          }

          final VirtualFile virtualFile = cssFile.getVirtualFile();
          final ExternalLocalStyleHolder existingLocalStyleHolder = externalLocalStyleHolders.get(virtualFile);
          if (existingLocalStyleHolder == null) {
            ExternalLocalStyleHolder localStyleHolder = new ExternalLocalStyleHolder(virtualFile, cssWriter.write(cssFile, module), userVirtualFile);
            externalLocalStyleHolders.put(virtualFile, localStyleHolder);
            return localStyleHolder;
          }
          else {
            existingLocalStyleHolder.addUser(userVirtualFile);
            return null;
          }
        }
      }
      else {
        PsiElement host = MxmlUtil.getInjectedHost(tag);
        if (host != null) {
          MyInjectedPsiVisitor visitor = new MyInjectedPsiVisitor(host);
          InjectedLanguageUtil.enumerate(host, visitor);
          cssFile = visitor.getCssFile();
        }
      }

      if (cssFile == null) {
        return null;
      }
      else {
        return new LocalStyleHolder(InjectedLanguageUtil.getTopLevelFile(cssFile).getVirtualFile(), cssWriter.write(cssFile, module));
      }
    }

    private static class MyInjectedPsiVisitor implements PsiLanguageInjectionHost.InjectedPsiVisitor {
      private final PsiElement host;
      private boolean visited;

      private CssFile cssFile;

      public MyInjectedPsiVisitor(PsiElement host) {
        this.host = host;
      }

      @Nullable
      public CssFile getCssFile() {
        return cssFile;
      }

      public void visit(@NotNull PsiFile injectedPsi, @NotNull List<PsiLanguageInjectionHost.Shred> places) {
        assert !visited;
        visited = true;

        assert places.size() == 1;
        assert places.get(0).getHost() == host;
        cssFile = (CssFile)injectedPsi;
      }
    }
  }
}