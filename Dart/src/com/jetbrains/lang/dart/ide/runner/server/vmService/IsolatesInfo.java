// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.runner.server.vmService;

import org.dartlang.vm.service.element.Isolate;
import org.dartlang.vm.service.element.IsolateRef;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class IsolatesInfo {
  public static final class IsolateInfo {
    private final String myIsolateId;
    private final String myIsolateName;
    private boolean breakpointsSet = false;
    private boolean shouldInitialResume = false;
    private CompletableFuture<Isolate> myCachedIsolate;

    private IsolateInfo(final @NotNull String isolateId, final @NotNull String isolateName) {
      myIsolateId = isolateId;
      myIsolateName = isolateName;
    }

    void invalidateCache() {
      myCachedIsolate = null;
    }

    CompletableFuture<Isolate> getCachedIsolate() {
      return myCachedIsolate;
    }

    void setCachedIsolate(CompletableFuture<Isolate> cachedIsolate) {
      myCachedIsolate = cachedIsolate;
    }

    public String getIsolateId() {
      return myIsolateId;
    }

    public String getIsolateName() {
      return myIsolateName;
    }

    @Override
    public String toString() {
      return myIsolateId + ": breakpointsSet=" + breakpointsSet + ", shouldInitialResume=" + shouldInitialResume;
    }
  }

  private final Map<String, IsolateInfo> myIsolateIdToInfoMap = new HashMap<>();

  public synchronized boolean addIsolate(final @NotNull IsolateRef isolateRef) {
    if (myIsolateIdToInfoMap.containsKey(isolateRef.getId())) {
      return false;
    }

    myIsolateIdToInfoMap.put(isolateRef.getId(), new IsolateInfo(isolateRef.getId(), isolateRef.getName()));

    return true;
  }

  public synchronized void setBreakpointsSet(final @NotNull IsolateRef isolateRef) {
    IsolateInfo info = myIsolateIdToInfoMap.get(isolateRef.getId());
    if (info != null) {
      info.breakpointsSet = true;
    }
  }

  public synchronized void setShouldInitialResume(final @NotNull IsolateRef isolateRef) {
    IsolateInfo info = myIsolateIdToInfoMap.get(isolateRef.getId());
    if (info != null) {
      info.shouldInitialResume = true;
    }
  }

  public synchronized boolean getShouldInitialResume(final @NotNull IsolateRef isolateRef) {
    IsolateInfo info = myIsolateIdToInfoMap.get(isolateRef.getId());
    if (info != null) {
      return info.breakpointsSet && info.shouldInitialResume;
    }
    else {
      return false;
    }
  }

  public synchronized void deleteIsolate(final @NotNull IsolateRef isolateRef) {
    myIsolateIdToInfoMap.remove(isolateRef.getId());
  }

  public synchronized void invalidateCache(String isolateId) {
    IsolateInfo info = myIsolateIdToInfoMap.get(isolateId);
    if (info != null) {
      info.invalidateCache();
    }
  }

  public synchronized @NotNull CompletableFuture<Isolate> getCachedIsolate(String isolateId,
                                                                           Supplier<? extends CompletableFuture<Isolate>> isolateSupplier) {
    IsolateInfo info = myIsolateIdToInfoMap.get(isolateId);
    if (info == null) {
      return CompletableFuture.completedFuture(null);
    }
    CompletableFuture<Isolate> cachedIsolate = info.getCachedIsolate();
    if (cachedIsolate != null) {
      return cachedIsolate;
    }
    cachedIsolate = isolateSupplier.get();
    info.setCachedIsolate(cachedIsolate);
    return cachedIsolate;
  }

  public synchronized Collection<IsolateInfo> getIsolateInfos() {
    return new ArrayList<>(myIsolateIdToInfoMap.values());
  }
}
