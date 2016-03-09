package org.jetbrains.flutter.run;

import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunnerParameters;
import org.jetbrains.annotations.Nullable;

public class FlutterRunnerParameters extends DartCommandLineRunnerParameters implements Cloneable {
  private @Nullable String myFlutterSdkPath = null;

  @Nullable
  public String getFlutterSdkPath() {
    return myFlutterSdkPath;
  }

  public void setFlutterSdkPath(@Nullable String path) {
    myFlutterSdkPath = path;
  }

  @Override
  protected FlutterRunnerParameters clone() {
    final FlutterRunnerParameters clone = (FlutterRunnerParameters)super.clone();
    clone.myFlutterSdkPath = myFlutterSdkPath;
    return clone;
  }
}
