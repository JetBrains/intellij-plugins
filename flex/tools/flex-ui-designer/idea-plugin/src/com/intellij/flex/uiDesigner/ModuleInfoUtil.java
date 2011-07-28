package com.intellij.flex.uiDesigner;

import com.intellij.facet.FacetManager;
import com.intellij.flex.uiDesigner.css.LocalCssWriter;
import com.intellij.flex.uiDesigner.mxml.StyleTagWriter;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.libraries.Library;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.search.JSClassSearch;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ModuleInfoUtil {
  public static void collectLocalStyleHolders(final ModuleInfo moduleInfo, final String flexSdkVersion,
      final StringRegistry.StringWriter stringWriter, final ProblemsHolder problemsHolder, List<XmlFile> unregisteredDocumentReferences) {
    final Module module = moduleInfo.getModule();
    final FlexBuildConfiguration flexBuildConfiguration;
    if (module.getModuleType() instanceof FlexModuleType) {
      flexBuildConfiguration = FlexBuildConfiguration.getInstance(module);
    }
    else {
      final Collection<FlexFacet> flexFacets = FacetManager.getInstance(module).getFacetsByType(FlexFacet.ID);
      assert !flexFacets.isEmpty();
      flexBuildConfiguration = FlexBuildConfiguration.getInstance(flexFacets.iterator().next());
    }

    AccessToken token = ReadAction.start();
    try {
      if (FlexBuildConfiguration.APPLICATION.equals(flexBuildConfiguration.OUTPUT_TYPE)) {
        collectApplicationLocalStyle(moduleInfo, flexSdkVersion, problemsHolder, stringWriter, unregisteredDocumentReferences);
      }
      else {
        collectLibraryLocalStyle(module, moduleInfo, stringWriter, problemsHolder, unregisteredDocumentReferences);
      }
    }
    finally {
      token.finish();
    }
  }

  private static void collectLibraryLocalStyle(Module module, ModuleInfo moduleInfo, StringRegistry.StringWriter stringWriter,
      ProblemsHolder problemsHolder, List<XmlFile> unregisteredDocumentReferences) {
    VirtualFile defaultsCss = null;
    for (VirtualFile sourceRoot : ModuleRootManager.getInstance(moduleInfo.getModule()).getSourceRoots(false)) {
      if ((defaultsCss = sourceRoot.findChild(Library.DEFAULTS_CSS)) != null) {
        break;
      }
    }

    if (defaultsCss != null) {
      moduleInfo.addLocalStyleHolder(
          new LocalStyleHolder(defaultsCss, new LocalCssWriter(stringWriter, unregisteredDocumentReferences).write(defaultsCss, module, problemsHolder)));
    }
  }

  private static void collectApplicationLocalStyle(final ModuleInfo moduleInfo, String flexSdkVersion, final ProblemsHolder problemsHolder,
      StringRegistry.StringWriter stringWriter, List<XmlFile> unregisteredDocumentReferences) {
    final GlobalSearchScope moduleWithDependenciesAndLibrariesScope =
        moduleInfo.getModule().getModuleWithDependenciesAndLibrariesScope(false);

    List<JSClass> holders = new ArrayList<JSClass>(2);
    if (flexSdkVersion.charAt(0) > '3') {
      JSClass clazz = ((JSClass)JSResolveUtil.findClassByQName("spark.components.Application", moduleWithDependenciesAndLibrariesScope));
      // it is not legal case, but user can use patched/modified Flex SDK
      if (clazz != null) {
        holders.add(clazz);
      }
    }

    JSClass mxApplicationClass = ((JSClass)JSResolveUtil.findClassByQName("mx.core.Application", moduleWithDependenciesAndLibrariesScope));
    // if null, mx.swc is not added to module dependencies
    if (mxApplicationClass != null) {
      holders.add(mxApplicationClass);
    }

    if (holders.isEmpty()) {
      return;
    }

    final StyleTagWriter localStyleWriter = new StyleTagWriter(stringWriter, problemsHolder, unregisteredDocumentReferences);
    final Processor<JSClass> processor = new Processor<JSClass>() {
      @Override
      public boolean process(JSClass jsClass) {
        PsiFile containingFile = jsClass.getNavigationElement().getContainingFile();
        if (containingFile instanceof XmlFile) {
          XmlTag rootTag = ((XmlFile)containingFile).getRootTag();
          if (rootTag != null) {
            for (final XmlTag subTag : rootTag.getSubTags()) {
              if (subTag.getNamespace().equals(JavaScriptSupportLoader.MXML_URI3) &&
                  subTag.getLocalName().equals(FlexPredefinedTagNames.STYLE)) {
                byte[] data = localStyleWriter.write(subTag, moduleInfo.getModule());
                if (data != null) {
                  moduleInfo.addLocalStyleHolder(new LocalStyleHolder(containingFile.getVirtualFile(), data));
                }
                break;
              }
            }
          }
        }
        return true;
      }
    };

    final GlobalSearchScope moduleScope = moduleInfo.getModule().getModuleScope(false);
    for (JSClass holder : holders) {
      JSClassSearch.searchClassInheritors(new JSClassSearch.SearchParameters(holder, true, moduleScope)).forEach(processor);
    }
  }
}
