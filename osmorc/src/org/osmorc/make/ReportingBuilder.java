/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.osmorc.make;

import aQute.lib.osgi.Builder;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompilerMessageCategory;

import java.text.MessageFormat;

/**
 * Created by IntelliJ IDEA. User: kork Date: Jul 20, 2009 Time: 9:51:18 PM To change this template use File | Settings
* | File Templates.
*/
class ReportingBuilder extends Builder
{
  public ReportingBuilder(CompileContext context, String sourceFileName)
  {
    super();
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

  public void begin() {
    super.begin();
  }

  private CompileContext _context;
  private String _sourceFileName;

}
