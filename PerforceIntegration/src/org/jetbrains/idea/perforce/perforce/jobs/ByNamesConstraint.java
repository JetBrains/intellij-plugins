package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ByNamesConstraint implements JobsSearchSpecificator {
  private final List<String> myNames;

  public ByNamesConstraint(List<String> names) {
    myNames = names;
  }

  @Override
  public String[] addParams(final String[] s) {
    if (myNames.isEmpty()) {
      return s;
    }
    final List<String> list = new ArrayList<>(s.length + 2);
    ContainerUtil.addAll(list, s);
    list.add("-e");
    list.add(getNamesConstraint());
    return ArrayUtilRt.toStringArray(list);
  }

  @Override
  public int getMaxCount() {
    return -1;
  }

  private String getNamesConstraint() {
    return "Job=" + StringUtil.join(myNames, s -> StringUtil.replace(s,
                                                                     Arrays.asList("\\", "&", "|", "=", ">", "<", "^"),
                                                                     Arrays.asList("\\\\", "\\&", "\\|", "\\=", "\\>", "\\<", "\\^")), "|");
  }
}
