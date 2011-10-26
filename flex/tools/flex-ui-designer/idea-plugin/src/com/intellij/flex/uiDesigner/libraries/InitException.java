package com.intellij.flex.uiDesigner.libraries;

import com.intellij.diagnostic.errordialog.Attachment;
import com.intellij.flex.uiDesigner.FlexUIDesignerBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

public class InitException extends Exception {
  public final Attachment[] attachments;
  public final String technicalMessage;

  InitException(@NotNull Throwable e, @PropertyKey(resourceBundle = FlexUIDesignerBundle.BUNDLE) String key) {
    this(e, key, null, null);
  }

  InitException(@NotNull Throwable e, @PropertyKey(resourceBundle = FlexUIDesignerBundle.BUNDLE) String key, @Nullable Attachment[] attachments, @Nullable String technicalMessage) {
    super(FlexUIDesignerBundle.message(key), e);
    this.attachments = attachments;
    this.technicalMessage = technicalMessage;
  }
}
