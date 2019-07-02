/**
 * Module description <warning>eror</warning>
 * @module ExampleClassWithNoTypos
 */

/**
 * A group of *members*.
 *
 * This class has no useful logic; it's just a documentation example.
 */
class ExampleClassWithNoTypos {
    /**
     * Creates an empty group
     * @param  {String} name the name of the group
     */
    constructor(name) {
        /** @private */
        this.name = name;
    }

    /**
     * Adds a [member] to this group.
     * @param {String} member member to add
     * @return {Number} the new size of the group.
     */
    goodFunction(member) {
        return 1; // no error comment
    }
}

/**
 * It is <warning>friend</warning>
 *
 * <warning>This guy have</warning> no useful logic; it's just a documentation example.
 */
class ExampleClassWithTypos {
    /**
     * Creates an empty group
     * @param  {String} name the <warning>name which</warning> group
     */
    constructor(name) {
        /** @private */
        this.name = name;
    }

    /**
     * It <warning>add</warning> a [member] to this <warning>grooup</warning>.
     * @param {String} member member to add
     * @return {Number} the new size <warning>a the</warning> group.
     */
    badFunction(member) {
        return 1; // <warning>eror</warning> comment
    }
}

module.exports = ExampleClassWithNoTypos;
