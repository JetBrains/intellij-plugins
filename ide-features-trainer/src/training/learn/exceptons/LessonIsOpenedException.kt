package training.learn.exceptons

class LessonIsOpenedException : Exception {

  constructor(s: String) : super(s) {}

  constructor() : super() {}
}
