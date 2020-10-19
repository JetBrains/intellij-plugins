package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.util.xmlb.annotations.Tag;

@Tag("CHANGE_LIST")
public class ParticularConnectionSettings  {
  public String INTEGRATED_CHANGE_LIST_NUMBER = "";
  public boolean INTEGRATE_CHANGE_LIST = false;
  public boolean INTEGRATE_REVERSE= false;
  public @NlsSafe String INTEGRATE_BRANCH_NAME = null;
  public long INTEGRATE_TO_CHANGELIST_NUM = -1;
}
