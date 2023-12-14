package com.intellij.dts.completion

class DtsPropertyInsertionTest : DtsCompletionTest() {
  private val unproductiveStatements = """
        // comment
        /include/ "file"
        #include "file"
        /*comment*/
    """

  fun `test string (status)`() = doTest(
    input = "status",
    after = "status = \"<caret>\";",
  )

  fun `test string (model)`() = doTest(
    input = "model",
    after = "model = \"<caret>\";",
  )

  fun `test cell array (reg)`() = doTest(
    input = "reg",
    after = "reg = <<caret>>;",
  )

  fun `test cell array (virtual-reg)`() = doTest(
    input = "virtual-reg",
    after = "virtual-reg = <<caret>>;",
  )

  fun `test boolean (dma-coherent)`() = doTest(
    input = "dma-coherent",
    after = "dma-coherent;<caret>",
  )

  fun `test phandle (phandle)`() = doTest(
    input = "phandle",
    after = "phandle = <&<caret>>;",
  )

  fun `test compound (interrupts-extended)`() = doTest(
    input = "interrupts-extended",
    after = "interrupts-extended = <caret>;",
  )

  fun `test no completion if line not empty (comment)`() = doTest(
    lookup = "phandle",
    input = "p<caret> // comment",
    after = "phandle<caret> // comment",
  )

  fun `test no completion if line not empty (text)`() = doTest(
    lookup = "phandle",
    input = "p<caret> text",
    after = "phandle<caret> text",
  )

  fun `test no duplicated tokens (semicolon)`() = doTest(
    lookup = "phandle",
    input = "p<caret> $unproductiveStatements ;",
    after = "phandle = <&<caret>> $unproductiveStatements ;",
  )

  fun `test no duplicated tokens (langl)`() = doTest(
    lookup = "phandle",
    input = "p<caret> $unproductiveStatements >;",
    after = "phandle = <<caret> $unproductiveStatements >;",
  )

  // Fails because array content is not checked.
  fun `failing test no duplicated tokens (and)`() = doTest(
    lookup = "phandle",
    input = "p<caret> $unproductiveStatements &>;",
    after = "phandle = <<caret> $unproductiveStatements &>;",
  )

  fun `test no duplicated tokens (rangl)`() = doTest(
    lookup = "phandle",
    input = "p<caret> $unproductiveStatements <&>;",
    after = "phandle = <caret> $unproductiveStatements <&>;",
  )

  fun `test no duplicated tokens (assign)`() = doTest(
    lookup = "phandle",
    input = "p<caret> $unproductiveStatements = <&>;",
    after = "phandle<caret> $unproductiveStatements = <&>;",
  )

  fun `test const property value (int)`() = doTest(
    lookup = "prop-int" ,
    input = "<caret>",
    after = "prop-int = <10<caret>>;",
    compatible = "custom,const",
  )

  fun `test const property value (hex int)`() = doTest(
    lookup = "prop-int-hex" ,
    input = "<caret>",
    after = "prop-int-hex = <16<caret>>;",
    compatible = "custom,const",
  )

  fun `test const property value (string)`() = doTest(
    lookup = "prop-string" ,
    input = "<caret>",
    after = "prop-string = \"value<caret>\";",
    compatible = "custom,const",
  )

  fun `test const property value (cell array)`() = doTest(
    lookup = "prop-cell-array" ,
    input = "<caret>",
    after = "prop-cell-array = <0 1 2 3<caret>>;",
    compatible = "custom,const",
  )

  fun `test const property value (byte array)`() = doTest(
    lookup = "prop-byte-array" ,
    input = "<caret>",
    after = "prop-byte-array = [00 01 02 03<caret>];",
    compatible = "custom,const",
  )

  fun `test const property value (string array)`() = doTest(
    lookup = "prop-string-array" ,
    input = "<caret>",
    after = "prop-string-array = \"value1\", \"value2<caret>\";",
    compatible = "custom,const",
  )
  fun `test default property value (int)`() = doTest(
    lookup = "prop-int" ,
    input = "<caret>",
    after = "prop-int = <10<caret>>;",
    compatible = "custom,default",
  )

  fun `test default property value (hex int)`() = doTest(
    lookup = "prop-int-hex" ,
    input = "<caret>",
    after = "prop-int-hex = <16<caret>>;",
    compatible = "custom,default",
  )

  fun `test default property value (string)`() = doTest(
    lookup = "prop-string" ,
    input = "<caret>",
    after = "prop-string = \"value<caret>\";",
    compatible = "custom,default",
  )

  fun `test default property value (cell array)`() = doTest(
    lookup = "prop-cell-array" ,
    input = "<caret>",
    after = "prop-cell-array = <0 1 2 3<caret>>;",
    compatible = "custom,default",
  )

  fun `test default property value (byte array)`() = doTest(
    lookup = "prop-byte-array" ,
    input = "<caret>",
    after = "prop-byte-array = [00 01 02 03<caret>];",
    compatible = "custom,default",
  )

  fun `test default property value (string array)`() = doTest(
    lookup = "prop-string-array" ,
    input = "<caret>",
    after = "prop-string-array = \"value1\", \"value2<caret>\";",
    compatible = "custom,default",
  )

  private fun doTest(input: String, after: String) = doTest(
    lookup = input,
    input = "$input<caret>",
    after = after,
  )

  private fun doTest(lookup: String, input: String, after: String, compatible: String = "") {
    addZephyr()

    val surrounding = """
      / {
        compatible = "$compatible"; 
        <embed>
      }; 
    """

    doCompletionTest(
      lookupString = lookup,
      input = input,
      after = after,
      surrounding = surrounding,
      useNodeContentVariations = true,
    )
  }
}