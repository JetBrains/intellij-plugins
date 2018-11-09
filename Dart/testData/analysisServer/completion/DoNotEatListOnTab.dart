class MyHomePageState extends State<MyHomePage> {
  @override
  Widget build(BuildContext context) {
    return new Scaffold(
        appBar: new AppBar(
          title: new Text(config.title),
        ),
        body: new Center(
            child: new Row(
              children: <Widget>[
                FlatButton(
                    child: Text("NEXT"),
                    onPressed: () {
                      _usenameController.clear();
                      _passwordController.<caret>
                    }
                ),
                RaisedButton(
                  child: Text("NEXT"),
                )
              ],
            )
        )
    );
  }
}
