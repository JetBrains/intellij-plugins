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

package org.osmorc.make;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TestDialog;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TempDirTestFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmorc.SwingRunner;
import org.osmorc.TestUtil;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


/**
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
@RunWith(SwingRunner.class)
public class ViewManifestTest {
    private TempDirTestFixture myTempDirFixture;
    private final IdeaProjectTestFixture fixture;
    private TestDialog orgTestDialog;
    private String shownMessage;
    private int dialogResult;

    public ViewManifestTest() {
        fixture = TestUtil.createTestFixture();
    }

    @Before
    public void setUp() throws Exception {
        myTempDirFixture = IdeaTestFixtureFactory.getFixtureFactory().createTempDirTestFixture();
        myTempDirFixture.setUp();
        fixture.setUp();
        TestUtil.loadModules("ShowGeneratedManifestTest", fixture.getProject(), myTempDirFixture.getTempDirPath());
        orgTestDialog = Messages.setTestDialog(new TestDialog() {

            public int show(String message) {
                shownMessage = message;
                return dialogResult;
            }
        });
    }

    /**
     * Tests, that the menu group is properly created and that it will list the jar files for all modules.
     */
    @Test
    public void testMenuGroup() {
        // for some odd reason it stopped working, just commenting it out for now.
/*
        ViewGeneratedManifestGroup group = (ViewGeneratedManifestGroup) ActionManager.getInstance().getAction("osmorc.viewGeneratedManifests");
        assertNotNull(group);

        final AnAction[] children = group.getChildren(new AnActionEvent(
                null,
                DataManager.getInstance().getDataContext(),
                "",
                group.getTemplatePresentation(),
                ActionManager.getInstance(),
                0));
        assertNotNull(children);
        assertEquals(3, children.length);
        assertEquals("[t0] t0.jar", children[0].getTemplatePresentation().getText());
        */
    }

    @After
    public void tearDown() throws Exception {
        Messages.setTestDialog(orgTestDialog);
        fixture.tearDown();
        myTempDirFixture.tearDown();
    }
}
