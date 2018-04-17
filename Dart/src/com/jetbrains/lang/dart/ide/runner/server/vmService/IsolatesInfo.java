package com.jetbrains.lang.dart.ide.runner.server.vmService;

import gnu.trove.THashMap;
import org.dartlang.vm.service.element.IsolateRef;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class IsolatesInfo {

  public static class IsolateInfo {
    private final String myIsolateId;
    private final String myIsolateName;
    private boolean breakpointsSet = false;
    private boolean shouldInitialResume = false;

    private IsolateInfo(@NotNull final String isolateId, @NotNull final String isolateName) {
      myIsolateId = isolateId;
      myIsolateName = isolateName;
    }

    public String getIsolateId() {
      return myIsolateId;
    }

    public String getIsolateName() {
      return myIsolateName;
    }

    public String toString() {
      return myIsolateId + ": breakpointsSet=" + breakpointsSet + ", shouldInitialResume=" + shouldInitialResume;
    }
  }

  private final Map<String, IsolateInfo> myIsolateIdToInfoMap = new THashMap<>();

  public synchronized boolean addIsolate(@NotNull final IsolateRef isolateRef) {
    if (myIsolateIdToInfoMap.containsKey(isolateRef.getId())) {
      return false;
    }

    myIsolateIdToInfoMap.put(isolateRef.getId(), new IsolateInfo(isolateRef.getId(), isolateRef.getName()));

    return true;
  }

  public synchronized void setBreakpointsSet(@NotNull final IsolateRef isolateRef) {
    IsolateInfo info = myIsolateIdToInfoMap.get(isolateRef.getId());
    if (info != null) {
      info.breakpointsSet = true;
    }
  }

  public synchronized void setShouldInitialResume(@NotNull final IsolateRef isolateRef) {
    IsolateInfo info = myIsolateIdToInfoMap.get(isolateRef.getId());
    if (info != null) {
      info.shouldInitialResume = true;
    }
  }

  public synchronized boolean getShouldInitialResume(@NotNull final IsolateRef isolateRef) {
    IsolateInfo info = myIsolateIdToInfoMap.get(isolateRef.getId());
    if (info != null) {
      return info.breakpointsSet && info.shouldInitialResume;
    }
    else {
      return false;
    }
  }

  public synchronized void deleteIsolate(@NotNull final IsolateRef isolateRef) {
    myIsolateIdToInfoMap.remove(isolateRef.getId());
  }

  public synchronized Collection<IsolateInfo> getIsolateInfos() {
    return new ArrayList<>(myIsolateIdToInfoMap.values());
  }
}
