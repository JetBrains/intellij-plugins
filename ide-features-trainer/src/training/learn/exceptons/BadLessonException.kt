package training.learn.exceptons

class BadLessonException : Exception {

  constructor(s: String) : super(s) {}

  constructor() : super() {}
}
