// Copyright (c) 2015, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

/// This library defines runtime operations on objects used by the code
/// generator.
part of dart._runtime;

// TODO(jmesserly): remove this in favor of _Invocation.
class InvocationImpl extends Invocation {
  final Symbol memberName;
  final List positionalArguments;
  final Map<Symbol, dynamic> namedArguments;
  final List<Type> typeArguments;
  final bool isMethod;
  final bool isGetter;
  final bool isSetter;
  final String failureMessage;

  InvocationImpl(memberName, List<Object> positionalArguments,
      {namedArguments,
      List typeArguments,
      this.isMethod = false,
      this.isGetter = false,
      this.isSetter = false,
      this.failureMessage = 'method not found'})
      : memberName =
            isSetter ? _setterSymbol(memberName) : _dartSymbol(memberName),
        positionalArguments = List.unmodifiable(positionalArguments),
        namedArguments = _namedArgsToSymbols(namedArguments),
        typeArguments = typeArguments == null
            ? const []
            : List.unmodifiable(typeArguments.map(wrapType));

  static Map<Symbol, dynamic> _namedArgsToSymbols(namedArgs) {
    if (namedArgs == null) return const {};
    return Map.unmodifiable(Map.fromIterable(getOwnPropertyNames(namedArgs),
        key: _dartSymbol, value: (k) => JS('', '#[#]', namedArgs, k)));
  }
}

/// Given an object and a method name, tear off the method.
/// Sets the runtime type of the torn off method appropriately,
/// and also binds the object.
///
/// If the optional `f` argument is passed in, it will be used as the method.
/// This supports cases like `super.foo` where we need to tear off the method
/// from the superclass, not from the `obj` directly.
// TODO(leafp): Consider caching the tearoff on the object?
bind(obj, name, method) {
  if (obj == null) obj = jsNull;
  if (method == null) method = JS('', '#[#]', obj, name);
  var f = JS('', '#.bind(#)', method, obj);
  // TODO(jmesserly): canonicalize tearoffs.
  JS('', '#._boundObject = #', f, obj);
  JS('', '#._boundMethod = #', f, method);
  JS('', '#[#] = #', f, _runtimeType, getMethodType(getType(obj), name));
  return f;
}

/// Binds the `call` method of an interface type, handling null.
///
/// Essentially this works like `obj?.call`. It also handles the needs of
/// [dsend]/[dcall], returning `null` if no method was found with the given
/// canonical member [name].
///
/// [name] is typically `"call"` but it could be the [extensionSymbol] for
/// `call`, if we define it on a native type, and [obj] is known statially to be
/// a native type/interface with `call`.
bindCall(obj, name) {
  if (obj == null) return null;
  var ftype = getMethodType(getType(obj), name);
  if (ftype == null) return null;
  var method = JS('', '#[#]', obj, name);
  var f = JS('', '#.bind(#)', method, obj);
  // TODO(jmesserly): canonicalize tearoffs.
  JS('', '#._boundObject = #', f, obj);
  JS('', '#._boundMethod = #', f, method);
  JS('', '#[#] = #', f, _runtimeType, ftype);
  return f;
}

/// Instantiate a generic method.
///
/// We need to apply the type arguments both to the function, as well as its
/// associated function type.
gbind(f, @rest List typeArgs) {
  GenericFunctionType type = JS('!', '#[#]', f, _runtimeType);
  type.checkBounds(typeArgs);
  // Create a JS wrapper function that will also pass the type arguments, and
  // tag it with the instantiated function type.
  var result =
      JS('', '(...args) => #.apply(null, #.concat(args))', f, typeArgs);
  return fn(result, type.instantiate(typeArgs));
}

dloadRepl(obj, field) => dload(obj, replNameLookup(obj, field), false);

// Warning: dload, dput, and dsend assume they are never called on methods
// implemented by the Object base class as those methods can always be
// statically resolved.
dload(obj, field, [@undefined mirrors]) {
  if (JS('!', 'typeof # == "function" && # == "call"', obj, field)) {
    return obj;
  }
  var f = _canonicalMember(obj, field);

  trackCall(obj);
  if (f != null) {
    var type = getType(obj);

    if (hasField(type, f) || hasGetter(type, f)) return JS('', '#[#]', obj, f);
    if (hasMethod(type, f)) return bind(obj, f, null);

    // Always allow for JS interop objects.
    if (!JS<bool>('!', '#', mirrors) && isJsInterop(obj)) {
      return JS('', '#[#]', obj, f);
    }
  }
  return noSuchMethod(obj, InvocationImpl(field, JS('', '[]'), isGetter: true));
}

