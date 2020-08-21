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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class AdditionalJARContentsTableModelTest {
  @Test
  public void testDelete() {
    AdditionalJARContentsTableModel testObject = new AdditionalJARContentsTableModel();
    assertEquals(0, testObject.getRowCount());
    testObject.addAdditionalJARContent("test1", "test2");
    assertEquals(1, testObject.getRowCount());
    testObject.deleteAdditionalJARContent(0);
    assertEquals(0, testObject.getRowCount());
  }

  @Test
  public void testAdd() {
    AdditionalJARContentsTableModel testObject = new AdditionalJARContentsTableModel();
    assertEquals(0, testObject.getRowCount());

    testObject.addAdditionalJARContent("test1", "test2");
    assertEquals(1, testObject.getRowCount());
    assertEquals("test1", testObject.getValueAt(0, 0));
    assertEquals("test2", testObject.getValueAt(0, 1));

    testObject.addAdditionalJARContent("test3", "test4");
    assertEquals(2, testObject.getRowCount());
    assertEquals("test1", testObject.getValueAt(0, 0));
    assertEquals("test2", testObject.getValueAt(0, 1));
    assertEquals("test3", testObject.getValueAt(1, 0));
    assertEquals("test4", testObject.getValueAt(1, 1));
  }

  @Test
  public void testChange() {
    AdditionalJARContentsTableModel testObject = new AdditionalJARContentsTableModel();
    assertEquals(0, testObject.getRowCount());

    testObject.addAdditionalJARContent("test1", "test2");
    testObject.setValueAt("test3", 0, 0);
    assertEquals("test3", testObject.getValueAt(0, 0));
    assertEquals("test2", testObject.getValueAt(0, 1));

    testObject.setValueAt("test4", 0, 1);
    assertEquals("test3", testObject.getValueAt(0, 0));
    assertEquals("test4", testObject.getValueAt(0, 1));

    testObject.changeAdditionalJARContent(0, "test5", "test6");
    assertEquals("test5", testObject.getValueAt(0, 0));
    assertEquals("test6", testObject.getValueAt(0, 1));
  }
}
