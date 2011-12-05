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
        if(frameInfo.isAnonymous()){
          return JSResolveUtil.findClassByQName(frameInfo.getQNameByFile(), scope) != null;
        }
        return JSResolveUtil.findClassByQName(frameInfo.getQName(), scope) != null;
      }
    });
  }
}
