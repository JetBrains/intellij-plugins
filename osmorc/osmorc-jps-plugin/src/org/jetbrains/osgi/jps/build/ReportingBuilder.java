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
package org.jetbrains.osgi.jps.build;

import aQute.bnd.osgi.Builder;
import aQute.bnd.osgi.Processor;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public class ReportingBuilder extends Builder {
  private final Reporter myReporter;

  public ReportingBuilder(@NotNull Reporter reporter) {
    myReporter = reporter;
  }

  public ReportingBuilder(@NotNull Reporter reporter, @NotNull Processor parent) {
    super(parent);
    myReporter = reporter;
    use(parent);
  }

  @Override
  public SetLocation exception(Throwable t, String format, Object... args) {
    Logger.getInstance(myReporter.getClass()).warn(formatArrays(format, args), t);
    return super.exception(t, format, args);
  }

  @Override
  @SuppressWarnings("deprecation")
  public void progress(float progress, @Nls(capitalization = Nls.Capitalization.Sentence) String format, Object... args) {
    myReporter.progress(formatArrays(format, args));
  }

  @Override
  public boolean isTrace() {
    return myReporter.isDebugEnabled();
  }

  @Override
  public void trace(String format, Object... args) {
    myReporter.debug(formatArrays(format, args));
  }
}
