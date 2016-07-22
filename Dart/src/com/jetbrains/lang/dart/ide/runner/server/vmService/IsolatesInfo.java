package com.jetbrains.lang.dart.ide.runner.server.vmService;

import gnu.trove.THashMap;
import org.dartlang.vm.service.element.IsolateRef;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class IsolatesInfo {

  public static class IsolateInfo {
    private final String myIsolateId;
    private final String myIsolateName;

    public IsolateInfo(@NotNull final String isolateId, @NotNull final String isolateName) {
      myIsolateId = isolateId;
      myIsolateName = isolateName;
    }

    public String getIsolateId() {
      return myIsolateId;
    }

    public String getIsolateName() {
      return myIsolateName;
    }
  }

  private final Map<String, IsolateInfo> myIsolateIdToInfoMap = Collections.synchronizedMap(new THashMap<String, IsolateInfo>());

  public boolean addIsolate(@NotNull final IsolateRef isolateRef) {
    return myIsolateIdToInfoMap.put(isolateRef.getId(), new IsolateInfo(isolateRef.getId(), isolateRef.getName())) == null;
  }

  public void deleteIsolate(@NotNull final IsolateRef isolateRef) {
    myIsolateIdToInfoMap.remove(isolateRef.getId());
  }

  public Collection<IsolateInfo> getIsolateInfos() {
    return new ArrayList<>(myIsolateIdToInfoMap.values());
  }
}