// Version of dload that matches legacy mirrors behavior for JS types.
dloadMirror(obj, field) => dload(obj, field, true);

_stripGenericArguments(type) {
  var genericClass = getGenericClass(type);
  if (genericClass != null) return JS('', '#()', genericClass);
  return type;
}

// Version of dput that matches legacy Dart 1 type check rules and mirrors
// behavior for JS types.
// TODO(jacobr): remove the type checking rules workaround when mirrors based
// PageLoader code can generate the correct reified generic types.
dputMirror(obj, field, value) => dput(obj, field, value, true);

dputRepl(obj, field, value) =>
    dput(obj, replNameLookup(obj, field), value, false);

dput(obj, field, value, [@undefined mirrors]) {
  var f = _canonicalMember(obj, field);
  trackCall(obj);
  if (f != null) {
    var setterType = getSetterType(getType(obj), f);
    if (setterType != null) {
      if (JS('!', '#', mirrors))
        setterType = _stripGenericArguments(setterType);
      return JS('', '#[#] = #._check(#)', obj, f, setterType, value);
    }
    // Always allow for JS interop objects.
    if (!JS<bool>('!', '#', mirrors) && isJsInterop(obj)) {
      return JS('', '#[#] = #', obj, f, value);
    }
  }
  noSuchMethod(
      obj, InvocationImpl(field, JS('', '[#]', value), isSetter: true));
  return value;
}

/// Returns an error message if function of a given [type] can't be applied to
/// [actuals] and [namedActuals].
///
/// Returns `null` if all checks pass.
String _argumentErrors(FunctionType type, List actuals, namedActuals) {
  // Check for too few required arguments.
  int actualsCount = JS('!', '#.length', actuals);
  var required = type.args;
  int requiredCount = JS('!', '#.length', required);
  if (actualsCount < requiredCount) {
    return 'Dynamic call with too few arguments. '
        'Expected: $requiredCount Actual: $actualsCount';
  }

  // Check for too many postional arguments.
  var extras = actualsCount - requiredCount;
  var optionals = type.optionals;
  if (extras > JS<int>('!', '#.length', optionals)) {
    return 'Dynamic call with too many arguments. '
        'Expected: $requiredCount Actual: $actualsCount';
  }

  // Check if we have invalid named arguments.
  Iterable names;
  var named = type.named;
  if (namedActuals != null) {
    names = getOwnPropertyNames(namedActuals);
    for (var name in names) {
      if (!JS('!', '#.hasOwnProperty(#)', named, name)) {
        return "Dynamic call with unexpected named argument '$name'.";
      }
    }
  }
  // Now that we know the signature matches, we can perform type checks.
  for (var i = 0; i < requiredCount; ++i) {
    JS('', '#[#]._check(#[#])', required, i, actuals, i);
  }
  for (var i = 0; i < extras; ++i) {
    JS('', '#[#]._check(#[#])', optionals, i, actuals, i + requiredCount);
  }
  if (names != null) {
    for (var name in names) {
      JS('', '#[#]._check(#[#])', named, name, namedActuals, name);
    }
  }
  return null;
}

_toSymbolName(symbol) => JS('', '''(() => {
        let str = $symbol.toString();
        // Strip leading 'Symbol(' and trailing ')'
        return str.substring(7, str.length-1);
    })()''');

_toDisplayName(name) => JS('', '''(() => {
      // Names starting with _ are escaped names used to disambiguate Dart and
      // JS names.
      if ($name[0] === '_') {
        // Inverse of
        switch($name) {
          case '_get':
            return '[]';
          case '_set':
            return '[]=';
          case '_negate':
            return 'unary-';
          case '_constructor':
          case '_prototype':
            return $name.substring(1);
        }
      }
      return $name;
  })()''');

