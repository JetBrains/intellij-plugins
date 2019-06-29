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
        return 1
    }
}

/**
 * It is <warning>friend</warning>
 *
 * <warning>This guy have</warning> no useful logic; it's just a documentation example.
 *
 * @param T the <warning>type of a</warning> <warning>membr</warning> in this group.
 * @property name the <warning>name which</warning> group
 * @constructor Creates an empty group.
 */
class ExampleClassWithTypos<T>(val name: String) {
    /**
     * It <warning>add</warning> a [member] to this <warning>grooup</warning>.
     * @return the new size of <warning>a the</warning> group.
     */
    fun badFunction(member: T): Int {
        return 1
    }
}
