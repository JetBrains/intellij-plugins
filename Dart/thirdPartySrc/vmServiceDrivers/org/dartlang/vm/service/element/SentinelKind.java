/*
 * Copyright (c) 2015, the Dart project authors.
 *
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.dartlang.vm.service.element;

// This is a generated file.

/**
 * A {@link SentinelKind} is used to distinguish different kinds of {@link Sentinel} objects.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public enum SentinelKind {

  /**
   * Indicates that a variable or field is in the process of being initialized.
   */
  BeingInitialized,

  /**
   * Indicates that the object referred to has been collected by the GC.
   */
  Collected,

  /**
   * Indicates that an object id has expired.
   */
  Expired,

  /**
   * Reserved for future use.
   */
  Free,

  /**
   * Indicates that a variable or field has not been initialized.
   */
  NotInitialized,

  /**
   * Indicates that a variable has been eliminated by the optimizing compiler.
   */
  OptimizedOut,

  /**
   * Represents a value returned by the VM but unknown to this client.
   */
  Unknown
}
