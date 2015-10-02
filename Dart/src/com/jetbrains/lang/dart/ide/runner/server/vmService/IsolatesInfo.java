package com.jetbrains.lang.dart.ide.runner.server.vmService;

import com.intellij.util.containers.ArrayListSet;
import gnu.trove.THashMap;
import org.dartlang.vm.service.element.Isolate;
import org.dartlang.vm.service.element.IsolateRef;
import org.dartlang.vm.service.element.Library;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

public class IsolatesInfo {

  private final Map<String, Collection<Library>> myIsolateIdToLibrariesMap = new THashMap<String, Collection<Library>>();
  private final Map<String, String> myIsolateIdToNameMap = new THashMap<String, String>();

  public void addLibrary(@NotNull final Isolate isolate, @NotNull final Library library) {
    Collection<Library> libraries = myIsolateIdToLibrariesMap.get(isolate.getId());
    if (libraries == null) {
      libraries = new ArrayListSet<Library>();
      myIsolateIdToLibrariesMap.put(isolate.getId(), libraries);
      myIsolateIdToNameMap.put(isolate.getId(), isolate.getName());
    }
    libraries.add(library);
  }

  public void deleteIsolate(@NotNull final IsolateRef isolateRef) {
    myIsolateIdToLibrariesMap.remove(isolateRef.getId());
    myIsolateIdToNameMap.remove(isolateRef.getId());
  }
}
