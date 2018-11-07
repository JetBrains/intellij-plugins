// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.ObjectUtils;
import org.angular2.entities.metadata.stubs.Angular2MetadataReferenceStub;
import org.angular2.index.Angular2MetadataNodeModuleIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2MetadataReference extends Angular2MetadataElement<Angular2MetadataReferenceStub> {
  public Angular2MetadataReference(@NotNull Angular2MetadataReferenceStub element) {
    super(element);
  }

  @Nullable
  public Angular2MetadataElement resolve() {
    String moduleName = getStub().getModule();
    Angular2MetadataNodeModule module;
    if (moduleName != null) {
      Ref<Angular2MetadataNodeModule> moduleRef = new Ref<>();
      StubIndex.getInstance().processElements(
        Angular2MetadataNodeModuleIndex.KEY, moduleName, getProject(),
        GlobalSearchScope.allScope(getProject()), Angular2MetadataNodeModule.class,
        nodeModule -> {
          if (nodeModule.isValid()) {
            moduleRef.set(nodeModule);
            return false;
          }
          return true;
        });
      module = moduleRef.get();
    }
    else {
      module = getNodeModule();
    }
    return module != null
           ? ObjectUtils.tryCast(module.findMember(getStub().getName()), Angular2MetadataElement.class)
           : null;
  }

  @Override
  public String toString() {
    String module = getStub().getModule();
    return (module == null ? "" : module + "#") + getStub().getName() + " <metadata reference>";
  }
}
