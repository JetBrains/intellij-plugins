package com.jetbrains.actionscript.profiler.util;

import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class ResolveUtil {
  private ResolveUtil() {
  }

  @Nullable
  public static PsiElement findClassByQName(String name, @Nullable GlobalSearchScope scope) {
    if (scope == null) {
      return null;
    }
    return JSResolveUtil.findClassByQName(name, scope);
  }

  public static List<FrameInfo> filterByScope(Collection<FrameInfo> traces, final GlobalSearchScope scope) {
    if (scope == null) {
      return new ArrayList<FrameInfo>(traces);
    }
    return ContainerUtil.filter(traces, new Condition<FrameInfo>() {
      @Override
      public boolean value(FrameInfo frameInfo) {
        final String qName = frameInfo.isAnonymous() ? frameInfo.getQNameByFile() : frameInfo.getQName();
        if (qName == null) {
          return false;
        }
        return ResolveUtil.findClassByQName(qName, scope) != null;
      }
    });
  }
}