Symbol _dartSymbol(name) {
  return (JS<bool>('!', 'typeof # === "symbol"', name))
      ? JS('Symbol', '#(new #.new(#, #))', const_, PrivateSymbol,
          _toSymbolName(name), name)
      : JS('Symbol', '#(new #.new(#))', const_, internal.Symbol,
          _toDisplayName(name));
}

Symbol _setterSymbol(name) {
  return (JS<bool>('!', 'typeof # === "symbol"', name))
      ? JS('Symbol', '#(new #.new(# + "=", #))', const_, PrivateSymbol,
          _toSymbolName(name), name)
      : JS('Symbol', '#(new #.new(# + "="))', const_, internal.Symbol,
          _toDisplayName(name));
}

_checkAndCall(f, ftype, obj, typeArgs, args, named, displayName) =>
    JS('', '''(() => {
  $trackCall($obj);

  let originalTarget = obj === void 0 ? f : obj;

  function callNSM(errorMessage) {
    return $noSuchMethod(originalTarget, new $InvocationImpl.new(
        $displayName, $args, {
          namedArguments: $named,
          typeArguments: $typeArgs,
          isMethod: true,
          failureMessage: errorMessage
        }));
  }
  if ($f == null) return callNSM('Dynamic call of null.');
  if (!($f instanceof Function)) {
    // We're not a function (and hence not a method either)
    // Grab the `call` method if it's not a function.
    if ($f != null) {
      // Getting the member succeeded, so update the originalTarget.
      // (we're now trying `call()` on `f`, so we want to call its nSM rather
      // than the original target's nSM).
      originalTarget = f;
      $f = ${bindCall(f, _canonicalMember(f, 'call'))};
      $ftype = null;
      $displayName = "call";
    }
    if ($f == null) return callNSM(
        "Dynamic call of object has no instance method 'call'.");
  }
  // If f is a function, but not a method (no method type)
  // then it should have been a function valued field, so
  // get the type from the function.
  if ($ftype == null) $ftype = $f[$_runtimeType];

  if ($ftype == null) {
    // TODO(leafp): Allow JS objects to go through?
    if ($typeArgs != null) {
      // TODO(jmesserly): is there a sensible way to handle these?
      $throwTypeError('call to JS object `' + $obj +
          '` with type arguments <' + $typeArgs + '> is not supported.');
    }

    if ($named != null) $args.push($named);
    return $f.apply($obj, $args);
  }

  // TODO(vsm): Remove when we no longer need mirrors metadata.
  // An array is used to encode annotations attached to the type.
  if ($ftype instanceof Array) $ftype = $ftype[0];

  // Apply type arguments
  if ($ftype instanceof $GenericFunctionType) {
    let formalCount = $ftype.formalCount;

    if ($typeArgs == null) {
      $typeArgs = $ftype.instantiateDefaultBounds();
    } else if ($typeArgs.length != formalCount) {
      return callNSM('Dynamic call with incorrect number of type arguments. ' +
          'Expected: ' + formalCount + ' Actual: ' + $typeArgs.length);
    } else {
      $ftype.checkBounds($typeArgs);
    }
    $ftype = $ftype.instantiate($typeArgs);
  } else if ($typeArgs != null) {
    return callNSM('Dynamic call with unexpected type arguments. ' +
        'Expected: 0 Actual: ' + $typeArgs.length);
  }
  let errorMessage = $_argumentErrors($ftype, $args, $named);
  if (errorMessage == null) {
    if ($typeArgs != null) $args = $typeArgs.concat($args);
    if ($named != null) $args.push($named);
    return $f.apply($obj, $args);
  }
  return callNSM(errorMessage);
})()''');

dcall(f, args, [@undefined named]) => _checkAndCall(
    f, null, JS('', 'void 0'), null, args, named, JS('', 'f.name'));

dgcall(f, typeArgs, args, [@undefined named]) =>
    _checkAndCall(f, null, JS('', 'void 0'), typeArgs, args, named, 'call');

