package com.jetbrains.actionscript.profiler.util;

import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.actionscript.profiler.sampler.SampleLocationResolver;

import java.util.Collection;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class LocationResolverUtil {
  private LocationResolverUtil() {
  }

  public static List<String> filterByScope(Collection<String> traces, final GlobalSearchScope scope) {
    return ContainerUtil.filter(traces, new Condition<String>() {
      @Override
      public boolean value(String trace) {
        SampleLocationResolver.LocationInfo l = SampleLocationResolver.buildMethodInfo(trace);
        final PsiElement classByQName = JSResolveUtil.findClassByQName(l.getClazz(), scope);
        boolean isAnonymous = "Function".equals(l.getClazz()) && "<anonymous>".equals(l.getName());
        return isAnonymous || classByQName != null;
      }
    });
  }
}
