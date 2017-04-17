typedef void VoidCallback1(Event event);
typedef void VoidCallback2(Event event, int x);
typedef void VoidCallback3(Event event, int x, y);
typedef void VoidCallback4(Event event, int x, var y);

typedef Callback1(Event event);
typedef Callback2(Event event, int x);
typedef Callback3(Event event, int x, y);
typedef Callback4(Event event, int x, var y);

typedef int IntCallback1(Event event);
typedef int IntCallback2(Event event, int x);
typedef int IntCallback3(Event event, int x, y);
typedef int IntCallback4(Event event, int x, var y);

typedef Box<int> BoxCallback1(Event event);
typedef Box<int> BoxCallback2(Event event, int x);
typedef Box<int> BoxCallback3(Event event, int x, y);
typedef Box<int> BoxCallback4(Event event, int x, var y);

typedef Box<Box<int>> BoxBoxCallback1(Event event);
typedef Box<Box<int>> BoxBoxCallback2(Event event, int x);
typedef Box<Box<int>> BoxBoxCallback3(Event event, int x, y);
typedef Box<Box<int>> BoxBoxCallback4(Event event, int x, var y);

typedef void VoidCallbak1(Event event);
typedef void VoidCallbak2(Event event, int x);
typedef void VoidCallbak3(Event event, int x, y);
typedef void VoidCallbak4(Event event, int x, var y);

typedef void VoidCallbuk1<E>(E event);
typedef void VoidCallbuk2<E, I>(E event, I x);
typedef void VoidCallbuk3<E extends Event, I extends int>(E event, I x, y);
typedef void VoidCallbuk4<E extends Event<E>, I extends int>(E event, I x,
                                                               var y);

typedef Callbuk1<E>(E event);
typedef Callbuk2<E, I>(E event, I x);
typedef Callbuk3<E extends Event, I>(E event, I x, y);
typedef Callbuk4<E, I extends int>(E event, I x, var y);

typedef int IntCallbuk1<E>(E event);
typedef I IntCallbuk2<E extends Event, I extends int>(E event, I x);
typedef I IntCallbuk3<E, I>(E event, I x, y);
typedef I IntCallbuk4<E, I>(E event, I x, var y);

typedef Box<int> BoxCallbuk1<E>(E event);
typedef Box<I> BoxCallbuk2<E, I>(E event, I x);
typedef Box<I> BoxCallbuk3<E, I>(E event, I x, y);
typedef Box<I> BoxCallbuk4<E, I>(E event, I x, var y);

typedef Box<int> BoxBoxCallbuk1<E>(E event);
typedef Box<I> BoxBoxCallbuk2<E, I>(E event, I x);
typedef Box<I> BoxBoxCallbuk3<E, I>(E event, I x, y);
typedef Box<I> BoxBoxCallbuk4<E, I>(E event, I x, var y);

typedef F<T> = List<T> Function<T>(T,);
typedef G = List<int> Function();
// Function may be a special word or a type, but not a name identifier
typedef I<T> = List<T> Function([int Function<T>() a,
Function(int) a,
Function a,
Function]);
// Here Function may be a special word, type and a name identifier, all cases are tested here
@foo @bar typedef L<T, E> = List<T,E> Function<S,E>(S, {T Function(int, S) factory,
Function<T>() factory,
Function factory});

Function<T>() factory;
typedef X = Function();
typedef Y = Function Function();
typedef Z = Function Function() Function();
typedef Q = Function Function() Function() Function();
x1700(Function() s) {}
x1701(int Function() s) {}
x1702(Function(int x, [List<Function> x2]) Function() s) {}
class a{
  Function() fsa;
  Function Function() Function() fsa1();
  x1703(Function() s) {}
  x1704(int Function() s) {}
  x1705(Function(int x, [List<Function> x2]) Function() s) {}
}
