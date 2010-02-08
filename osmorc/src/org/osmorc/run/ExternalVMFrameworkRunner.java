package org.osmorc.run;

import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RemoteConnection;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkRunner;
import org.osmorc.run.ui.SelectedBundle;

/**
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public interface ExternalVMFrameworkRunner extends FrameworkRunner {

  RemoteConnection getRemoteConnection();

  /**
   * Fills the command line parameters into the given ParametersList.
   *
   * @param commandLineParameters the list where to fill the command line parameters in.
   * @param bundlesToInstall      the list of bundles to install.
   * @param vmParameters          a list of VM parameters.
   * */
  void fillCommandLineParameters(@NotNull ParametersList commandLineParameters,
                                 @NotNull SelectedBundle[] bundlesToInstall, @NotNull String vmParameters);
}
