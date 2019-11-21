package training.check.go

class GoCheckFillStructFields : GoCheck() {

  override fun check(): Boolean = goFile?.findFunction("main")?.block?.text == "{\n" +
          "\tp := Person{\n" +
          "\t\tname:    \"\",\n" +
          "\t\tage:     0,\n" +
          "\t\taddress: Address{},\n" +
          "\t}\n" +
          "\tfmt.Println(p)\n" +
          "}"

}