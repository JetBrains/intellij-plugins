package com.intellij.dts.completion

class DtsPropertyValueCompletionTest : DtsCompletionTest() {
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

  fun `test no completion if line not empty (comment)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "phandle",
      input = "p<caret> // comment",
      after = "phandle<caret> // comment",
    )
  }

  fun `test no completion if line not empty (text)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "phandle",
      input = "p<caret> text",
      after = "phandle<caret> text",
    )
  }

  fun `test no duplicated tokens (semicolon)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "phandle",
      input = "p<caret> $unproductiveStatements ;",
      after = "phandle = <&<caret>>$unproductiveStatements ;",
    )
  }

  fun `test no duplicated tokens (langl)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "phandle",
      input = "p<caret> $unproductiveStatements >;",
      after = "phandle = <<caret>$unproductiveStatements >;",
    )
  }

  // Fails because array content is not checked.
  fun `failing test no duplicated tokens (and)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "phandle",
      input = "p<caret> $unproductiveStatements &>;",
      after = "phandle = <<caret>$unproductiveStatements &>;",
    )
  }

  fun `test no duplicated tokens (rangl)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "phandle",
      input = "p<caret> $unproductiveStatements <&>;",
      after = "phandle = <caret>$unproductiveStatements <&>;",
    )
  }

  fun `test no duplicated tokens (assign)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "phandle",
      input = "p<caret> $unproductiveStatements = <&>;",
      after = "phandle<caret> $unproductiveStatements = <&>;",
    )
  }

  fun `test const property value (int)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "prop-int",
      input = "<caret>",
      after = "prop-int = <10<caret>>;",
      compatible = "custom,const",
    )
  }

  fun `test const property value (hex int)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "prop-int-hex",
      input = "<caret>",
      after = "prop-int-hex = <16<caret>>;",
      compatible = "custom,const",
    )
  }

  fun `test const property value (string)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "prop-string",
      input = "<caret>",
      after = "prop-string = \"value<caret>\";",
      compatible = "custom,const",
    )
  }

  fun `test const property value (cell array)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "prop-cell-array",
      input = "<caret>",
      after = "prop-cell-array = <0 1 2 3<caret>>;",
      compatible = "custom,const",
    )
  }

  fun `test const property value (byte array)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "prop-byte-array",
      input = "<caret>",
      after = "prop-byte-array = [00 01 02 03<caret>];",
      compatible = "custom,const",
    )
  }

  fun `test const property value (string array)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "prop-string-array",
      input = "<caret>",
      after = "prop-string-array = \"value1\", \"value2<caret>\";",
      compatible = "custom,const",
    )
  }

  fun `test default property value (int)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "prop-int",
      input = "<caret>",
      after = "prop-int = <10<caret>>;",
      compatible = "custom,default",
    )
  }

  fun `test default property value (hex int)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "prop-int-hex",
      input = "<caret>",
      after = "prop-int-hex = <16<caret>>;",
      compatible = "custom,default",
    )
  }

  fun `test default property value (string)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "prop-string",
      input = "<caret>",
      after = "prop-string = \"value<caret>\";",
      compatible = "custom,default",
    )
  }

  fun `test default property value (cell array)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "prop-cell-array",
      input = "<caret>",
      after = "prop-cell-array = <0 1 2 3<caret>>;",
      compatible = "custom,default",
    )
  }

  fun `test default property value (byte array)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "prop-byte-array",
      input = "<caret>",
      after = "prop-byte-array = [00 01 02 03<caret>];",
      compatible = "custom,default",
    )
  }

  fun `test default property value (string array)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "prop-string-array",
      input = "<caret>",
      after = "prop-string-array = \"value1\", \"value2<caret>\";",
      compatible = "custom,default",
    )
  }

  fun `test enum property value (int)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "10",
      input = "prop-int = <<caret>>;",
      after = "prop-int = <10<caret>>;",
      compatible = "custom,enum",
    )
  }

  fun `test enum property value (string)`() = dtsTimeoutRunBlocking {
    doTest(
      lookup = "value1",
      input = "prop-string = \"<caret>\";",
      after = "prop-string = \"value1<caret>\";",
      compatible = "custom,enum",
    )
  }

  private fun doTest(input: String, after: String) = dtsTimeoutRunBlocking {
    doTest(
      lookup = input,
      input = "$input<caret>",
      after = after,
    )
  }

  private suspend fun doTest(lookup: String, input: String, after: String, compatible: String = "") {
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