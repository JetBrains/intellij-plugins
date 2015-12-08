/*
 * Copyright (c) 2013, the Dart project authors.
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
package com.google.dart.server.utilities.instrumentation;

/**
 * The interface {@code InstrumentationLogger} defines the behavior of objects that are used to log
 * instrumentation data.
 * <p>
 * For an example of using objects that implement this interface, see {@link Instrumentation}.
 * 
 * @coverage dart.server.utilities
 */
public interface InstrumentationLogger {
  /**
   * Create a builder that can collect the data associated with an operation identified by the given
   * name.
   * 
   * @param name the name used to uniquely identify the operation
   * @return the builder that was created
   */
  public InstrumentationBuilder createBuilder(String name);
}
