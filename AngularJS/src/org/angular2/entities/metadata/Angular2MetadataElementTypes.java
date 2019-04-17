// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata;

import org.angular2.entities.metadata.psi.*;
import org.angular2.entities.metadata.stubs.*;
import org.angular2.lang.metadata.MetadataJsonLanguage;
import org.angular2.lang.metadata.psi.MetadataElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public interface Angular2MetadataElementTypes {

  MetadataElementType<Angular2MetadataStringStub> STRING =
    new Angular2MetadataElementType<>("STRING", Angular2MetadataStringStub::new, Angular2MetadataString::new);
  MetadataElementType<Angular2MetadataArrayStub> ARRAY =
    new Angular2MetadataElementType<>("ARRAY", Angular2MetadataArrayStub::new, Angular2MetadataArray::new);
  MetadataElementType<Angular2MetadataObjectStub> OBJECT =
    new Angular2MetadataElementType<>("OBJECT", Angular2MetadataObjectStub::new, Angular2MetadataObject::new);
  MetadataElementType<Angular2MetadataReferenceStub> REFERENCE =
    new Angular2MetadataElementType<>("REFERENCE", Angular2MetadataReferenceStub::new, Angular2MetadataReference::new);
  MetadataElementType<Angular2MetadataFunctionStub> FUNCTION =
    new Angular2MetadataElementType<>("FUNCTION", Angular2MetadataFunctionStub::new, Angular2MetadataFunction::new);
  MetadataElementType<Angular2MetadataCallStub> CALL =
    new Angular2MetadataElementType<>("CALL", Angular2MetadataCallStub::new, Angular2MetadataCall::new);

  MetadataElementType<Angular2MetadataClassStub> CLASS =
    new Angular2MetadataElementType<>("CLASS", Angular2MetadataClassStub::new, Angular2MetadataClass::new);
  MetadataElementType<Angular2MetadataNodeModuleStub> NODE_MODULE =
    new Angular2MetadataElementType<>("NODE_MODULE", Angular2MetadataNodeModuleStub::new, Angular2MetadataNodeModule::new);
  MetadataElementType<Angular2MetadataModuleExportStub> MODULE_EXPORT =
    new Angular2MetadataElementType<>("MODULE_EXPORT", Angular2MetadataModuleExportStub::new, Angular2MetadataModuleExport::new);


  MetadataElementType<Angular2MetadataModuleStub> MODULE =
    new Angular2MetadataElementType<>("MODULE", Angular2MetadataModuleStub::new, Angular2MetadataModule::new);
  MetadataElementType<Angular2MetadataPipeStub> PIPE =
    new Angular2MetadataElementType<>("PIPE", Angular2MetadataPipeStub::new, Angular2MetadataPipe::new);
  MetadataElementType<Angular2MetadataDirectiveStub> DIRECTIVE =
    new Angular2MetadataElementType<>("DIRECTIVE", Angular2MetadataDirectiveStub::new, Angular2MetadataDirective::new);
  MetadataElementType<Angular2MetadataComponentStub> COMPONENT =
    new Angular2MetadataElementType<>("COMPONENT", Angular2MetadataComponentStub::new, Angular2MetadataComponent::new);

  final class Angular2MetadataElementType<Stub extends Angular2MetadataElementStub<?>> extends MetadataElementType<Stub> {

    public Angular2MetadataElementType(@NotNull @NonNls String debugName,
                                       @NotNull MetadataStubConstructor<? extends Stub> stubConstructor,
                                       @NotNull MetadataElementConstructor<Stub> psiConstructor) {
      super(debugName, MetadataJsonLanguage.INSTANCE, stubConstructor, psiConstructor);
    }
  }
}
