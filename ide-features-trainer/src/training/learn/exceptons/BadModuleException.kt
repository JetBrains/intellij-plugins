package training.learn.exceptons

class BadModuleException : Exception {

  constructor() {}

  constructor(s: String) : super(s) {}
}
