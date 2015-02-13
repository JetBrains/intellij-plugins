library foo;

part 'CreateMethodInAnotherFile_part.dart';

main() {
  Iterable b = new Bar().<caret>doSomething(2+2==4, 1+1, 2*2.0, "", [1], {1:2}, #*, null);
}