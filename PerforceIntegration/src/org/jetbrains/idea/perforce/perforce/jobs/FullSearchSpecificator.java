package org.jetbrains.idea.perforce.perforce.jobs;

import com.intellij.openapi.util.Pair;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.containers.ContainerUtil;

import java.util.ArrayList;
import java.util.List;

public class FullSearchSpecificator implements JobsSearchSpecificator {
  // todo setting
  public static final int ourMaxLines = 500;
  private final List<Pair<Parts, String>> myStandard = new ArrayList<>();

  public void addStandardConstraint(final Parts part, final String pattern) {
    myStandard.add(Pair.create(part, pattern));
  }

  @Override
  public String[] addParams(String[] s) {
    final List<String> list = new ArrayList<>(s.length + 2);
    ContainerUtil.addAll(list, s);
    list.add("-m");
    list.add("" + (ourMaxLines + 1));
    if (!(myStandard.isEmpty())) {
      list.add("-e");
      list.add(createPatterns());
    }
    return ArrayUtilRt.toStringArray(list);
  }

  @Override
  public int getMaxCount() {
    return ourMaxLines;
  }

  private String createPatterns() {
    final StringBuilder sb = new StringBuilder();
    for (Pair<Parts, String> pair : myStandard) {
      sb.append("(");
      pair.getFirst().add(sb, pair.getSecond());
      sb.append(") ");
    }
    return sb.toString();
  }

  public enum Parts {
    jobname("Job") {
      @Override
      protected void addImpl(final StringBuilder sb, final String pattern) {
        sb.append("=").append(pattern);
      }},
    status("Status") {
      @Override
      protected void addImpl(final StringBuilder sb, final String pattern) {
        sb.append("=").append(pattern);
      }},
    user("User") {
      @Override
      protected void addImpl(final StringBuilder sb, final String pattern) {
        sb.append("=").append(pattern);
      }},
    dateBefore("Date") {
      @Override
      protected void addImpl(final StringBuilder sb, final String pattern) {
        sb.append("<=").append(pattern);
      }},
    dateAfter("Date") {
      @Override
      protected void addImpl(final StringBuilder sb, final String pattern) {
        sb.append(">=").append(pattern);
      }},
    description("Description") {
      @Override
      protected void addImpl(final StringBuilder sb, final String pattern) {
        sb.append("=").append(pattern);
      }};

    private final String myName;

    Parts(final String pattern) {
      myName = pattern;
    }

    public void add(final StringBuilder sb, final String pattern) {
      sb.append(myName);
      addImpl(sb, pattern);
    }

    protected abstract void addImpl(final StringBuilder sb, final String pattern);
  }
}
