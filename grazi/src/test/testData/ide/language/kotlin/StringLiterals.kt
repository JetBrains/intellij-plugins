@file:Suppress("unused", "MayBeConstant", "UNUSED_PARAMETER", "SpellCheckingInspection")

package ide.language.kotlin

object OneLine {
    val oneTypo = "It is <warning descr="ARTICLE_MISSING">friend</warning> of human"
    val oneSpellcheckTypo = "It is <warning descr="MORFOLOGIK_RULE_EN_US">frend</warning> of human"
    val fewTypos = "It <warning descr="IT_VBZ">are</warning> working for <warning descr="MUCH_COUNTABLE">much</warning> warnings"
    val ignoreTemplate = "It is ${1} friend"
    val notIgnoreOtherMistakes = "It is <warning descr="ARTICLE_MISSING">friend</warning>. <warning descr="And">But</warning> I have a ${1} here"
}

object MultiLine {
    val oneTypo = """It is <warning descr="ARTICLE_MISSING">friend</warning> of human"""
    val oneSpellcheckTypo = """It is <warning descr="MORFOLOGIK_RULE_EN_US">frend</warning> of human"""
    val fewTypos = """It <warning descr="IT_VBZ">are</warning> working for <warning descr="MUCH_COUNTABLE">much</warning> warnings"""
    val ignoreTemplate = """It is ${1} friend"""
    val notIgnoreOtherMistakes = """It is <warning descr="ARTICLE_MISSING">friend</warning>. <warning descr="And">But</warning> I have a ${1} here"""
}

object InFunc {
    fun a(b: String) {
        a("It is <warning descr="ARTICLE_MISSING">friend</warning> of human")
        a("It is <warning descr="MORFOLOGIK_RULE_EN_US">frend</warning> of human")
        a("It <warning descr="IT_VBZ">are</warning> working for <warning descr="MUCH_COUNTABLE">much</warning> warnings")
        a("It is ${1} friend")
        a("It is <warning descr="ARTICLE_MISSING">friend</warning>. <warning descr="And">But</warning> I have a ${1} here")
    }
}


