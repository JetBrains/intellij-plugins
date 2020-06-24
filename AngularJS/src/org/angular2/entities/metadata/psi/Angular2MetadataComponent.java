// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2DirectiveKind;
import org.angular2.entities.Angular2DirectiveSelector;
import org.angular2.entities.Angular2DirectiveSelectorImpl;
import org.angular2.entities.metadata.stubs.Angular2MetadataComponentStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static com.intellij.util.ObjectUtils.notNull;
import static com.intellij.util.containers.ContainerUtil.map;

public class Angular2MetadataComponent extends Angular2MetadataDirectiveBase<Angular2MetadataComponentStub> implements Angular2Component {


  private final AtomicNotNullLazyValue<List<Angular2DirectiveSelector>> myNgContentSelectors = AtomicNotNullLazyValue.createValue(
    () -> map(getStub().getNgContentSelectors(), selector ->
      new Angular2DirectiveSelectorImpl(() -> notNull(getTypeScriptClass(), this), selector, null))
  );

  public Angular2MetadataComponent(@NotNull Angular2MetadataComponentStub element) {
    super(element);
  }

  @Override
  public @Nullable HtmlFileImpl getTemplateFile() {
    return null;
  }

  @Override
  public @NotNull List<PsiFile> getCssFiles() {
    return Collections.emptyList();
  }

  @Override
  public @NotNull List<Angular2DirectiveSelector> getNgContentSelectors() {
    return myNgContentSelectors.getValue();
  }

  @Override
  public @NotNull Angular2DirectiveKind getDirectiveKind() {
    return Angular2DirectiveKind.REGULAR;
  }
}
