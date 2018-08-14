// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi;

import com.intellij.lang.javascript.psi.JSStatement;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Angular2HtmlAnimationEvent extends XmlAttribute {

  @NotNull
  @NonNls
  String getAnimationEventName();

  @Nullable
  AnimationPhase getPhase();

  @Nullable
  JSStatement getStatement();

  enum AnimationPhase {
    START,
    DONE
  }
}
