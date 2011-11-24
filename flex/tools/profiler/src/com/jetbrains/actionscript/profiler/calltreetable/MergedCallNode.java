package com.jetbrains.actionscript.profiler.calltreetable;

import com.intellij.openapi.util.Pair;
import com.intellij.pom.Navigatable;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import com.jetbrains.actionscript.profiler.base.LazyNode;
import com.jetbrains.actionscript.profiler.base.NavigatableDataProducer;
import com.jetbrains.actionscript.profiler.calltree.CallTree;
import com.jetbrains.actionscript.profiler.sampler.Sample;
import com.jetbrains.actionscript.profiler.sampler.SampleLocationResolver;
import com.jetbrains.actionscript.profiler.util.LocationResolverUtil;
import com.jetbrains.actionscript.profiler.vo.CallInfo;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MergedCallNode<T extends Sample> extends LazyNode implements NavigatableDataProducer {
  private final CallTree callTree;
  private final String[] callFrames;
  private final boolean backTrace;
  private final GlobalSearchScope scope;
  private SampleLocationResolver sampleLocationResolver;

  public MergedCallNode(CallInfo callInfo,
                        CallTree callTree,
                        String[] callFrames,
                        boolean backTrace,
                        GlobalSearchScope scope) {
    setUserObject(callInfo);
    this.callTree = callTree;
    this.callFrames = callFrames;
    this.backTrace = backTrace;
    this.scope = scope;
  }

  public CallTree getCallTree() {
    return callTree;
  }

  @Nullable
  public CallInfo getCallInfo() {
    if (getUserObject() instanceof CallInfo) {
      return (CallInfo)getUserObject();
    }
    return null;
  }

  public String getFrame() {
    final CallInfo callInfo = getCallInfo();
    return callInfo != null ? callInfo.getFrameName() : "";
  }

  @Override
  protected void doLoadChildren() {
    String[] frames = Arrays.copyOf(callFrames, callFrames.length + 1);
    frames[frames.length - 1] = getFrame();

    Pair<Map<String, Long>, Map<String, Long>> countMaps;
    if (backTrace) {
      countMaps = callTree.getCallersTimeMaps(frames);
    }
    else {
      countMaps = callTree.getCalleesTimeMaps(ArrayUtil.reverseArray(frames));
    }
    final Map<String, Long> countMap = countMaps.getFirst();
    final Map<String, Long> selfCountMap = countMaps.getSecond();

    List<String> traces = LocationResolverUtil.filterByScope(countMap.keySet(), scope);
    ArrayList<CallInfo> callInfos = new ArrayList<CallInfo>();
    for (final String t : traces) {
      callInfos.add(new CallInfo(t, countMap.get(t), selfCountMap.get(t)));
    }

    for (int index = 0; index < callInfos.size(); ++index) {
      insert(new MergedCallNode<T>(callInfos.get(index), callTree, frames, backTrace, scope), index);
    }
  }

  @Override
  public String toString() {
    return getFrame();
  }

  @Override
  public Navigatable getNavigatableData() {
    if (sampleLocationResolver == null) {
      sampleLocationResolver = new SampleLocationResolver(getFrame(), scope);
    }
    return sampleLocationResolver;
  }
}
