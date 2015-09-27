package com.google.jstestdriver.idea.rt;

import com.google.jstestdriver.Args4jFlagsParser;
import com.google.jstestdriver.Flags;
import com.google.jstestdriver.FlagsImpl;
import com.google.jstestdriver.FlagsParser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class IntelliJFlagParser implements FlagsParser {

  private final JstdSettings mySettings;
  private final FlagsParser myOriginal;
  private final boolean myDryRun;

  public IntelliJFlagParser(@NotNull JstdSettings settings, boolean dryRun) {
    mySettings = settings;
    myDryRun = dryRun;
    myOriginal = new Args4jFlagsParser();
  }

  @Override
  public Flags parseArgument(String[] strings) {
    Flags flags = myOriginal.parseArgument(strings);
    if (flags instanceof FlagsImpl) {
      fix((FlagsImpl) flags);
    }
    return flags;
  }

  private void fix(@NotNull FlagsImpl flags) {
    List<String> tests = mySettings.getTestFileScope().toJstdList();
    if (myDryRun) {
      flags.setDryRunFor(tests);
    }
    else {
      flags.setTests(tests);
    }
  }
}
