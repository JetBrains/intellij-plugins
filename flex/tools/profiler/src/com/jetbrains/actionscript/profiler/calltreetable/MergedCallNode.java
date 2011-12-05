package com.jetbrains.actionscript.profiler.calltreetable;

import com.intellij.openapi.util.Pair;
import com.intellij.pom.Navigatable;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import com.jetbrains.actionscript.profiler.base.FrameInfoProducer;
import com.jetbrains.actionscript.profiler.base.LazyNode;
import com.jetbrains.actionscript.profiler.base.NavigatableDataProducer;
import com.jetbrains.actionscript.profiler.calltree.CallTree;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;
import com.jetbrains.actionscript.profiler.sampler.Sample;
import com.jetbrains.actionscript.profiler.sampler.SampleLocationResolver;
import com.jetbrains.actionscript.profiler.util.LocationResolverUtil;
import com.jetbrains.actionscript.profiler.vo.CallInfo;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MergedCallNode<T extends Sample> extends LazyNode implements NavigatableDataProducer, FrameInfoProducer {
  private final CallTree callTree;
  private final FrameInfo[] callFrames;
  private final boolean backTrace;
  private final GlobalSearchScope scope;
  private SampleLocationResolver sampleLocationResolver;

  public MergedCallNode(CallInfo callInfo,
                        CallTree callTree,
                        FrameInfo[] callFrames,
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

  @Nullable
  public FrameInfo getFrameInfo() {
    final CallInfo callInfo = getCallInfo();
    return callInfo != null ? callInfo.getFrameInfo() : null;
  }

  @Override
  protected void doLoadChildren() {
    FrameInfo[] frames = Arrays.copyOf(callFrames, callFrames.length + 1);
    frames[frames.length - 1] = getFrameInfo();

    Pair<Map<FrameInfo, Long>, Map<FrameInfo, Long>> countMaps;
    if (backTrace) {
      countMaps = callTree.getCallersTimeMaps(frames);
    }
    else {
      countMaps = callTree.getCalleesTimeMaps(ArrayUtil.reverseArray(frames));
    }
    final Map<FrameInfo, Long> countMap = countMaps.getFirst();
    final Map<FrameInfo, Long> selfCountMap = countMaps.getSecond();

    List<FrameInfo> traces = LocationResolverUtil.filterByScope(countMap.keySet(), scope);
    ArrayList<CallInfo> callInfos = new ArrayList<CallInfo>();
    for (final FrameInfo t : traces) {
      callInfos.add(new CallInfo(t, countMap.get(t), selfCountMap.get(t)));
    }

    for (int index = 0; index < callInfos.size(); ++index) {
      insert(new MergedCallNode<T>(callInfos.get(index), callTree, frames, backTrace, scope), index);
    }
  }

  @Override
  public String toString() {
    FrameInfo frameInfo = getFrameInfo();
    return frameInfo != null ? frameInfo.toString() : "";
  }

  @Override
  public Navigatable getNavigatable() {
    if (sampleLocationResolver == null) {
      sampleLocationResolver = new SampleLocationResolver(getFrameInfo(), scope);
    }
    return sampleLocationResolver;
  }
}
