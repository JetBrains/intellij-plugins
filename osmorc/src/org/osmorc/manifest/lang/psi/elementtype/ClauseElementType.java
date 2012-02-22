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

package org.osmorc.manifest.lang.psi.elementtype;

import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.lang.psi.Clause;
import org.osmorc.manifest.lang.psi.impl.ClauseImpl;
import org.osmorc.manifest.lang.psi.stub.ClauseStub;
import org.osmorc.manifest.lang.psi.stub.impl.ClauseStubImpl;

import java.io.IOException;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ClauseElementType extends AbstractManifestStubElementType<ClauseStub, Clause> {
  public ClauseElementType() {
    super("CLAUSE");
  }

  @Override
  public Clause createPsi(@NotNull ClauseStub stub) {
    return new ClauseImpl(stub, this);
  }

  @Override
  public Clause createPsi(ASTNode node) {
    return new ClauseImpl(node);
  }

  @Override
  public ClauseStub createStub(@NotNull Clause psi, StubElement parentStub) {
    return new ClauseStubImpl(parentStub);
  }

  public void serialize(ClauseStub stub, StubOutputStream dataStream) throws IOException {
  }

  public ClauseStub deserialize(StubInputStream dataStream, StubElement parentStub) throws IOException {
    return new ClauseStubImpl(parentStub);
  }

  public void indexStub(ClauseStub stub, IndexSink sink) {
  }
}
