package org.osmorc.run;

import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RemoteConnection;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkRunner;
import org.osmorc.run.ui.SelectedBundle;

/**
 * Marker interface for framework runners which will start a VM outside of IntellIJ.
 * @author <a href="janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public interface ExternalVMFrameworkRunner extends FrameworkRunner {
}
