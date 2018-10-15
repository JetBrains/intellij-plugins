// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata;

import com.intellij.psi.tree.IStubFileElementType;
import org.angular2.entities.metadata.psi.*;
import org.angular2.entities.metadata.stubs.*;
import org.angular2.lang.metadata.psi.MetadataElementType;
import org.angular2.lang.metadata.psi.MetadataStubFileElementType;
import org.angular2.lang.metadata.stubs.MetadataFileStubImpl;
import org.jetbrains.annotations.NotNull;

public interface Angular2MetadataElementTypes {

  MetadataElementType<Angular2MetadataArrayStub> ARRAY =
    new Angular2MetadataElementType<>("ARRAY", Angular2MetadataArrayStub::new, Angular2MetadataArray::new);
  MetadataElementType<Angular2MetadataClassStub> CLASS =
    new Angular2MetadataElementType<>("CLASS", Angular2MetadataClassStub::new, Angular2MetadataClass::new);
  MetadataElementType<Angular2MetadataNodeModuleStub> NODE_MODULE =
    new Angular2MetadataElementType<>("MODULE", Angular2MetadataNodeModuleStub::new, Angular2MetadataNodeModule::new);
  MetadataElementType<Angular2MetadataObjectStub> OBJECT =
    new Angular2MetadataElementType<>("OBJECT", Angular2MetadataObjectStub::new, Angular2MetadataObject::new);
  MetadataElementType<Angular2MetadataReferenceStub> REFERENCE =
    new Angular2MetadataElementType<>("REFERENCE", Angular2MetadataReferenceStub::new, Angular2MetadataReference::new);

  IStubFileElementType<MetadataFileStubImpl> FILE = new MetadataStubFileElementType(Angular2MetadataLanguage.INSTANCE);

  final class Angular2MetadataElementType<Stub extends Angular2MetadataElementStub> extends MetadataElementType<Stub> {

    public Angular2MetadataElementType(@NotNull String debugName,
                                       @NotNull MetadataStubConstructor<Stub> stubConstructor,
                                       @NotNull MetadataElementConstructor<Stub> psiConstructor) {
      super(debugName, Angular2MetadataLanguage.INSTANCE, stubConstructor, psiConstructor);
    }
  }
}
