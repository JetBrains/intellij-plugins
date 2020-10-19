package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.View;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author peter
 */
class ClientData {
  private final Map<String, List<String>> myData;
  private volatile List<View> myViews;
  private volatile List<String> myOptions;

  ClientData(Map<String, List<String>> data) {
    myData = Collections.unmodifiableMap(data);
  }

  List<String> getAllRoots() {
    return ContainerUtil.concat(myData.getOrDefault(PerforceRunner.CLIENTSPEC_ROOT, Collections.emptyList()),
                                myData.getOrDefault(PerforceRunner.CLIENTSPEC_ALTROOTS, Collections.emptyList()));
  }

  @Override
  public String toString() {
    return myData.toString();
  }

  @NotNull
  List<String> getOptions() {
    if (myOptions == null) {
      List<String> list = myData.getOrDefault(PerforceRunner.CLIENT_OPTIONS, Collections.emptyList());
      myOptions = !list.isEmpty() ? StringUtil.split(list.get(0), " ") : Collections.emptyList();
    }
    return myOptions;
  }

  @NotNull
  List<View> getViews() {
    if (myViews == null) {
      myViews = ContainerUtil.mapNotNull(myData.getOrDefault(PerforceRunner.VIEW, Collections.emptyList()), View::create);
    }
    return myViews;
  }
}
