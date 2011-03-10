package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.mxml.LocalStyleWriter;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.search.JSClassSearch;
import com.intellij.openapi.module.impl.scopes.ModuleWithDependenciesScope;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Processor;

public class ModuleInfoUtil {
  public static void collectLocalStyleHolders(final ModuleInfo moduleInfo, final String flexSdkVersion, StringRegistry.StringWriter stringWriter) {
    final ModuleWithDependenciesScope moduleScope = new ModuleWithDependenciesScope(moduleInfo.getModule(), true, false, false, false);
    final LocalStyleWriter localStyleWriter = new LocalStyleWriter(stringWriter);
    final Processor<JSClass> processor = new Processor<JSClass>() {
      @Override
      public boolean process(JSClass jsClass) {
        PsiFile containingFile = jsClass.getNavigationElement().getContainingFile();
        if (containingFile instanceof XmlFile) {
          XmlTag rootTag = ((XmlFile) containingFile).getRootTag();
          if (rootTag != null) {
            for (final XmlTag subTag : rootTag.getSubTags()) {
              if (subTag.getNamespace().equals(JavaScriptSupportLoader.MXML_URI3) && subTag.getLocalName().equals(FlexPredefinedTagNames.STYLE)) {
                if (localStyleWriter.write(subTag, moduleInfo.getModule())) {
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
    
    final GlobalSearchScope moduleWithDependenciesAndLibrariesScope = moduleInfo.getModule().getModuleWithDependenciesAndLibrariesScope(false);
    if (flexSdkVersion.charAt(0) > '3') {
      JSClass sparkApplicationClass = ((JSClass) JSResolveUtil.findClassByQName("spark.components.Application", moduleWithDependenciesAndLibrariesScope));
      // it is not legal case, but user can use patched/modified Flex SDK
      if (sparkApplicationClass != null) {
        JSClassSearch.searchClassInheritors(new JSClassSearch.SearchParameters(sparkApplicationClass, true, moduleScope)).forEach(processor);
      }
    }
    
    JSClass mxApplicationClass = ((JSClass) JSResolveUtil.findClassByQName("mx.core.Application", moduleWithDependenciesAndLibrariesScope));
    // if null, mx.swc is not added to module dependencies
    if (mxApplicationClass != null) {
      JSClassSearch.searchClassInheritors(new JSClassSearch.SearchParameters(mxApplicationClass, true, moduleScope)).forEach(processor);
    }
  }
}
