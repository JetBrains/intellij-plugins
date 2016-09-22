main(){
  return measure(() {

  });
  var status = "";
  if (status is !int) {
    throw new IllegalArgumentException("int status expected");
  }

  for (int i = 0, len = 10; i < len; i++, --len) {
    print(i);
  }
}

class Foo {
  @Foo(parameters: [[1, 1], [2, 4]])
  foo() {
    bar( themes: const {
      "Default": const Theme(const {LoginForm: const _LoginForm("pure-form")}),
      "Stacked": const Theme(const {LoginForm: const _LoginForm("pure-form pure-form-stacked")})
    });

    bar( themes: {
      "Default": Theme({LoginForm: _LoginForm("pure-form")}),
      "Stacked": Theme({LoginForm: _LoginForm("pure-form pure-form-stacked")})
    });

    var now = new DateTime();
    var elm = new Element.html('<u>54</u>');

    var now1 = DateTime();
    var elm1 = Element.html('<u>54</u>');
  }

  int parse(String int, [int start, int end], {int radix, void onError(String source)});
}

void get _process
  native "return process;";

abstract class Map<K, V> {
  /**
   * Creates a Map instance with the default implementation.
   */
  factory Map() = LinkedHashMap<K, V>;

  /**
   * Creates a Map instance that contains all key-value pairs of [other].
   */
  factory Map.from(Map<K, V> other) = LinkedHashMap<K, V>.from;

  /**
   * Creates an identity map with the default implementation.
   */
  factory Map.identity() = LinkedHashMap<K, V>.identity;

  /**
   * Creates a Map instance in which the keys and values are computed from the [iterable].
   */
  factory Map.fromIterable(Iterable iterable,
      {K key(element), V value(element)}) = LinkedHashMap<K, V>.fromIterable;

  /**
   * Creates a Map instance associating the given [keys] to [values].
   */
  factory Map.fromIterables(Iterable<K> keys, Iterable<V> values)
      = LinkedHashMap<K, V>.fromIterables;

  /**
   * Returns true if this map contains the given value.
   */
  bool containsValue(Object value);

  /**
   * Returns the value for the given [key] or null if [key] is not in the map.
   */
  V operator [](Object key);

  /**
   * Associates the [key] with the given [value].
   */
  void operator []=(K key, V value);

  V putIfAbsent(K key, V ifAbsent());
  void addAll(Map<K, V> other);
  V remove(Object key);
  void clear();
  void forEach(void f(K key, V value));
  Iterable<K> get keys;
  Iterable<V> get values;
  int get length;
  bool get isEmpty;
  bool get isNotEmpty;
}

final _RE_HEADER = const RegExp(r'^(#{1,6})(.*?)#*$');

abstract class Exception {
  factory Exception([var message]) => new _ExceptionImplementation(message);
}
