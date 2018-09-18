package org.angular2.codeInsight.metadata

data class AngularField(val name: String)

open class AngularClass(val name: String,
                        val sourcePath: String,
                        val inputs: Array<AngularField>,
                        val outputs: Array<AngularField>)

class AngularDirective(name: String,
                       sourcePath: String,
                       inputs: Array<AngularField>,
                       outputs: Array<AngularField>,
                       val selector: String) : AngularClass(name, sourcePath, inputs, outputs)


class AngularMetadata(val classes: Array<AngularClass>) {
  fun findDirectives(selector: String): List<AngularClass> = classes.filter { it is AngularDirective && it.selector.contains(selector) }
  fun findClass(name: String): AngularClass? = classes.find { it.name == name }
}