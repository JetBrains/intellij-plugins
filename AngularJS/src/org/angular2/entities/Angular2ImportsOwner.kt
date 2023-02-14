// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

/**
 * Relevant excerpt from Angular sources:
 *
 *
 * Angular components marked as `standalone` do not need to be declared in an NgModule.
 * Such components directly manage their own template dependencies (components, directives and pipes
 * used in a template) via the `imports` property.
 */
interface Angular2ImportsOwner : Angular2Entity {
  val imports: Set<Angular2Entity>

  val declarationsInScope: Set<Angular2Declaration>
}
