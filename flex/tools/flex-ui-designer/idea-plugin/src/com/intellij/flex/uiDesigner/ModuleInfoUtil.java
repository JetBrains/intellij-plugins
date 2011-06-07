package com.intellij.flex.uiDesigner;

import com.intellij.facet.FacetManager;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.libraries.Library;
import com.intellij.flex.uiDesigner.mxml.LocalStyleWriter;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.search.JSClassSearch;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Processor;

import java.util.Collection;

public class ModuleInfoUtil {
  public static void collectLocalStyleHolders(final ModuleInfo moduleInfo, final String flexSdkVersion,
                                              final StringRegistry.StringWriter stringWriter, final ProblemsHolder problemsHolder) {
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

    ApplicationManager.getApplication().runReadAction(FlexBuildConfiguration.APPLICATION.equals(flexBuildConfiguration.OUTPUT_TYPE) ?
                                                      new ApplicationLocalStyleFinder(stringWriter, moduleInfo, flexSdkVersion, problemsHolder) :
                                                      new Runnable() {
                                                        @Override
                                                        public void run() {
                                                          VirtualFile defaultsCss = null;
                                                          for (VirtualFile sourceRoot : ModuleRootManager.getInstance(
                                                            moduleInfo.getModule()).getSourceRoots(false)) {
                                                            if ((defaultsCss = sourceRoot.findChild(Library.DEFAULTS_CSS)) !=
                                                                null) {
                                                              break;
                                                            }
                                                          }

                                                          if (defaultsCss != null) {
                                                            moduleInfo.addLocalStyleHolder(
                                                              new LocalStyleHolder(defaultsCss, new CssWriter(stringWriter).write(
                                                                defaultsCss, module, problemsHolder)));
                                                          }
                                                        }
                                                      });
  }

  private static class ApplicationLocalStyleFinder implements Runnable {
    private final Processor<JSClass> processor;
    private LocalStyleWriter localStyleWriter;

    private final StringRegistry.StringWriter stringWriter;
    private final ModuleInfo moduleInfo;
    private final String flexSdkVersion;

    private LocalStyleWriter getStyleWriter(ProblemsHolder problemsHolder) {
      if (localStyleWriter == null) {
        localStyleWriter = new LocalStyleWriter(stringWriter, problemsHolder);
      }
      return localStyleWriter;
    }

    ApplicationLocalStyleFinder(StringRegistry.StringWriter stringWriter, final ModuleInfo moduleInfo, String flexSdkVersion,
                                final ProblemsHolder problemsHolder) {
      this.stringWriter = stringWriter;
      this.moduleInfo = moduleInfo;
      this.flexSdkVersion = flexSdkVersion;

      processor = new Processor<JSClass>() {
        @Override
        public boolean process(JSClass jsClass) {
          PsiFile containingFile = jsClass.getNavigationElement().getContainingFile();
          if (containingFile instanceof XmlFile) {
            XmlTag rootTag = ((XmlFile)containingFile).getRootTag();
            if (rootTag != null) {
              for (final XmlTag subTag : rootTag.getSubTags()) {
                if (subTag.getNamespace().equals(JavaScriptSupportLoader.MXML_URI3) &&
                    subTag.getLocalName().equals(FlexPredefinedTagNames.STYLE)) {
                  if (getStyleWriter(problemsHolder).write(subTag, moduleInfo.getModule())) {
                    moduleInfo.addLocalStyleHolder(new LocalStyleHolder(containingFile.getVirtualFile(), localStyleWriter.getData()));
                  }
                  break;
                }
              }
            }
          }
          return true;
        }
      };
    }

    @Override
    public void run() {
      final GlobalSearchScope moduleScope = moduleInfo.getModule().getModuleScope(false);
      final GlobalSearchScope moduleWithDependenciesAndLibrariesScope =
        moduleInfo.getModule().getModuleWithDependenciesAndLibrariesScope(false);
      if (flexSdkVersion.charAt(0) > '3') {
        JSClass sparkApplicationClass =
          ((JSClass)JSResolveUtil.findClassByQName("spark.components.Application", moduleWithDependenciesAndLibrariesScope));
        // it is not legal case, but user can use patched/modified Flex SDK
        if (sparkApplicationClass != null) {
          JSClassSearch.searchClassInheritors(
            new JSClassSearch.SearchParameters(sparkApplicationClass, true, moduleScope)).forEach(processor);
        }
      }

      JSClass mxApplicationClass = ((JSClass)JSResolveUtil.findClassByQName("mx.core.Application",
                                                                            moduleWithDependenciesAndLibrariesScope));
      // if null, mx.swc is not added to module dependencies
      if (mxApplicationClass != null) {
        JSClassSearch.searchClassInheritors(new JSClassSearch.SearchParameters(mxApplicationClass, true, moduleScope)).
          forEach(processor);
      }
    }
  }
}
