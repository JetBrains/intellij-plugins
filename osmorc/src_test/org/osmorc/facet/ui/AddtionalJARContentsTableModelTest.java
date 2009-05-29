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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class AddtionalJARContentsTableModelTest {
    @Test
    public void testDelete() {
        AdditionalJARContentsTableModel testObject = new AdditionalJARContentsTableModel();
        assertThat(testObject.getRowCount(), equalTo(0));
        testObject.addAdditionalJARContent("test1", "test2");

        assertThat(testObject.getRowCount(), equalTo(1));
        testObject.deleteAdditionalJARContent(0);
        assertThat(testObject.getRowCount(), equalTo(0));
    }

    @Test
    public void testAdd() {
        AdditionalJARContentsTableModel testObject = new AdditionalJARContentsTableModel();
        assertThat(testObject.getRowCount(), equalTo(0));

        testObject.addAdditionalJARContent("test1", "test2");
        assertThat(testObject.getRowCount(), equalTo(1));
        assertThat((String) testObject.getValueAt(0, 0), equalTo("test1"));
        assertThat((String) testObject.getValueAt(0, 1), equalTo("test2"));

        testObject.addAdditionalJARContent("test3", "test4");
        assertThat(testObject.getRowCount(), equalTo(2));
        assertThat((String) testObject.getValueAt(0, 0), equalTo("test1"));
        assertThat((String) testObject.getValueAt(0, 1), equalTo("test2"));
        assertThat((String) testObject.getValueAt(1, 0), equalTo("test3"));
        assertThat((String) testObject.getValueAt(1, 1), equalTo("test4"));

    }

    @Test
    public void testChange() {
        AdditionalJARContentsTableModel testObject = new AdditionalJARContentsTableModel();
        assertThat(testObject.getRowCount(), equalTo(0));


        testObject.addAdditionalJARContent("test1", "test2");
        testObject.setValueAt("test3", 0, 0);
        assertThat((String) testObject.getValueAt(0, 0), equalTo("test3"));
        assertThat((String) testObject.getValueAt(0, 1), equalTo("test2"));

        testObject.setValueAt("test4", 0, 1);
        assertThat((String) testObject.getValueAt(0, 0), equalTo("test3"));
        assertThat((String) testObject.getValueAt(0, 1), equalTo("test4"));

        testObject.changeAdditionalJARConent(0, "test5", "test6");
        assertThat((String) testObject.getValueAt(0, 0), equalTo("test5"));
        assertThat((String) testObject.getValueAt(0, 1), equalTo("test6"));
    }
}
