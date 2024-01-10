// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi

import org.angular2.lang.expr.psi.Angular2Action

interface Angular2HtmlEvent : Angular2HtmlBoundAttribute {
  val eventName: String
  val eventType: EventType
  val action: Angular2Action?
  val animationPhase: AnimationPhase?

  enum class EventType {
    REGULAR,
    ANIMATION
  }

  enum class AnimationPhase {
    START,
    DONE,
    INVALID
  }
}