class Bogus {
  x() {
    var destination, descriptionStyle, buttonStyle;

    return new Bingo(
      nothing: "some long string that will prevent lines collapsing here",
      children: <Widget>[
        new Text(destination.description[0], style: descriptionStyle),
        new Text(destination.description[1], style: descriptionStyle),
        new Text(destination.description[2], style: descriptionStyle),
        new Flexible(
          child: new Row(
            justifyContent: FlexJustifyContent.start,
            alignItems: FlexAlignItems.end,
            children: <Widget>[ // list
              new Padding(
                padding: const EdgeDims.only(right: 16.0),
                child: new Text('SHARP', style: buttonStyle),
              ),
              new Text('EXPLODE', style: buttonStyle),
            ],
          ),
        ),
        <caret>new Flexible(
          child: new Row(
            justifyContent: FlexJustifyContent.start,
            alignItems: FlexAlignItems.end,
            children: <Widget>[
              new Padding(
                padding: const EdgeDims.only(right: 16.0),
                child: new Text('SHARE', style: buttonStyle),
              ),
              new Text('EXPLORE', style: buttonStyle),
            ],
          ),
        ),
      ],
    );
  }
}

class Bingo {
  Bingo({nothing: null, children: null});
}

class Widget {}

class Text extends Widget {
  Text(a, {style: null});
}

class Flexible extends Widget {
  Flexible({child: null});
}

class Padding extends Widget {
  Padding({padding: null, child: null});
}

class Row extends Widget {
  Row({justifyContent: null, alignItems: null, children: null});
}

class FlexJustifyContent {
  static const start = null;
}

class FlexAlignItems {
  static const end = null;
}

class EdgeDims {
  const EdgeDims.only({right: null});
}
