package com.intellij.flex.uiDesigner;

import com.intellij.facet.FacetManager;
import com.intellij.flex.uiDesigner.css.LocalCssWriter;
import com.intellij.flex.uiDesigner.io.StringRegistry.StringWriter;
import com.intellij.flex.uiDesigner.libraries.Library;
import com.intellij.flex.uiDesigner.mxml.StyleTagWriter;
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
import com.intellij.openapi.module.ModuleType;
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
                                              final StringWriter stringWriter, final ProblemsHolder problemsHolder,
                                              List<XmlFile> unregisteredDocumentReferences, AssetCounter assetCounter) {
    final Module module = moduleInfo.getModule();
    final FlexBuildConfiguration flexBuildConfiguration;
    if (ModuleType.get(module) instanceof FlexModuleType) {
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
        collectApplicationLocalStyle(moduleInfo, flexSdkVersion, problemsHolder, stringWriter, unregisteredDocumentReferences,
                                     assetCounter);
      }
      else {
        collectLibraryLocalStyle(module, moduleInfo, stringWriter, problemsHolder, unregisteredDocumentReferences, assetCounter);
      }
    }
    finally {
      token.finish();
    }
  }

  private static void collectLibraryLocalStyle(Module module, ModuleInfo moduleInfo, StringWriter stringWriter,
                                               ProblemsHolder problemsHolder, List<XmlFile> unregisteredDocumentReferences,
                                               AssetCounter assetCounter) {
    VirtualFile defaultsCss = null;
    for (VirtualFile sourceRoot : ModuleRootManager.getInstance(moduleInfo.getModule()).getSourceRoots(false)) {
      if ((defaultsCss = sourceRoot.findChild(Library.DEFAULTS_CSS)) != null) {
        break;
      }
    }

    if (defaultsCss != null) {
      final LocalCssWriter cssWriter = new LocalCssWriter(stringWriter, problemsHolder, unregisteredDocumentReferences);
      moduleInfo.addLocalStyleHolder(new LocalStyleHolder(defaultsCss, cssWriter.write(defaultsCss, module)));
      assetCounter.append(cssWriter.getAssetCounter());
    }
  }

  private static void collectApplicationLocalStyle(final ModuleInfo moduleInfo, String flexSdkVersion, final ProblemsHolder problemsHolder,
                                                   StringWriter stringWriter, List<XmlFile> unregisteredDocumentReferences,
                                                   final AssetCounter assetCounter) {
    final GlobalSearchScope moduleWithDependenciesAndLibrariesScope =
        moduleInfo.getModule().getModuleWithDependenciesAndLibrariesScope(false);

    final List<JSClass> holders = new ArrayList<JSClass>(2);
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
            problemsHolder.setCurrentFile(containingFile.getVirtualFile());
            try {
              for (final XmlTag subTag : rootTag.getSubTags()) {
                if (subTag.getNamespace().equals(JavaScriptSupportLoader.MXML_URI3) &&
                    subTag.getLocalName().equals(FlexPredefinedTagNames.STYLE)) {
                  try {
                    byte[] data = localStyleWriter.write(subTag, moduleInfo.getModule());
                    if (data != null) {
                      moduleInfo.addLocalStyleHolder(new LocalStyleHolder(containingFile.getVirtualFile(), data));
                    }
                  }
                  catch (InvalidPropertyException e) {
                    problemsHolder.add(e);
                  }
                  break;
                }
              }
            }
            finally {
              problemsHolder.setCurrentFile(null);
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

    assetCounter.append(localStyleWriter.getRequiredAssetsInfo());
  }
}
