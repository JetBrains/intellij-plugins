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
package org.osmorc.frameworkintegration.impl;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkRunner;
import org.osmorc.frameworkintegration.util.PropertiesWrapper;

import java.io.File;
import java.io.IOException;

/**
 * Abstract base class for framework runners that implements shared default behaviour.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id$
 */
public abstract class AbstractFrameworkRunner<Props extends PropertiesWrapper> implements FrameworkRunner<Props>
{
  protected void createTempFolder()
  {
    // create a temp folder for running all the stuff.
    String tempDir =
        PathManager.getSystemPath() + File.separator + "osmorc" + File.separator + "runtmp" +
            System.currentTimeMillis();
    File f = new File(tempDir);
    f.mkdirs();
    try
    {
      _workingDirectory = f.getCanonicalPath();
    }
    catch (IOException e)
    {
      throw new IllegalStateException("Could not create temp folder. " + tempDir, e);
    }
  }

  @NotNull
  public String getWorkingDirectory()
  {
    return _workingDirectory;
  }

  public void dispose()
  {
    //  kill the temp folder when this runner is disposed
    FileUtil.asyncDelete(new File(_workingDirectory));
  }

  protected String _workingDirectory;
}
