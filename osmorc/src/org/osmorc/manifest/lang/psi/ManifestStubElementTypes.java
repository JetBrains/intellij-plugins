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

package org.osmorc.manifest.lang.psi;

import com.intellij.psi.tree.IFileElementType;
import org.osmorc.manifest.lang.psi.elementtype.*;
import org.osmorc.manifest.lang.ManifestLanguage;
import com.intellij.psi.tree.IStubFileElementType;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ManifestStubElementTypes {
    public static final HeaderElementType HEADER = new HeaderElementType();
    public static final SectionElementType SECTION = new SectionElementType();
    public static final HeaderValuePartElementType HEADER_VALUE_PART = new HeaderValuePartElementType();
    public static final AttributeElementType ATTRIBUTE = new AttributeElementType();
    public static final DirectiveElementType DIRECTIVE = new DirectiveElementType();
    public static final ClauseElementType CLAUSE = new ClauseElementType();

    public static final IFileElementType FILE = new IStubFileElementType("ManifestFile", ManifestLanguage.INSTANCE) {
        @Override
        public int getStubVersion() {
            return 1;
        }
    };

    private ManifestStubElementTypes() {
    }
}