/// Helper for REPL dynamic invocation variants that make a best effort to
/// enable accessing private members across library boundaries.
replNameLookup(object, field) => JS('', '''(() => {
  let rawField = $field;
  if (typeof(field) == 'symbol') {
    // test if the specified field exists in which case it is safe to use it.
    if ($field in $object) return $field;

    // Symbol is from a different library. Make a best effort to
    $field = $field.toString();
    $field = $field.substring('Symbol('.length, field.length - 1);

  } else if ($field.charAt(0) != '_') {
    // Not a private member so default call path is safe.
    return $field;
  }

  // If the exact field name is present, invoke callback with it.
  if ($field in $object) return $field;

  // TODO(jacobr): warn if there are multiple private members with the same
  // name which could happen if super classes in different libraries have
  // the same private member name.
  let proto = $object;
  while (proto !== null) {
    // Private field (indicated with "_").
    let symbols = Object.getOwnPropertySymbols(proto);
    let target = 'Symbol(' + $field + ')';

    for (let s = 0; s < symbols.length; s++) {
      let sym = symbols[s];
      if (target == sym.toString()) return sym;
    }
    proto = proto.__proto__;
  }
  // We didn't find a plausible alternate private symbol so just fall back
  // to the regular field.
  return rawField;
})()''');

/// Shared code for dsend, dindex, and dsetindex.
callMethod(obj, name, typeArgs, args, named, displayName) {
  if (JS('!', 'typeof # == "function" && # == "call"', obj, name)) {
    return dgcall(obj, typeArgs, args, named);
  }
  var symbol = _canonicalMember(obj, name);
  if (symbol == null) {
    return noSuchMethod(obj, InvocationImpl(displayName, args, isMethod: true));
  }
  var f = obj != null ? JS('', '#[#]', obj, symbol) : null;
  var type = getType(obj);
  var ftype = getMethodType(type, symbol);
  // No such method if dart object and ftype is missing.
  return _checkAndCall(f, ftype, obj, typeArgs, args, named, displayName);
}

dsend(obj, method, args, [@undefined named]) =>
    callMethod(obj, method, null, args, named, method);

dgsend(obj, typeArgs, method, args, [@undefined named]) =>
    callMethod(obj, method, typeArgs, args, named, method);

dsendRepl(obj, method, args, [@undefined named]) =>
    callMethod(obj, replNameLookup(obj, method), null, args, named, method);

dgsendRepl(obj, typeArgs, method, args, [@undefined named]) =>
    callMethod(obj, replNameLookup(obj, method), typeArgs, args, named, method);

dindex(obj, index) => callMethod(obj, '_get', null, [index], null, '[]');

dsetindex(obj, index, value) =>
    callMethod(obj, '_set', null, [index, value], null, '[]=');

@notNull
@JSExportName('is')
bool instanceOf(obj, type) {
  if (obj == null) {
    return identical(type, unwrapType(Null)) || _isTop(type);
  }
  return isSubtypeOf(getReifiedType(obj), type);
}

@JSExportName('as')
cast(obj, type, @notNull bool isImplicit) {
  if (obj == null) return obj;
  var actual = getReifiedType(obj);
  if (isSubtypeOf(actual, type)) {
    return obj;
  }
  return castError(obj, type, isImplicit);
}

bool test(bool obj) {
  if (obj == null) _throwBooleanConversionError();
  return obj;
}

bool dtest(obj) {
  if (obj is! bool) booleanConversionFailed(obj);
  return obj;
}

void _throwBooleanConversionError() => throw BooleanConversionAssertionError();

void booleanConversionFailed(obj) {
  var actual = typeName(getReifiedType(test(obj)));
  throw TypeErrorImpl("type '$actual' is not a 'bool' in boolean expression");
}

asInt(obj) {
  if (obj == null) return null;

  if (JS('!', 'Math.floor(#) != #', obj, obj)) {
    castError(obj, JS('', '#', int), false);
  }
  return obj;
}

/// Checks that `x` is not null or undefined.
//
// TODO(jmesserly): inline this, either by generating it as a function into
// the module, or via some other pattern such as:
//
//     <expr> || nullErr()
//     (t0 = <expr>) != null ? t0 : nullErr()
@JSExportName('notNull')
_notNull(x) {
  if (x == null) throwNullValueError();
  return x;
}

/// The global constant map table.
final constantMaps = JS('', 'new Map()');

// TODO(leafp): This table gets quite large in apps.
// Keeping the paths is probably expensive.  It would probably
// be more space efficient to just use a direct hash table with
// an appropriately defined structural equality function.
Object _lookupNonTerminal(Object map, Object key) {
  var result = JS('', '#.get(#)', map, key);
  if (result != null) return result;
  JS('', '#.set(#, # = new Map())', map, key, result);
  return result;
}

