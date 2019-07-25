#![crate_name = "doc"]

fn main() {
    let oneTypo = "It is <warning descr="ARTICLE_MISSING">friend</warning> of human";
    let oneSpellcheckTypo = "It is <warning descr="MORFOLOGIK_RULE_EN_US">frend</warning> of human";
    let fewTypos = "It <warning descr="IT_VBZ">are</warning> working for <warning descr="MUCH_COUNTABLE">much</warning> warnings";
    let ignoreTemplate = "It is {} friend";
    let notIgnoreOtherMistakes = "It is <warning descr="ARTICLE_MISSING">friend</warning>. <warning descr="And">But</warning> I have a {} here";

    println!("It is <warning descr="ARTICLE_MISSING">friend</warning> of human");
    println!("It is <warning descr="MORFOLOGIK_RULE_EN_US">frend</warning> of human");
    println!("It <warning descr="IT_VBZ">are</warning> working for <warning descr="MUCH_COUNTABLE">much</warning> warnings");
    println!("It is {} friend", "my");
    println!("It is <warning descr="ARTICLE_MISSING">friend</warning>. <warning descr="And">But</warning> I have a {} here", "friend");
}
