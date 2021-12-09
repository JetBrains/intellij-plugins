package org.jetbrains.idea.perforce.perforce.jobs;

public interface JobsSearchSpecificator {
  String[] addParams(final String[] s);

  /**
   * negative for not defined
   */
  int getMaxCount();

}
