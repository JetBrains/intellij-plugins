/**
 * Module description <warning descr="MORFOLOGIK_RULE_EN_US">eror</warning>
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
 * It is <warning descr="ARTICLE_MISSING">friend</warning>
 *
 * <warning descr="PLURAL_VERB_AFTER_THIS">This guy have</warning> no useful logic; it's just a documentation example.
 */
class ExampleClassWithTypos {
    /**
     * Creates an empty group
     * @param  {String} name the <warning descr="COMMA_WHICH">name which</warning> group
     */
    constructor(name) {
        /** @private */
        this.name = name;
    }

    /**
     * It <warning descr="IT_VBZ">add</warning> a [member] to this <warning descr="MORFOLOGIK_RULE_EN_US">grooup</warning>.
     * @param {String} member member to add
     * @return {Number} the new size <warning descr="DT_DT">a the</warning> group.
     */
    badFunction(member) {
        return 1; // It <warning descr="IT_VBZ">are</warning> <warning descr="MORFOLOGIK_RULE_EN_US">eror</warning> comment
    }
}

/**
 * В коробке лежало <warning descr="Sklonenije_NUM_NN">пять карандаша</warning>.
 * А <warning descr="grammar_vse_li_noun">все ли ошибка</warning> найдены?
 * Это случилось <warning descr="INVALID_DATE">31 ноября</warning> 2014 г.
 * За весь вечер она <warning descr="ne_proronila_ni">не проронила и слово</warning>.
 * Собрание состоится в <warning descr="RU_COMPOUNDS">конференц зале</warning>.
 * <warning descr="WORD_REPEAT_RULE">Он он</warning> ошибка.
 */
class ForMultiLanguageSupport {

}

module.exports = ExampleClassWithNoTypos;
