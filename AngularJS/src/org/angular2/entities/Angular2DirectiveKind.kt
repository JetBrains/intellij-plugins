// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Angular2DirectiveKind {
  REGULAR,
  STRUCTURAL,
  BOTH;

  public boolean isStructural() {
    return this == STRUCTURAL || this == BOTH;
  }

  public boolean isRegular() {
    return this == REGULAR || this == BOTH;
  }

  public static @NotNull Angular2DirectiveKind get(boolean isRegular, boolean isStructural) {
    if (isRegular && isStructural) {
      return BOTH;
    }
    else if (isStructural) {
      return STRUCTURAL;
    }
    else {
      return REGULAR;
    }
  }

  public static @Nullable Angular2DirectiveKind get(boolean hasElementRef, boolean hasTemplateRef, boolean hasViewContainerRef) {
    return hasElementRef || hasTemplateRef || hasViewContainerRef
           ? get(hasElementRef || (hasViewContainerRef && !hasTemplateRef),
                 hasTemplateRef || hasViewContainerRef)
           : null;
  }
}
