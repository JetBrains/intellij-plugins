abstract class Animal {
  void speak(final String say);
}

class Dog implements Animal {
  <caret>
  String name;

  Dog(this.name);
}