/*
 * Copyright 2012, the Dart project authors.
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

package com.jetbrains.lang.dart.ide.runner.server.google;

public class URIUtilities {
  /**
   * Convert from a non-uri encoded string to a uri encoded one.
   *
   * @param str
   * @return the uri encoded input string
   */
  public static String uriEncode(String str) {
    StringBuilder builder = new StringBuilder(str.length() * 2);

    for (char c : str.toCharArray()) {
      switch (c) {
        case '%':
        case '?':
        case ';':
        case '#':
        case '"':
        case '\'':
        case '<':
        case '>':
        case ' ':
          // ' ' ==> "%20"
          builder.append('%');
          builder.append(Integer.toHexString(c));
          break;
        default:
          builder.append(c);
          break;
      }
    }

    return builder.toString();
  }
}
