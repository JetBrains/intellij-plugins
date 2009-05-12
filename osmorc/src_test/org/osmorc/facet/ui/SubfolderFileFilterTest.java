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

package org.osmorc.facet.ui;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiFile;
import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class SubfolderFileFilterTest
{
  @Test
  public void testAcceptContainedFile()
  {
    VirtualFileSystem fileSystem = createMock(VirtualFileSystem.class);
    VirtualFile rootFolder1 = createMock(VirtualFile.class);
    VirtualFile rootFolder2 = createMock(VirtualFile.class);
    VirtualFile file = createMock(VirtualFile.class);
    PsiFile psiFile = createMock(PsiFile.class);

    expect(psiFile.getVirtualFile()).andReturn(file).anyTimes();
    expect(rootFolder1.getFileSystem()).andReturn(fileSystem).anyTimes();
    expect(rootFolder2.getFileSystem()).andReturn(fileSystem).anyTimes();
    expect(file.getFileSystem()).andReturn(fileSystem).anyTimes();
    expect(file.getParent()).andReturn(rootFolder2).anyTimes();
    expect(rootFolder1.getParent()).andReturn(null).anyTimes();
    expect(rootFolder2.getParent()).andReturn(null).anyTimes();

    replay(fileSystem, rootFolder1, rootFolder2, file, psiFile);

    SubfolderFileFilter testOject = new SubfolderFileFilter(new VirtualFile[]{rootFolder1, rootFolder2});

    assertTrue(testOject.accept(psiFile));

    verify(fileSystem, rootFolder1, rootFolder2, file, psiFile);
  }

  @Test
  public void testAcceptNotContainedFile()
  {
    VirtualFileSystem fileSystem = createMock(VirtualFileSystem.class);
    VirtualFile rootFolder1 = createMock(VirtualFile.class);
    VirtualFile rootFolder2 = createMock(VirtualFile.class);
    VirtualFile file = createMock(VirtualFile.class);
    PsiFile psiFile = createMock(PsiFile.class);

    expect(psiFile.getVirtualFile()).andReturn(file).anyTimes();
    expect(rootFolder1.getFileSystem()).andReturn(fileSystem).anyTimes();
    expect(rootFolder2.getFileSystem()).andReturn(fileSystem).anyTimes();
    expect(file.getFileSystem()).andReturn(fileSystem).anyTimes();
    expect(file.getParent()).andReturn(rootFolder2).anyTimes();
    expect(rootFolder1.getParent()).andReturn(null).anyTimes();
    expect(rootFolder2.getParent()).andReturn(null).anyTimes();

    replay(fileSystem, rootFolder1, rootFolder2, file, psiFile);

    SubfolderFileFilter testOject = new SubfolderFileFilter(new VirtualFile[]{rootFolder1});

    assertFalse(testOject.accept(psiFile));

    verify(fileSystem, rootFolder1, rootFolder2, file, psiFile);
  }

  @Test
  public void testAcceptNoFile()
  {
    VirtualFile rootFolder1 = createMock(VirtualFile.class);
    VirtualFile rootFolder2 = createMock(VirtualFile.class);
    PsiFile psiFile = createMock(PsiFile.class);

    expect(psiFile.getVirtualFile()).andReturn(null).anyTimes();
    expect(rootFolder1.getParent()).andReturn(null).anyTimes();
    expect(rootFolder2.getParent()).andReturn(null).anyTimes();

    replay(rootFolder1, rootFolder2, psiFile);

    SubfolderFileFilter testOject = new SubfolderFileFilter(new VirtualFile[]{rootFolder1});

    assertFalse(testOject.accept(psiFile));

    verify(rootFolder1, rootFolder2, psiFile);
  }
}
