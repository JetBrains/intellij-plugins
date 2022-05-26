package jetbrains.plugins.yeoman.settings;


import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import org.jetbrains.annotations.Nullable;

public interface YeomanNodeFiles {

  @Nullable
  NodeJsLocalInterpreter getInterpreter();
}