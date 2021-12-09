package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;

import java.util.ArrayList;
import java.util.List;

public class JobViewSearchSpecificator implements JobsSearchSpecificator {
  private final String myJobView;
  private final String myCustomFilter;

  public JobViewSearchSpecificator(final String jobView, final String customFilter) {
    myJobView = jobView;
    myCustomFilter = customFilter;
  }

  @Override
  public String[] addParams(final String[] s) {
    final List<String> list = new ArrayList<>(s.length + 2);
    ContainerUtil.addAll(list, s);
    list.add("-m");
    list.add("" + (FullSearchSpecificator.ourMaxLines + 1));

    final boolean notEmptyJobView = !StringUtil.isEmptyOrSpaces(myJobView);
    final boolean notEmptyCustomFilter = !StringUtil.isEmptyOrSpaces(myCustomFilter);
    if (notEmptyJobView || notEmptyCustomFilter) {
      list.add("-e");
      final StringBuilder sb = new StringBuilder();
      if (notEmptyJobView) {
        sb.append("(").append(myJobView).append(")");
      }
      if (notEmptyCustomFilter) {
        if (notEmptyJobView) {
          sb.append(" & ");
        }
        sb.append("(").append(myCustomFilter).append(")");
      }
      list.add(sb.toString());
    }
    return ArrayUtilRt.toStringArray(list);
  }

  @Override
  public int getMaxCount() {
    return 50;
  }
}
