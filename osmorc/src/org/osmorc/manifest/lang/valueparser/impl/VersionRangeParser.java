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
package org.osmorc.manifest.lang.valueparser.impl;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.Nullable;
import org.osmorc.manifest.lang.psi.ManifestHeaderValue;
import org.osmorc.valueobject.Version;
import org.osmorc.valueobject.VersionRange;

/**
 * Author: Robert F. Beeger (robert@beeger.net)
 */
public class VersionRangeParser extends AbstractValueParserImpl<VersionRange>
{
  public VersionRangeParser(VersionParser versionParser)
  {
    _versionParser = versionParser;
  }

  protected VersionRange parseValue(
      @Nullable ManifestHeaderValue headerValue, String text, int start,
      @Nullable AnnotationHolder annotationHolder)
  {
    VersionRange.Boundary floorBoundary = null;
    Version floor = null;
    Version ceiling = null;
    VersionRange.Boundary ceilingBoundary = null;

    int nextStart = start;
    int nextEnd = nextStart + 1;

    if (text.charAt(nextStart) == '[' || text.charAt(nextStart) == '(')
    {
      if (annotationHolder != null)
      {
        annotationHolder.createErrorAnnotation(createTextRange(headerValue, nextStart, nextEnd),
            "Version ranges have to be enclosed in double quotes.");
      }
      nextStart++;
    }
    else if (text.charAt(nextStart) == '\"')
    {
      nextStart++;
      while (Character.isWhitespace(text.charAt(nextStart)))
      {
        nextStart++;
      }
      if (text.charAt(nextStart) == '[')
      {
        floorBoundary = VersionRange.Boundary.INCLUSIVE;
      }
      else if (text.charAt(nextStart) == '(')
      {
        floorBoundary = VersionRange.Boundary.EXCLUSIVE;
      }
      else
      {
        nextEnd = nextStart + 1;
        while (!Character.isDigit(text.charAt(nextEnd)))
        {
          nextEnd++;
        }
        if (annotationHolder != null)
        {
          Annotation annotation = annotationHolder.createErrorAnnotation(
              createTextRange(headerValue, nextStart, nextEnd), "Invalid boundary definition");
//          annotation.registerFix();
        }
      }
    }

    nextEnd = text.indexOf(',');

    return null;
  }

  private TextRange createTextRange(ManifestHeaderValue headerValue, int startInValue, int endInValue)
  {
    int headerValueStartOffset = headerValue.getTextRange().getStartOffset();
    return new TextRange(headerValueStartOffset + startInValue, headerValueStartOffset + endInValue);
  }

  private final VersionParser _versionParser;
}
