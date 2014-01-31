abstract class Animal {
  void speak(String say);
}

class Dog implements Animal {
  String name;
  <caret>
  Dog(this.name);
}