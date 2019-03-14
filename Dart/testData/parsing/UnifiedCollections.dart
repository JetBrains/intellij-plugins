var a = [
  ...buildTab2Conversation(),
];

Widget build(BuildContext context) {
  return CupertinoPageScaffold(
    child: ListView(
      children: [
        Tab2Header(),
        ...buildTab2Conversation(),
      ],
    ),
  );
}

var args = [
  ...testArgs,
  '--packages=${PackageMap.globalPackagesPath}',
  '-rexpanded',
  ...filePaths
];

var params = {
  "userId": 123,
  "timeout": 300,
  ...uri.queryParameters
};

var items = [2, 3, 4];
var set = { 1, 2, ...items };

var what = { ...a, ...b };

var command = [
  engineDartPath,
  '--target=flutter',
  ...?extraFrontEndOptions,
  mainPath
];

var things = [2, null, 3];
var more = [1, ...things, 4]; // [1, 2, null, 3, 4].
var also = [1, ...?things, 4]; // [1, 2, null, 3, 4].

const list = [...["why"]];

Widget build(BuildContext context) {
  return Row(
    children: [
      IconButton(icon: Icon(Icons.menu)),
      Expanded(child: title),
      if (isAndroid) IconButton(icon: Icon(Icons.search)),
    ]
  );
}

Widget build(BuildContext context) {
  return Row(
    children: [
      IconButton(icon: Icon(Icons.menu)),
      Expanded(child: title),
      if (isAndroid)
        IconButton(icon: Icon(Icons.search))
      else
        IconButton(icon: Icon(Icons.about)),
    ]
  );
}

var command = [
  engineDartPath,
  frontendServer,
  for (var root in fileSystemRoots) '--filesystem-root=$root',
  for (var entryPointsJson in entryPointsJsonFiles)
    if (fileExists("$entryPointsJson.json")) entryPointsJson,
  mainPath
];

var integers = [for (var i = 1; i < 5; i++) i]; // [1, 2, 3, 4]
var squares = [for (var n in integers) n * n]; // [1, 4, 9, 16]

foo() {
  return {
    for (var demo in kAllGalleryDemos)
      '${demo.routeName}': demo.buildRoute,
  };
}

main() async {
  var stream = getAStream();
  var elements = [await for (var element in stream) element];
}

var a = [for (var x in hor) for (var y in vert) Point(x, y)];
var b = [for (var i in integers) if (i.isEven) i * i];

Widget build(BuildContext context) {
  return Row(
    children: [
      IconButton(icon: Icon(Icons.menu)),
      Expanded(child: title),
      if (isAndroid) ...[
        IconButton(icon: Icon(Icons.search)),
        IconButton(icon: Icon(Icons.refresh)),
        IconButton(icon: Icon(Icons.help))
      ],
    ]
  );
}

var routes = {
  for (var demo in kAllGalleryDemos)
    if (demo.documentationUrl != null)
       demo.routeName: demo.documentationUrl
};

Widget build(BuildContext context) {
  final themeData = Theme.of(context);

  return MergeSemantics(
    child: Padding(
      padding: const EdgeInsets.symmetric(vertical: 16.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                for (var line in lines .sublist(0, lines.length - 1))
                  Text(line),
                Text(lines.last, style: themeData.textTheme.caption)
              ]
            )
          ),
          if (icon != null) SizedBox(
            width: 72.0,
            child: IconButton(
              icon: Icon(icon),
              color: themeData.primaryColor,
              onPressed: onPressed
            )
          )
        ]
      )
    ),
  );
}

Widget build(BuildContext context) {
  var buttons = [
    if (isAndroid) IconButton(icon: Icon(Icons.search)),
    IconButton(icon: Icon(Icons.menu))
  ];

  return Row(children: buttons);
}

Widget build(BuildContext context) {
  return Row(children: [
    if (isAndroid) IconButton(icon: Icon(Icons.search)),
    IconButton(icon: Icon(Icons.menu))
  ]);
}

var c = [
  for (var i = 1; i < 4; i++) i,
  for (var i in [1, 2, 3]) i
];
