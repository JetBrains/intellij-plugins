package jetbrains.plugins.yeoman.projectGenerator.template;

import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.ActionEvent;

public interface YeomanProjectGeneratorSubPanel extends Disposable {
  void render();

  @Nls
  @Nullable
  String validate();

  @NotNull
  YeomanProjectGeneratorSubPanel next(@Nullable ActionEvent e);

  boolean isCreateButtonEnabled();

  void commitSettings();
}
