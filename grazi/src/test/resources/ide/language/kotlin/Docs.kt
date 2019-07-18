@file:Suppress("unused", "MayBeConstant", "UNUSED_PARAMETER", "SpellCheckingInspection")

package ide.language.kotlin

/**
 * A group of *members*.
 *
 * This class has no useful logic; it's just a documentation example.
 *
 * @param T the type of member in this group.
 * @property name the name of this group.
 * @constructor Creates an empty group.
 */
class ExampleClassWithNoTypos<T>(val name: String) {
    /**
     * Adds a [member] to this group.
     * @return the new size of the group.
     */
    fun goodFunction(member: T): Int {
        return 1 // no error comment
    }
}

/**
 * It is <warning descr="ARTICLE_MISSING">friend</warning>
 *
 * <warning descr="PLURAL_VERB_AFTER_THIS">This guy have</warning> no useful logic; it's just a documentation example.
 *
 * @param T the <warning descr="KIND_OF_A">type of a</warning> <warning descr="MORFOLOGIK_RULE_EN_US">membr</warning> in this group.
 * @property name the <warning descr="COMMA_WHICH">name which</warning> group
 * @constructor Creates an empty group.
 */
class ExampleClassWithTypos<T>(val name: String) {
    /**
     * It <warning descr="IT_VBZ">add</warning> a [member] to this <warning descr="MORFOLOGIK_RULE_EN_US">grooup</warning>.
     * @return the new size of <warning descr="DT_DT">a the</warning> group.
     */
    fun badFunction(member: T): Int {
        return 1 // It <warning descr="IT_VBZ">are</warning> <warning descr="MORFOLOGIK_RULE_EN_US">eror</warning> comment
    }
}
