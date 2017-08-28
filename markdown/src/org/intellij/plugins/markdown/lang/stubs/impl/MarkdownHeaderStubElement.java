package org.intellij.plugins.markdown.lang.stubs.impl;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHeaderImpl;
import org.intellij.plugins.markdown.lang.stubs.MarkdownStubElementBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MarkdownHeaderStubElement extends MarkdownStubElementBase<MarkdownHeaderImpl> {
  @Nullable private final String myName;
  private final ItemPresentation myPresentation;

  protected MarkdownHeaderStubElement(@NotNull StubElement parent,
                                      @NotNull IStubElementType elementType,
                                      @Nullable String indexedName,
                                      @NotNull ItemPresentation presentation) {
    super(parent, elementType);
    myName = indexedName;
    myPresentation = presentation;
  }

  @Nullable
  String getLocationString() {
    return myPresentation.getLocationString();
  }

  @Nullable
  String getPresentableText() {
    return myPresentation.getPresentableText();
  }

  @Nullable
  String getIndexedName() {
    return myName;
  }
}