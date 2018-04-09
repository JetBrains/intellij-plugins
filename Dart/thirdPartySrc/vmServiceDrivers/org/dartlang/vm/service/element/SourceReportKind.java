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

@SuppressWarnings({"WeakerAccess", "unused", "UnnecessaryInterfaceModifier"})
public enum SourceReportKind {

  /**
   * Used to request a code coverage information.
   */
  Coverage,

  /**
   * Used to request a list of token positions of possible breakpoints.
   */
  PossibleBreakpoints,

  /**
   * Represents a value returned by the VM but unknown to this client.
   */
  Unknown
}
