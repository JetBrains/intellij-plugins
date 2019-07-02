#![crate_name = "doc"]

fn main() {
    let oneTypo = "It is <warning>friend</warning> of human";
    let oneSpellcheckTypo = "It is <warning>frend</warning> of human";
    let fewTypos = "It <warning>are</warning> working for <warning>much</warning> warnings";
    let ignoreTemplate = "It is {} friend";
    let notIgnoreOtherMistakes = "It is <warning>friend</warning>. <warning>But</warning> I have a {} here";

    println!("It is <warning>friend</warning> of human");
    println!("It is <warning>frend</warning> of human");
    println!("It <warning>are</warning> working for <warning>much</warning> warnings");
    println!("It is {} friend", "my");
    println!("It is <warning>friend</warning>. <warning>But</warning> I have a {} here", "friend");
}
