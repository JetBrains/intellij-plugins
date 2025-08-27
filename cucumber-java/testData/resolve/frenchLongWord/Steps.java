package java8;

import io.cucumber.java8.Fr;

public class FrenchSteps implements Fr {
  public FrenchSteps() {
    Quand("simple", () -> {
    });

    Etantdonnéque("complex", () -> {
    });
  }
}