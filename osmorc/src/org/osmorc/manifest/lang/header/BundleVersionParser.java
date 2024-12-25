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
package org.osmorc.manifest.lang.header;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.header.HeaderParser;
import org.jetbrains.lang.manifest.header.impl.StandardHeaderParser;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.HeaderValue;
import org.jetbrains.lang.manifest.psi.HeaderValuePart;
import org.osgi.framework.Version;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public final class BundleVersionParser extends StandardHeaderParser {
  public static final HeaderParser INSTANCE = new BundleVersionParser();

  private BundleVersionParser() { }

  @Override
  public boolean annotate(@NotNull Header header, @NotNull AnnotationHolder holder) {
    HeaderValue value = header.getHeaderValue();
    if (value instanceof HeaderValuePart) {
      try {
        //noinspection ResultOfObjectAllocationIgnored
        new Version(value.getUnwrappedText());
      }
      catch (IllegalArgumentException e) {
        TextRange range = ((HeaderValuePart)value).getHighlightingRange();
        holder.newAnnotation(HighlightSeverity.ERROR, e.getMessage()).range(range).create();
        return true;
      }
    }

    return false;
  }

  @Override
  public @Nullable Object getConvertedValue(@NotNull Header header) {
    HeaderValue value = header.getHeaderValue();
    if (value instanceof HeaderValuePart) {
      try {
        return new Version(value.getUnwrappedText());
      }
      catch (IllegalArgumentException ignored) { }
    }

    return null;
  }
}
