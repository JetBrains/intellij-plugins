package training.learn.interfaces

interface Module {

  val lessons: List<Lesson>

  val sanitizedName: String

  var id: String?

  val name: String

  val primaryLanguage: String?

  val moduleType: ModuleType

  val description: String?

  fun giveNotPassedLesson(): Lesson?

  fun giveNotPassedAndNotOpenedLesson(): Lesson?

  fun hasNotPassedLesson(): Boolean
}