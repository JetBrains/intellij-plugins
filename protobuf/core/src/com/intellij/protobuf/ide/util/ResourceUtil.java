/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public final class ResourceUtil {

  public static byte[] readUrlAsBytes(URL url) throws IOException {
    try (InputStream stream = url.openStream()) {
      ByteArrayOutputStream result = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];
      int len;

      while ((len = stream.read(buffer)) != -1) {
        result.write(buffer, 0, len);
      }

      return result.toByteArray();
    }
  }

  public static String readUrlAsString(URL url) throws IOException {
    return new String(readUrlAsBytes(url));
  }

  public static String readPathAsString(String path) throws IOException {
    URL url = ResourceUtil.class.getResource(path);
    if (url == null) {
      throw new IOException("Resource path not found: " + path);
    }
    return readUrlAsString(url);
  }

  private ResourceUtil() {}
}
