/*
 * Copyright (c) 2012, the Dart project authors.
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
package com.google.dart.server.utilities.general;

/**
 * The class {@code ObjectUtilities} defines general utility methods that are useful across all
 * objects.
 * 
 * @coverage dart.server.utilities
 */
public final class ObjectUtilities {
  /**
   * Combine two hash codes to make a new one.
   * 
   * @param firstHashCode the first hash code to be combined
   * @param secondHashCode the second hash code to be combined
   * @return the result of combining the hash codes
   */
  public static int combineHashCodes(int firstHashCode, int secondHashCode) {
    return firstHashCode * 31 + secondHashCode;
  }

  /**
   * Return {@code true} if the given objects are equal.
   * 
   * @param first the first object being compared
   * @param second the second object being compared
   * @return {@code true} if the given objects are equal
   */
  public static boolean equals(Object first, Object second) {
    if (first == null) {
      return second == null;
    } else if (second == null) {
      return false;
    }
    return first.equals(second);
  }

  /**
   * Prevent the creation of instances of this class.
   */
  private ObjectUtilities() {
  }
}
