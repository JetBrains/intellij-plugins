class FX {
  Widget build(BuildContext context) {
    return new Scaffold(
      body: new Center(
        child: <selection>new Text('Got $_counter time${ _counter == 1 ? '' : 's' }.',)</selection>,
      ),
    );
  }
}
