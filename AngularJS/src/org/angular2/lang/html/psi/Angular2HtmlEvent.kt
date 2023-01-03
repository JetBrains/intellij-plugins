// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi;

import org.angular2.lang.expr.psi.Angular2Action;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Angular2HtmlEvent extends Angular2HtmlBoundAttribute {

  @NotNull
  String getEventName();

  @NotNull
  EventType getEventType();

  @Nullable
  Angular2Action getAction();

  @Nullable
  AnimationPhase getAnimationPhase();

  enum EventType {
    REGULAR,
    ANIMATION
  }

  enum AnimationPhase {
    START,
    DONE,
    INVALID
  }
}
