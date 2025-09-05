import io.cucumber.java.pl.Kiedy;
import io.cucumber.java.pl.Wtedy;
import io.cucumber.java.pl.Zakładając;

public class Steps {

  @Zakładając("teraz jestem bardzo głodny")
  public void jestemGłodny() {
  }

  @Kiedy("proszę o radę")
  public void proszęoRadę() {
  }

  @Wtedy("jem ciastko")
  public void jemCiastko() {
  }
}
