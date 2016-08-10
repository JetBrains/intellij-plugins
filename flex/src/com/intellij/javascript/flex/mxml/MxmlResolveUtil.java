package com.intellij.javascript.flex.mxml;

import com.intellij.lang.javascript.psi.JSField;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataCache;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.SmartList;

import java.util.List;


public class MxmlResolveUtil {

  public static boolean processImplicitFields(PsiScopeProcessor processor, PsiFile containingFile,
                                              ImplicitFieldProvider cachedPredefinedVars,
                                              Key<CachedValue<List<JSField>>> cachedPredefinedVarsKey) {
    for(JSField var: cachedPredefinedVars.get(cachedPredefinedVarsKey, (XmlFile)containingFile, null).getValue()) {
      if(!processor.execute(var, ResolveState.initial())) return false;
    }
    return true;
  }

  public abstract static class ImplicitFieldProvider extends UserDataCache<CachedValue<List<JSField>>, XmlFile, Object> {
    @Override
    protected CachedValue<List<JSField>> compute(final XmlFile xmlFile, Object p) {
      return CachedValuesManager.getManager(xmlFile.getProject()).createCachedValue(() -> {
        SmartList<JSField> vars = new SmartList<>();
        doComputeVars(vars, xmlFile);
        return new CachedValueProvider.Result<List<JSField>>(vars, xmlFile);
      }, false);
    }

    protected abstract void doComputeVars(List<JSField> vars, XmlFile xmlFile);
  }
}
