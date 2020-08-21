// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.jps.build;

import aQute.bnd.build.ProjectBuilder;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ReportingProjectBuilder extends ProjectBuilder {
  private final Reporter myReporter;

  public ReportingProjectBuilder(@NotNull Reporter reporter, @NotNull ProjectBuilder parent) {
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
