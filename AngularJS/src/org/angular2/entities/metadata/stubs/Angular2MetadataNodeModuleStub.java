// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.stubs;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import org.angular2.entities.metadata.Angular2MetadataElementTypes;
import org.angular2.entities.metadata.psi.Angular2MetadataNodeModule;
import org.angular2.lang.metadata.MetadataUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Angular2MetadataNodeModuleStub extends Angular2MetadataElementStub<Angular2MetadataNodeModule> {

  private static final String ORIGINS = "origins";
  private static final String IMPORT_AS = "importAs";
  private static final String METADATA = "metadata";

  private String myImportAs;
  private Map<String, String> myOrigins;

  public Angular2MetadataNodeModuleStub(@NotNull StubInputStream dataStream, @Nullable StubElement parentStub) throws IOException {
    super(dataStream, parentStub, Angular2MetadataElementTypes.NODE_MODULE);
  }

  public Angular2MetadataNodeModuleStub(@Nullable StubElement parentStub, @Nullable JsonValue fileRoot) {
    super((String)null, parentStub, Angular2MetadataElementTypes.NODE_MODULE);
    if (fileRoot instanceof JsonObject) {
      readFile((JsonObject)fileRoot);
    }
  }

  public String getImportAs() {
    return myImportAs;
  }

  Map<String, String> getOrigins() {
    return myOrigins;
  }

  private void readFile(JsonObject fileRoot) {
    myImportAs = MetadataUtils.readStringPropertyValue(fileRoot.findProperty(IMPORT_AS));
    myOrigins = MetadataUtils.streamObjectProperty(fileRoot.findProperty(ORIGINS))
      .map(MetadataUtils::readStringProperty)
      .filter(Objects::nonNull)
      .collect(Collectors.toMap(p -> p.first, p -> p.second, (a,b) -> a));
    MetadataUtils.streamObjectProperty(fileRoot.findProperty(METADATA))
      .forEach(this::loadMemberProperty);
  }

}
