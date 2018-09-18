package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.css.LocalCssWriter;
import com.intellij.flex.uiDesigner.io.StringRegistry.StringWriter;
import com.intellij.flex.uiDesigner.libraries.Library;
import com.intellij.flex.uiDesigner.mxml.MxmlUtil;
import com.intellij.flex.uiDesigner.mxml.ProjectComponentReferenceCounter;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.search.JSClassSearch;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Processor;
import com.intellij.util.concurrency.Semaphore;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleInfoUtil {
  public static boolean isApp(Module module) {
    return FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration().getNature().isApp();
  }

  public static List<LocalStyleHolder> collectLocalStyle(final ModuleInfo moduleInfo, final String flexSdkVersion,
                                                         final StringWriter stringWriter, final ProblemsHolder problemsHolder,
                                                         ProjectComponentReferenceCounter projectComponentReferenceCounter,
                                                         AssetCounter assetCounter) {
    Project project = moduleInfo.getModule().getProject();
    DumbService dumbService = DumbService.getInstance(project);
    if (dumbService.isDumb()) {
      dumbService.waitForSmartMode();
    }

    final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
    if (psiDocumentManager.hasUncommitedDocuments()) {
      final Semaphore semaphore = new Semaphore();
      semaphore.down();
      Application application = ApplicationManager.getApplication();
      LogMessageUtil.LOG.assertTrue(!application.isReadAccessAllowed());

      application.invokeLater(() -> psiDocumentManager.performWhenAllCommitted(() -> semaphore.up()));
      semaphore.waitFor();
    }

    return ReadAction.compute(()->{
      if (moduleInfo.isApp()) {
        return collectApplicationLocalStyle(moduleInfo.getModule(), flexSdkVersion, problemsHolder, stringWriter, projectComponentReferenceCounter,
                                     assetCounter);
      }
      else {
        return collectLibraryLocalStyle(moduleInfo.getModule(), stringWriter, problemsHolder, projectComponentReferenceCounter, assetCounter);
      }});
  }

  @Nullable
  private static List<LocalStyleHolder> collectLibraryLocalStyle(Module module,
                                                                 StringWriter stringWriter,
                                                                 ProblemsHolder problemsHolder,
                                                                 ProjectComponentReferenceCounter unregisteredComponentReferences,
                                                                 AssetCounter assetCounter) {
    VirtualFile defaultsCss = null;
    for (VirtualFile sourceRoot : ModuleRootManager.getInstance(module).getSourceRoots(false)) {
      if ((defaultsCss = sourceRoot.findChild(Library.DEFAULTS_CSS)) != null) {
        break;
      }
    }

    if (defaultsCss != null) {
      byte[] data = new LocalCssWriter(stringWriter, problemsHolder, unregisteredComponentReferences, assetCounter).write(defaultsCss, module);
      if (data != null) {
        return Collections.singletonList(new LocalStyleHolder(defaultsCss, data));
      }
    }
    return null;
  }

  @Nullable
  private static List<LocalStyleHolder> collectApplicationLocalStyle(final Module module,
                                                                     String flexSdkVersion,
                                                                     final ProblemsHolder problemsHolder,
                                                                     StringWriter stringWriter,
                                                                     ProjectComponentReferenceCounter projectComponentReferenceCounter,
                                                                     final AssetCounter assetCounter) {
    GlobalSearchScope moduleWithDependenciesAndLibrariesScope = module.getModuleWithDependenciesAndLibrariesScope(false);
    final List<JSClass> holders = new ArrayList<>(2);
    if (flexSdkVersion.charAt(0) > '3') {
      JSClass clazz = ((JSClass)ActionScriptClassResolver
        .findClassByQNameStatic(FlexCommonTypeNames.SPARK_APPLICATION, moduleWithDependenciesAndLibrariesScope));
      // it is not legal case, but user can use patched/modified Flex SDK
      if (clazz != null) {
        holders.add(clazz);
      }
    }

    JSClass mxApplicationClass = ((JSClass)ActionScriptClassResolver
      .findClassByQNameStatic(FlexCommonTypeNames.MX_APPLICATION, moduleWithDependenciesAndLibrariesScope));
    // if null, mx.swc is not added to module dependencies
    if (mxApplicationClass != null) {
      holders.add(mxApplicationClass);
    }

    if (holders.isEmpty()) {
      return null;
    }

    final StyleTagWriter styleTagWriter =
      new StyleTagWriter(new LocalCssWriter(stringWriter, problemsHolder, projectComponentReferenceCounter, assetCounter));
    final List<LocalStyleHolder> result = new ArrayList<>();
    final Processor<JSClass> processor = jsClass -> {
      PsiFile psiFile = jsClass.getNavigationElement().getContainingFile();
      if (!(psiFile instanceof XmlFile)) {
        return true;
      }

      XmlTag rootTag = ((XmlFile)psiFile).getRootTag();
      if (rootTag == null) {
        return true;
      }

      final VirtualFile virtualFile = psiFile.getVirtualFile();
      problemsHolder.setCurrentFile(virtualFile);
      try {
        // IDEA-73558
        for (final XmlTag subTag : rootTag.getSubTags()) {
          if (subTag.getNamespace().equals(JavaScriptSupportLoader.MXML_URI3) &&
              subTag.getLocalName().equals(FlexPredefinedTagNames.STYLE)) {
            try {
              LocalStyleHolder localStyleHolder = styleTagWriter.write(subTag, module, virtualFile);
              if (localStyleHolder != null) {
                result.add(localStyleHolder);
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
    };

    final GlobalSearchScope moduleScope = module.getModuleScope(false);
    for (JSClass holder : holders) {
      JSClassSearch.searchClassInheritors(holder, true, moduleScope).forEach(processor);
    }
    return result;
  }

  private static class StyleTagWriter {
    private final LocalCssWriter cssWriter;
    private final THashMap<VirtualFile, ExternalLocalStyleHolder> externalLocalStyleHolders = new THashMap<>();

    StyleTagWriter(LocalCssWriter localCssWriter) {
      cssWriter = localCssWriter;
    }

    @Nullable
    public LocalStyleHolder write(@NotNull XmlTag tag, @NotNull Module module, @NotNull VirtualFile userVirtualFile) throws InvalidPropertyException {
      XmlAttribute source = tag.getAttribute("source");
      if (source == null) {
        PsiElement host = MxmlUtil.getInjectedHost(tag);
        if (host == null) {
          return null;
        }

        MyInjectedPsiVisitor visitor = new MyInjectedPsiVisitor(host);
        InjectedLanguageManager.getInstance(host.getProject()).enumerate(host, visitor);
        StylesheetFile stylesheetFile = visitor.getStylesheetFile();
        byte[] data = stylesheetFile == null ? null : cssWriter.write(stylesheetFile, module);
        return data == null ? null : new LocalStyleHolder(InjectedLanguageManager.getInstance(stylesheetFile.getProject()).getTopLevelFile(stylesheetFile).getVirtualFile(), data);
      }
      else {
        XmlAttributeValue valueElement = source.getValueElement();
        if (valueElement == null) {
          return null;
        }

        PsiFileSystemItem psiFile = InjectionUtil.getReferencedPsiFile(valueElement);
        if (!(psiFile instanceof StylesheetFile)) {
          throw new InvalidPropertyException(valueElement, "embed.source.is.not.css.file", psiFile.getName());
        }

        StylesheetFile stylesheetFile = (StylesheetFile)psiFile;
        VirtualFile virtualFile = stylesheetFile.getVirtualFile();
        ExternalLocalStyleHolder existingLocalStyleHolder = externalLocalStyleHolders.get(virtualFile);
        if (existingLocalStyleHolder == null) {
          byte[] data = cssWriter.write(stylesheetFile, module);
          if (data == null) {
            return null;
          }

          ExternalLocalStyleHolder localStyleHolder = new ExternalLocalStyleHolder(virtualFile, data, userVirtualFile);
          externalLocalStyleHolders.put(virtualFile, localStyleHolder);
          return localStyleHolder;
        }
        else {
          existingLocalStyleHolder.addUser(userVirtualFile);
          return null;
        }
      }
    }

    private static class MyInjectedPsiVisitor implements PsiLanguageInjectionHost.InjectedPsiVisitor {
      private final PsiElement host;
      private boolean visited;

      private StylesheetFile stylesheetFile;

      MyInjectedPsiVisitor(PsiElement host) {
        this.host = host;
      }

      @Nullable
      public StylesheetFile getStylesheetFile() {
        return stylesheetFile;
      }

      @Override
      public void visit(@NotNull PsiFile injectedPsi, @NotNull List<PsiLanguageInjectionHost.Shred> places) {
        assert !visited;
        visited = true;

        assert places.size() == 1;
        assert places.get(0).getHost() == host;
        stylesheetFile = (StylesheetFile)injectedPsi;
      }
    }
  }
}