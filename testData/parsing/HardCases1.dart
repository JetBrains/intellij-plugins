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


var get _process()
  native "return process;";

interface JsEvaluator {
  var eval(String source);
}

interface Link<T> extends Iterable<T> default LinkFactory<T> {
}

interface Map<K, V> default HashMapImplementation<K extends Hashable, V> {
  Map.from(Map<K, V> other);
}

final _RE_HEADER = const RegExp(@'^(#{1,6})(.*?)#*$');

interface Exception default ExceptionImplementation {
  const Exception([var msg]);
}