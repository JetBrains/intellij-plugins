// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.metadata.stubs.Angular2MetadataComponentStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class Angular2MetadataComponent extends Angular2MetadataDirectiveBase<Angular2MetadataComponentStub> implements Angular2Component {
  public Angular2MetadataComponent(@NotNull Angular2MetadataComponentStub element) {
    super(element);
  }

  @Nullable
  @Override
  public HtmlFileImpl getTemplateFile() {
    return null;
  }

  @NotNull
  @Override
  public List<PsiFile> getCssFiles() {
    return Collections.emptyList();
  }

  @Override
  public boolean isTemplate() {
    return false;
  }
}
