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

import aQute.lib.osgi.Analyzer;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;

import java.text.MessageFormat;

/**
 * Created by IntelliJ IDEA. User: kork Date: Jul 20, 2009 Time: 9:51:04 PM To change this template use File | Settings
* | File Templates.
*/
class ReportingAnalyzer extends Analyzer
{
  public ReportingAnalyzer(CompileContext context, String sourceFileName)
  {
    super();    //To change body of overridden methods use File | Settings | File Templates.
    _context = context;
    _sourceFileName = sourceFileName;
  }

  @Override
  public void error(String s, Object... objects)
  {
    _context.addMessage(CompilerMessageCategory.ERROR, MessageFormat.format(s, objects), _sourceFileName, 0,0);
  }

  @Override
  public void error(String s, Throwable throwable, Object... objects)
  {
    _context.addMessage(CompilerMessageCategory.ERROR, MessageFormat.format(s, objects) + "(" + throwable.getMessage() + ")", _sourceFileName, 0, 0);
  }

  @Override
  public void warning(String s, Object... objects)
  {
    _context.addMessage(CompilerMessageCategory.WARNING, MessageFormat.format(s, objects), _sourceFileName, 0,0);

  }

  @Override
  public void progress(String s, Object... objects)
  {
    _context.addMessage(CompilerMessageCategory.INFORMATION, MessageFormat.format(s, objects), _sourceFileName, 0,0);

  }

  private CompileContext _context;
  private String _sourceFileName;
}
