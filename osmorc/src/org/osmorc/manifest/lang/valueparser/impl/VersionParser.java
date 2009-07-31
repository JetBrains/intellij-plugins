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

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.Nullable;
import org.osmorc.valueobject.Version;
import org.osmorc.manifest.lang.psi.HeaderValuePart;

import java.util.regex.Pattern;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class VersionParser extends AbstractValueParserImpl<Version> {
    public VersionParser() {
        _qualifierPattern = Pattern.compile("[\\w\\-]*");
    }

    protected Version parseValue(
            @Nullable HeaderValuePart headerValue, String text, int start,
            @Nullable AnnotationHolder annotationHolder) {
        String[] componentNames = new String[]{"major", "minor", "micro"};
        int[] components = new int[]{0, 0, 0};
        int componentStart;
        int componentEnd = -1;

        for (int componentIdx = 0; componentIdx < components.length; componentIdx++) {
            componentStart = componentEnd + 1;
            componentEnd = text.indexOf('.', componentStart);
            if (componentEnd < 0) {
                componentEnd = text.length();
            }
            try {
                components[componentIdx] = Integer.parseInt(text.substring(componentStart, componentEnd));
            }
            catch (NumberFormatException e) {
                createInvalidNumberAnnotation(headerValue, componentStart + start, componentEnd + start, annotationHolder,
                        componentNames[componentIdx]);
            }

            if (componentEnd == text.length()) {
                break;
            }
        }

        String qualifier = "";

        if (componentEnd < text.length()) {
            componentStart = componentEnd + 1;
            componentEnd = text.length();
            qualifier = text.substring(componentStart);
            if (annotationHolder != null && !_qualifierPattern.matcher(qualifier).matches()) {
                TextRange headerValueTextRange = headerValue.getTextRange();
                TextRange textRange = new TextRange(headerValueTextRange.getStartOffset() + componentStart,
                        headerValueTextRange.getStartOffset() + componentEnd);
                annotationHolder.createErrorAnnotation(textRange,
                        "The qualifier component of the defined version is invalid. It may only contain alphanumeric characters, '-' and '_'");
            }
        }

        return new Version(components[0], components[1], components[2], qualifier);
    }

    void createInvalidNumberAnnotation(@Nullable HeaderValuePart headerValuePart, int start, int end,
                                       @Nullable AnnotationHolder annotationHolder, String component) {
        if (annotationHolder != null) {
            TextRange headerValueTextRange = headerValuePart.getTextRange();
            TextRange textRange =
                    new TextRange(headerValueTextRange.getStartOffset() + start, headerValueTextRange.getStartOffset() + end);
            annotationHolder.createErrorAnnotation(textRange,
                    "The " + component + " component of the defined version is not a valid number");
        }
    }

    private final Pattern _qualifierPattern;
}
