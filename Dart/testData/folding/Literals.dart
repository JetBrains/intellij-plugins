/// Sets
// Empty
var s1 = {};

// Multiple lines
var s2 = <fold text='{...}' expand='true'>{
  'a',
  'b',
}</fold>;

// Not folding
var s3 = {'a', 'b'};

/// Maps
// Empty
var m1 = const <K, V>{};

// Not folding
var m2 = const
<K, V>
{1: 2};

var m3 = const {'a' : 'b'};

// Multiple lines
var m4 = const
    <K, V>
    <fold text='{...}' expand='true'>{
      1: 2,
    }</fold>;

/// Lists
// Empty
var l1 = [];

// Multiple lines
var l2 = <fold text='[...]' expand='true'>[
  1,
  2,
  3,
]</fold>;

var l3 = <fold text='[...]' expand='true'>[
  'a',
  'b',
]</fold>;

// Not folding
var l4 = ['a', 'b'];