Map<K, V> constMap<K, V>(JSArray elements) {
  var count = elements.length;
  var map = _lookupNonTerminal(constantMaps, count);
  for (var i = 0; i < count; i++) {
    map = _lookupNonTerminal(map, JS('', '#[#]', elements, i));
  }
  map = _lookupNonTerminal(map, K);
  var result = JS('', '#.get(#)', map, V);
  if (result != null) return result;
  result = ImmutableMap<K, V>.from(elements);
  JS('', '#.set(#, #)', map, V, result);
  return result;
}

final constantSets = JS('', 'new Map()');
var _immutableSetConstructor;

// We cannot invoke private class constructors directly in Dart.
Set<E> _createImmutableSet<E>(JSArray<E> elements) {
  _immutableSetConstructor ??=
      JS('', '#.#', getLibrary('dart:collection'), '_ImmutableSet\$');
  return JS('', 'new (#(#)).from(#)', _immutableSetConstructor, E, elements);
}

Set<E> constSet<E>(JSArray<E> elements) {
  var count = elements.length;
  var map = _lookupNonTerminal(constantSets, count);
  for (var i = 0; i < count; i++) {
    map = _lookupNonTerminal(map, JS('', '#[#]', elements, i));
  }
  var result = JS('', '#.get(#)', map, E);
  if (result != null) return result;
  result = _createImmutableSet<E>(elements);
  JS('', '#.set(#, #)', map, E, result);
  return result;
}

bool dassert(value) {
  if (JS('!', '# != null && #[#] instanceof #', value, value, _runtimeType,
      AbstractFunctionType)) {
    value = dcall(value, []);
  }
  return dtest(value);
}

final _value = JS('', 'Symbol("_value")');

///
/// Looks up a sequence of [keys] in [map], recursively, and
/// returns the result. If the value is not found, [valueFn] will be called to
/// add it. For example:
///
///     let map = new Map();
///     putIfAbsent(map, [1, 2, 'hi ', 'there '], () => 'world');
///
/// ... will create a Map with a structure like:
///
///     { 1: { 2: { 'hi ': { 'there ': 'world' } } } }
///
multiKeyPutIfAbsent(map, keys, valueFn) => JS('', '''(() => {
  for (let k of $keys) {
    let value = $map.get(k);
    if (!value) {
      // TODO(jmesserly): most of these maps are very small (e.g. 1 item),
      // so it may be worth optimizing for that.
      $map.set(k, value = new Map());
    }
    $map = value;
  }
  if ($map.has($_value)) return $map.get($_value);
  let value = $valueFn();
  $map.set($_value, value);
  return value;
})()''');

/// The global constant table.
/// This maps the number of names in the object (n)
/// to a path of length 2*n of maps indexed by the name and
/// and value of the field.  The final map is
/// indexed by runtime type, and contains the canonical
/// version of the object.
final constants = JS('', 'new Map()');

///
/// Canonicalize a constant object.
///
/// Preconditions:
/// - `obj` is an objects or array, not a primitive.
/// - nested values of the object are themselves already canonicalized.
///
@JSExportName('const')
const_(obj) => JS('', '''(() => {
  let names = $getOwnNamesAndSymbols($obj);
  let count = names.length;
  // Index by count.  All of the paths through this map
  // will have 2*count length.
  let map = $_lookupNonTerminal($constants, count);
  // TODO(jmesserly): there's no guarantee in JS that names/symbols are
  // returned in the same order.
  //
  // We could probably get the same order if we're judicious about
  // initializing fields in a consistent order across all const constructors.
  // Alternatively we need a way to sort them to make consistent.
  //
  // Right now we use the (name,value) pairs in sequence, which prevents
  // an object with incorrect field values being returned, but won't
  // canonicalize correctly if key order is different.
  //
  // See issue https://github.com/dart-lang/sdk/issues/30876
  for (let i = 0; i < count; i++) {
    let name = names[i];
    map = $_lookupNonTerminal(map, name);
    map = $_lookupNonTerminal(map, $obj[name]);
  }
  // TODO(leafp): It may be the case that the reified type
  // is always one of the keys already used above?
  let type = $getReifiedType($obj);
  let value = map.get(type);
  if (value) return value;
  map.set(type, $obj);
  return $obj;
})()''');

