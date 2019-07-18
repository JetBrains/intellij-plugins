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
 * It is <warning descr="ARTICLE_MISSING">friend</warning>
 *
 * <warning descr="PLURAL_VERB_AFTER_THIS">This guy have</warning> no useful logic; it's just a documentation example.
 *
 * @param T the <warning descr="KIND_OF_A">type of a</warning> <warning descr="MORFOLOGIK_RULE_EN_US">membr</warning> in this group.
 */
class ExampleClassWithTypos<T> {

    private String name;

    /**
     * Creates an empty group.
     *
     * @param name the <warning descr="COMMA_WHICH">name which</warning> group
     */
    public ExampleClassWithTypos(String name) {
        this.name = name;
    }

    /**
     * It <warning descr="IT_VBZ">add</warning> a [member] to this <warning descr="MORFOLOGIK_RULE_EN_US">grooup</warning>.
     *
     * @param member member to add
     * @return the new size of <warning descr="DT_DT">a the</warning> group.
     */
    Integer badFunction(T member) {
        return 1; // It <warning descr="IT_VBZ">are</warning> <warning descr="MORFOLOGIK_RULE_EN_US">eror</warning> comment
    }
}
