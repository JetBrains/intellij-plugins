class FX {
  Widget build(BuildContext context) {
    return new Scaffold(
      body: new Center(
        child: <selection>new Text(
          'Button tapped $_counter time${ _counter == 1 ? '' : 's' }.',
        )</selection>,
      ),
    );
  }
}
