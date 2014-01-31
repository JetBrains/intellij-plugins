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


var get _process
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