/// The global constant list table.
/// This maps the number of elements in the list (n)
/// to a path of length n of maps indexed by the value
/// of the field.  The final map is indexed by the element
/// type and contains the canonical version of the list.
final constantLists = JS('', 'new Map()');

/// Canonicalize a constant list
constList(elements, elementType) => JS('', '''(() => {
  let count = $elements.length;
  let map = $_lookupNonTerminal($constantLists, count);
  for (let i = 0; i < count; i++) {
    map = $_lookupNonTerminal(map, elements[i]);
  }
  let value = map.get($elementType);
  if (value) return value;

  ${getGenericClass(JSArray)}($elementType).unmodifiable($elements);
  map.set($elementType, elements);
  return elements;
})()''');

constFn(x) => JS('', '() => x');

/// Gets the extension symbol given a member [name].
///
/// This is inlined by the compiler when used with a literal string.
extensionSymbol(String name) => JS('', 'dartx[#]', name);

// The following are helpers for Object methods when the receiver
// may be null. These should only be generated by the compiler.
bool equals(x, y) {
  // We handle `y == null` inside our generated operator methods, to keep this
  // function minimal.
  // This pattern resulted from performance testing; it found that dispatching
  // was the fastest solution, even for primitive types.
  return JS('!', '# == null ? # == null : #[#](#)', x, y, x,
      extensionSymbol('_equals'), y);
}

int hashCode(obj) {
  return obj == null ? 0 : JS('!', '#[#]', obj, extensionSymbol('hashCode'));
}

@JSExportName('toString')
String _toString(obj) {
  if (obj == null) return "null";
  if (obj is String) return obj;
  return JS('!', '#[#]()', obj, extensionSymbol('toString'));
}

/// Converts to a non-null [String], equivalent to
/// `dart.notNull(dart.toString(obj))`.
///
/// This is commonly used in string interpolation.
@notNull
String str(obj) {
  if (obj == null) return "null";
  if (obj is String) return obj;
  return _notNull(JS('!', '#[#]()', obj, extensionSymbol('toString')));
}

// TODO(jmesserly): is the argument type verified statically?
noSuchMethod(obj, Invocation invocation) {
  if (obj == null) defaultNoSuchMethod(obj, invocation);
  return JS('', '#[#](#)', obj, extensionSymbol('noSuchMethod'), invocation);
}

/// The default implementation of `noSuchMethod` to match `Object.noSuchMethod`.
defaultNoSuchMethod(obj, Invocation i) {
  throw NoSuchMethodError.withInvocation(obj, i);
}

runtimeType(obj) {
  return obj == null ? Null : JS('', '#[dartx.runtimeType]', obj);
}

final identityHashCode_ = JS('', 'Symbol("_identityHashCode")');

/// Adapts a Dart `get iterator` into a JS `[Symbol.iterator]`.
// TODO(jmesserly): instead of an adaptor, we could compile Dart iterators
// natively implementing the JS iterator protocol. This would allow us to
// optimize them a bit.
final JsIterator = JS('', '''
  class JsIterator {
    constructor(dartIterator) {
      this.dartIterator = dartIterator;
    }
    next() {
      let i = this.dartIterator;
      let done = !i.moveNext();
      return { done: done, value: done ? void 0 : i.current };
    }
  }
''');

_canonicalMember(obj, name) {
  // Private names are symbols and are already canonical.
  if (JS('!', 'typeof # === "symbol"', name)) return name;

  if (obj != null && JS<bool>('!', '#[#] != null', obj, _extensionType)) {
    return JS('', 'dartx.#', name);
  }

  // Check for certain names that we can't use in JS
  if (JS('!', '# == "constructor" || # == "prototype"', name, name)) {
    JS('', '# = "+" + #', name, name);
  }
  return name;
}

/// Emulates the implicit "loadLibrary" function provided by a deferred library.
///
/// Libraries are not actually deferred in DDC, so this just returns a future
/// that completes immediately.
Future loadLibrary() => Future.value();

/// Defines lazy statics.
void defineLazy(to, from) {
  for (var name in getOwnNamesAndSymbols(from)) {
    defineLazyField(to, name, getOwnPropertyDescriptor(from, name));
  }
}
