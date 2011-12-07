package com.jetbrains.actionscript.profiler.util;

import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;

import java.util.Collection;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class LocationResolverUtil {
  private LocationResolverUtil() {
  }

  public static List<FrameInfo> filterByScope(Collection<FrameInfo> traces, final GlobalSearchScope scope) {
    return ContainerUtil.filter(traces, new Condition<FrameInfo>() {
      @Override
      public boolean value(FrameInfo frameInfo) {
        final String qName = frameInfo.isAnonymous() ? frameInfo.getQNameByFile() : frameInfo.getQName();
        if (qName == null) {
          return false;
        }
        return JSResolveUtil.findClassByQName(qName, scope) != null;
      }
    });
  }
}
