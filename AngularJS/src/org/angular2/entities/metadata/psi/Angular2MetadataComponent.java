// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.html.HtmlFileImpl;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2DirectiveKind;
import org.angular2.entities.Angular2DirectiveSelector;
import org.angular2.entities.Angular2DirectiveSelectorImpl;
import org.angular2.entities.metadata.stubs.Angular2MetadataComponentStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class Angular2MetadataComponent extends Angular2MetadataDirectiveBase<Angular2MetadataComponentStub> implements Angular2Component {
  private final NotNullLazyValue<List<Angular2DirectiveSelector>> myNgContentSelectors = NotNullLazyValue.lazy(() -> {
    return ContainerUtil.map(getStub().getNgContentSelectors(), selector -> {
      return new Angular2DirectiveSelectorImpl(() -> ObjectUtils.notNull(getTypeScriptClass(), this), selector, null);
    });
  });

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
