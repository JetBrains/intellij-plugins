package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.util.containers.ArrayListSet;
import gnu.trove.THashMap;
import org.dartlang.vm.service.element.Isolate;
import org.dartlang.vm.service.element.IsolateRef;
import org.dartlang.vm.service.element.Library;
import org.dartlang.vm.service.element.ScriptRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class IsolatesInfo {

  public static class IsolateInfo {
    private final String myIsolateId;
    private final String myName;
    private final Collection<Library> myLibraries = new ArrayListSet<Library>();
    private final Map<String, String> myScriptIdToUriMap = new THashMap<String, String>();
    private final Map<String, String> myUriToScriptIdMap = new THashMap<String, String>();

    public IsolateInfo(@NotNull final String isolateId, @NotNull final String name) {
      myIsolateId = isolateId;
      myName = name;
    }

    private void addLibrary(@NotNull final Library library) {
      myLibraries.add(library);
      for (ScriptRef scriptRef : library.getScripts()) {
        myScriptIdToUriMap.put(scriptRef.getId(), scriptRef.getUri());
        myUriToScriptIdMap.put(scriptRef.getUri(), scriptRef.getId());
      }
    }
  }

  private final Map<String, IsolateInfo> myIsolateIdToInfoMap = new THashMap<String, IsolateInfo>();

  public void addIsolate(@NotNull final IsolateRef isolateRef) {
    myIsolateIdToInfoMap.put(isolateRef.getId(), new IsolateInfo(isolateRef.getId(), isolateRef.getName()));
  }

  public boolean isIsolateKnown(@NotNull final String isolateId) {
    return myIsolateIdToInfoMap.containsKey(isolateId);
  }

  public void deleteIsolate(@NotNull final IsolateRef isolateRef) {
    myIsolateIdToInfoMap.remove(isolateRef.getId());
  }

  public void addLibrary(@NotNull final Isolate isolate, @NotNull final Library library) {
    myIsolateIdToInfoMap.get(isolate.getId()).addLibrary(library);
  }

  public Collection<String> getLiveIsolateIds() {
    return myIsolateIdToInfoMap.keySet();
  }

  @Nullable
  public String getScriptId(@NotNull final String isolateId, @NotNull String uri) {
    final IsolateInfo isolateInfo = myIsolateIdToInfoMap.get(isolateId);
    return isolateInfo == null ? null : isolateInfo.myUriToScriptIdMap.get(uri);
  }
}
