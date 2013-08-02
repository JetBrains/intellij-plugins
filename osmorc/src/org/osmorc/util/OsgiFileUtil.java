/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.util;

import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.StandardFileSystems;
import org.jetbrains.annotations.NotNull;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsgiFileUtil {
  @NotNull
  public static String pathToUrl(@NotNull String path) {
    return SystemInfo.isWindows ? StandardFileSystems.FILE_PROTOCOL_PREFIX + "/" + path : StandardFileSystems.FILE_PROTOCOL_PREFIX + path;
  }

  @NotNull
  public static String urlToPath(@NotNull String url) {
    String path = url;
    if (path.startsWith(StandardFileSystems.FILE_PROTOCOL_PREFIX)) {
      path = path.substring(StandardFileSystems.FILE_PROTOCOL_PREFIX.length());
      if (path.length() >= 2 && path.charAt(0) == '/' && Character.isLetter(path.charAt(1)) && path.charAt(2) == ':') {
        path = path.substring(1);
      }
    }
    return path;
  }
}
