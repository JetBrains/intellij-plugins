class Animal {
  abstract void speak(String say);
}

class Dog implements Animal {
  <caret>
  String name;

  Dog(this.name);
}