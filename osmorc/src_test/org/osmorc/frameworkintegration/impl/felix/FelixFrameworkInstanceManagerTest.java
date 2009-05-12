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

package org.osmorc.frameworkintegration.impl.felix;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import static org.easymock.classextension.EasyMock.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.frameworkintegration.impl.LibraryHandlerImpl;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class FelixFrameworkInstanceManagerTest
{
  public FelixFrameworkInstanceManagerTest()
  {
    TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder =
        IdeaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder();
    _fixture = fixtureBuilder.getFixture();
  }

  @Before
  public void setUp() throws Exception
  {
    _fixture.setUp();
    _root = ModuleRootManager.getInstance(_fixture.getModule()).getContentRoots()[0];
    _fileSystem = createMock(LocalFileSystem.class);
    _testObject =
        new FelixFrameworkInstanceManager(new LibraryHandlerImpl(), _fileSystem, ApplicationManager.getApplication());
    _instanceDefinition = new FrameworkInstanceDefinition();
    _instanceDefinition.setBaseFolder(new File(_root.getPath(), "felix").getAbsolutePath());
    _instanceDefinition.setName("test");

  }

  @After
  public void tearDown() throws Exception
  {
    _fixture.tearDown();
  }

  @Test
  public void testCheckValidityFolderDoesNotExist()
  {
    expect(_fileSystem.findFileByPath(_instanceDefinition.getBaseFolder())).andReturn(_root.findChild("felix"));
    replay(_fileSystem);
    assertThat(_testObject.checkValidity(_instanceDefinition),
        equalTo(MessageFormat.format(FelixFrameworkInstanceManager.FOLDER_DOES_NOT_EXIST,
            _instanceDefinition.getBaseFolder())));
    verify(_fileSystem);
  }

  @Test
  public void testCheckValidityNoBinFolder() throws Exception
  {
    ApplicationManager.getApplication().runWriteAction(new Runnable()
    {
      public void run()
      {
        try
        {
          _root.createChildDirectory(this, "felix");
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      }
    });

    expect(_fileSystem.findFileByPath(_instanceDefinition.getBaseFolder())).andReturn(_root.findChild("felix"));
    replay(_fileSystem);
    assertThat(_testObject.checkValidity(_instanceDefinition),
        equalTo(
            MessageFormat.format(FelixFrameworkInstanceManager.NO_BIN_FOLDER, _instanceDefinition.getBaseFolder())));
    verify(_fileSystem);
  }

  @Test
  public void testCheckValidityNoBundleFolder() throws Exception
  {
    ApplicationManager.getApplication().runWriteAction(new Runnable()
    {
      public void run()
      {
        try
        {
          _root.createChildDirectory(this, "felix").createChildDirectory(this, "bin");
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      }
    });

    expect(_fileSystem.findFileByPath(_instanceDefinition.getBaseFolder())).andReturn(_root.findChild("felix"));
    replay(_fileSystem);
    assertThat(_testObject.checkValidity(_instanceDefinition),
        equalTo(
            MessageFormat.format(FelixFrameworkInstanceManager.NO_BUNDLE_FOLDER, _instanceDefinition.getBaseFolder())));
    verify(_fileSystem);
  }

  @Test
  public void testCheckValidityOK() throws Exception
  {
    ApplicationManager.getApplication().runWriteAction(new Runnable()
    {
      public void run()
      {
        try
        {
          VirtualFile felixFolder = _root.createChildDirectory(this, "felix");
          felixFolder.createChildDirectory(this, "bin");
          felixFolder.createChildDirectory(this, "bundle");
        }
        catch (IOException e)
        {
          throw new RuntimeException(e);
        }
      }
    });

    expect(_fileSystem.findFileByPath(_instanceDefinition.getBaseFolder())).andReturn(_root.findChild("felix"));
    replay(_fileSystem);
    assertThat(_testObject.checkValidity(_instanceDefinition), nullValue());
    verify(_fileSystem);
  }

  private IdeaProjectTestFixture _fixture;
  private VirtualFile _root;
  private FelixFrameworkInstanceManager _testObject;
  private FrameworkInstanceDefinition _instanceDefinition;
  private LocalFileSystem _fileSystem;
}
