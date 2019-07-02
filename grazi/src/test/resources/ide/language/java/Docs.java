/**
 * A group of *members*.
 * <p>
 * This class has no useful logic; it's just a documentation example.
 *
 * @param T the type of member in this group.
 */
class ExampleClassWithNoTypos<T> {

    private String name;

    /**
     * Creates an empty group.
     *
     * @param name The name of the group.
     */
    public ExampleClassWithNoTypos(String name) {
        this.name = name;
    }

    /**
     * Adds a [member] to this group.
     *
     * @param member member to add
     * @return the new size of the group.
     */
    Integer goodFunction(T member) {
        return 1; // no error comment
    }
}

/**
 * It is <warning>friend</warning>
 *
 * <warning>This guy have</warning> no useful logic; it's just a documentation example.
 *
 * @param T the <warning>type of a</warning> <warning>membr</warning> in this group.
 */
class ExampleClassWithTypos<T> {

    private String name;

    /**
     * Creates an empty group.
     *
     * @param name the <warning>name which</warning> group
     */
    public ExampleClassWithTypos(String name) {
        this.name = name;
    }

    /**
     * It <warning>add</warning> a [member] to this <warning>grooup</warning>.
     *
     * @param member member to add
     * @return the new size of <warning>a the</warning> group.
     */
    Integer badFunction(T member) {
        return 1; // <warning>eror</warning> comment
    }
}
