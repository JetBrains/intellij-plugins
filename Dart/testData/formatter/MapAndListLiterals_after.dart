var simpleList = [1, 2, 3];

var simpleMap = {'a': 1, 'b': 2, 'c': 3};

var fooList = [
  'a',
  'b',
  'c'
];

var barMap = {
  'a': 'b',
  'c': 'd'
};

var listInsideMap = {
  'a': ['b', 'c', 'd'],
  'e': [
    'f',
    'g',
    'h'
  ]
};

var mapInsideMap = {
  'a': {'b': 'c', 'd': 'e'},
  'f': {
    'f': 'g',
    'h': 'i'
  }
};

var mapInsideList = [
  {
    'a': 'b',
    'c': 'd'
  },
  {
    'a': 'b',
    'c': 'd'
  },
];

Widget build(BuildContext context) {
  return Row(
      children: [
        IconButton(icon: Icon(Icons.menu)),
        Expanded(child: title),
        for (var root in fileSystemRoots)
          for (var entryPointsJson in entryPointsJsonFiles)
            if (fileExists("$entryPointsJson.json"))
              for (var root in fileSystemRoots)
                '--filesystem-root=$root',
        if (isAndroid)
          IconButton(icon: Icon(Icons.search))
        else
          IconButton(icon: Icon(Icons.about)),
        IconButton(icon: Icon(Icons.about)),
      ]
  );
}
