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

    public IsolateInfo(@NotNull final Isolate isolate) {
      myIsolateId = isolate.getId();
      myName = isolate.getName();
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

  public void addLibrary(@NotNull final Isolate isolate, @NotNull final Library library) {
    IsolateInfo isolateInfo = myIsolateIdToInfoMap.get(isolate.getId());
    if (isolateInfo == null) {
      isolateInfo = new IsolateInfo(isolate);
      myIsolateIdToInfoMap.put(isolate.getId(), isolateInfo);
    }

    isolateInfo.addLibrary(library);
  }

  public void deleteIsolate(@NotNull final IsolateRef isolateRef) {
    myIsolateIdToInfoMap.remove(isolateRef.getId());
  }

  @Nullable
  public String getScriptId(@NotNull final String isolateId, @NotNull String uri) {
    final IsolateInfo isolateInfo = myIsolateIdToInfoMap.get(isolateId);
    return isolateInfo == null ? null : isolateInfo.myUriToScriptIdMap.get(uri);
  }
}
