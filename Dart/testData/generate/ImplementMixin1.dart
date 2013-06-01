abstract class Persistence {
  void save(String filename) {
    print('saving the object as ${toJson()}');
  }

  void load(String filename) {
    print('loading from $filename');
  }

  abstract Object toJson();
}

abstract class Warrior extends Object with Persistence {
  fight(Warrior other) {
  }
}

class <caret>Ninja extends Warrior {

}