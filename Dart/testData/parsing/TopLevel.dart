// Top level functions.
topLevelUntypedFunction() {}
void topLevelTypedFunction(int a) {}

// Top level variables.
final topLevelFinalUntypedVariable = 1;
final topLevelListFinalUntypedVariable = 1, b = 2, c = 3;
final int topLevelFinalTypedVariable = 1;
final int topLevelListFinalTypedVariable = 1, b = 2, c = 3;
int topLevelTypedVariable;
var topLevelUnTypedVariable;
int topLevelListTypedVariable, a, b;
var topLevelListUnTypedVariable, a, b;
var topLevelInitializedVariable = 2;
final topLevelInitializedVariable2 = const Foo();

// Top level setters
get topLevelGetter {}
set topLevelSetter(a) {}
Foo<int> get topLevelGetter3 {}
void set topLevelSetter3(Foo<int> a) {}