package training.check.go

class GoCheckDeleteConversion : GoCheck() {

  override fun check(): Boolean = goFile?.findFunction("main")?.block?.text == "{\n" +
          "\t_ = ioutil.WriteFile(\"./out.txt\", getData(), 0644)\n" +
          "}"

}