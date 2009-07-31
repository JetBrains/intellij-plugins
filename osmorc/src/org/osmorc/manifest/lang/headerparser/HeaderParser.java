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
package org.osmorc.manifest.lang.headerparser;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.lang.psi.HeaderValuePart;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public interface HeaderParser {
    PsiReference[] getReferences(@NotNull HeaderValuePart headerValuePart);

    /**
     * The value of the given header value. Complex headers will probably have several header values. This method is
     * used to convert the raw data into some domain specific value as for example a Version or VersionRange.
     *
     * @return The converted value.
     */
    Object getValue(@NotNull HeaderValuePart headerValuePart);

    /**
     * Simple headers don't have clauses, attributes and directives. Semicolons and commas don't have any special meaning
     *
     * @return true, if the header parsed by this parser is a simple header, in which commas and semicolons don't
     *         have any special meaning
     */
    boolean isSimpleHeader();

    /**
     * Annotate the header value with error or any other useful information.
     *
     * @param headerValuePart
     * @param holder          The annotation holder into which to put the annotations.
     */
    void annotate(HeaderValuePart headerValuePart, AnnotationHolder holder);
}
