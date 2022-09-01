// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata;

import org.angular2.entities.metadata.psi.*;
import org.angular2.entities.metadata.stubs.*;
import org.angular2.lang.metadata.MetadataJsonLanguage;
import org.angular2.lang.metadata.psi.MetadataElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public interface Angular2MetadataElementTypes {

  int STUB_VERSION = 0;

  String EXTERNAL_PREFIX_ID = "METADATA_JSON:";

  MetadataElementType<Angular2MetadataStringStub> STRING =
    new Angular2MetadataElementType<>("STRING", (stream, parent) -> new Angular2MetadataStringStub(stream, parent),
                                      element -> new Angular2MetadataString(element));
  MetadataElementType<Angular2MetadataArrayStub> ARRAY =
    new Angular2MetadataElementType<>("ARRAY", (stream, parent) -> new Angular2MetadataArrayStub(stream, parent),
                                      element -> new Angular2MetadataArray(element));
  MetadataElementType<Angular2MetadataObjectStub> OBJECT =
    new Angular2MetadataElementType<>("OBJECT", (stream, parent) -> new Angular2MetadataObjectStub(stream, parent),
                                      element -> new Angular2MetadataObject(element));
  MetadataElementType<Angular2MetadataReferenceStub> REFERENCE =
    new Angular2MetadataElementType<>("REFERENCE", (stream, parent) -> new Angular2MetadataReferenceStub(stream, parent),
                                      element -> new Angular2MetadataReference(element));
  MetadataElementType<Angular2MetadataFunctionStub> FUNCTION =
    new Angular2MetadataElementType<>("FUNCTION", (stream, parent) -> new Angular2MetadataFunctionStub(stream, parent),
                                      element -> new Angular2MetadataFunction(element));
  MetadataElementType<Angular2MetadataCallStub> CALL =
    new Angular2MetadataElementType<>("CALL", (stream, parent) -> new Angular2MetadataCallStub(stream, parent),
                                      element -> new Angular2MetadataCall(element));
  MetadataElementType<Angular2MetadataSpreadStub> SPREAD =
    new Angular2MetadataElementType<>("SPREAD", (stream, parent) -> new Angular2MetadataSpreadStub(stream, parent),
                                      element -> new Angular2MetadataSpread(element));

  MetadataElementType<Angular2MetadataClassStub> CLASS =
    new Angular2MetadataElementType<>("CLASS", (stream, parent) -> new Angular2MetadataClassStub(stream, parent),
                                      element -> new Angular2MetadataClass(element));
  MetadataElementType<Angular2MetadataNodeModuleStub> NODE_MODULE =
    new Angular2MetadataElementType<>("NODE_MODULE", (stream, parentStub) -> new Angular2MetadataNodeModuleStub(stream, parentStub),
                                      element -> new Angular2MetadataNodeModule(element));
  MetadataElementType<Angular2MetadataModuleExportStub> MODULE_EXPORT =
    new Angular2MetadataElementType<>("MODULE_EXPORT", (stream, parent) -> new Angular2MetadataModuleExportStub(stream, parent),
                                      element -> new Angular2MetadataModuleExport(element));


  MetadataElementType<Angular2MetadataModuleStub> MODULE =
    new Angular2MetadataElementType<>("MODULE", (stream, parent) -> new Angular2MetadataModuleStub(stream, parent),
                                      element -> new Angular2MetadataModule(element));
  MetadataElementType<Angular2MetadataPipeStub> PIPE =
    new Angular2MetadataElementType<>("PIPE", (stream, parent) -> new Angular2MetadataPipeStub(stream, parent),
                                      element -> new Angular2MetadataPipe(element));
  MetadataElementType<Angular2MetadataDirectiveStub> DIRECTIVE =
    new Angular2MetadataElementType<>("DIRECTIVE", (stream, parent) -> new Angular2MetadataDirectiveStub(stream, parent),
                                      element -> new Angular2MetadataDirective(element));
  MetadataElementType<Angular2MetadataComponentStub> COMPONENT =
    new Angular2MetadataElementType<>("COMPONENT", (stream, parent) -> new Angular2MetadataComponentStub(stream, parent),
                                      element -> new Angular2MetadataComponent(element));

  final class Angular2MetadataElementType<Stub extends Angular2MetadataElementStub<?>> extends MetadataElementType<Stub> {

    public Angular2MetadataElementType(@NotNull @NonNls String debugName,
                                       @NotNull MetadataStubConstructor<? extends Stub> stubConstructor,
                                       @NotNull MetadataElementConstructor<Stub> psiConstructor) {
      super(debugName, MetadataJsonLanguage.INSTANCE, stubConstructor, psiConstructor);
    }

    @NonNls
    @Override
    public String toString() {
      return EXTERNAL_PREFIX_ID + super.getDebugName();
    }

  }
}
