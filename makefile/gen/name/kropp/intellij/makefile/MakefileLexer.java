package name.kropp.intellij.makefile;

import com.intellij.lexer.FlexAdapter;

public class MakefileLexer extends FlexAdapter {
  public MakefileLexer() {
    super(new _MakefileLexer());
  }
}
