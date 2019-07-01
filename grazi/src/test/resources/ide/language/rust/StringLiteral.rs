#![crate_name = "doc"]

pub struct Person {
    name: String,
}

impl Person {
    pub fn new(name: &str) -> Person {
        Person {
            name: name.to_string(),
        }
    }

    pub fn hello(& self) {
        println!("Hello, {}!", self.name);
    }
}

fn main() {
    let john = Person::new("John");

    let oneTypo = "It is <warning>friend</warning> of human";
    let oneSpellcheckTypo = "It is <warning>frend</warning> of human";
    let fewTypos = "It <warning>are</warning> working for <warning>much</warning> warnings";
    let ignoreTemplate = "It is {} friend";
    let notIgnoreOtherMistakes = "It is <warning>friend</warning>. <warning>But</warning> I have a {} here";

    println!("It is <warning>friend</warning> of human");
    println!("It is <warning>frend</warning> of human");
    println!("It <warning>are</warning> working for <warning>much</warning> warnings");
    println!("It is {} friend", john.name);
    println!("It is <warning>friend</warning>. <warning>But</warning> I have a {} here", john.name);

    john.hello();
}
