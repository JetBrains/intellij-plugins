#![crate_name = "doc"]

/// A group of *members*.
/// This class has no useful logic; it's just a documentation example.
pub struct ExampleClassWithNoTypos {
    /// Name of the group
    name: String,
}

impl ExampleClassWithNoTypos {
    /// Creates an empty group
    ///
    /// # Argument
    ///
    /// * `name` -- the name of the group
    ///
    pub fn new(name: &str) -> Person {
        Person {
            name: name.to_string(),
        }
    }

    /// Adds a [member] to this group.
    ///
    /// # Argument
    ///
    /// * `member` -- member to add
    ///
    /// # Returns
    ///
    /// Int -- the new size of the group.
    ///
    pub fn good_function(member: &str) -> Int {
        return 1 // no error comment
    }
}

/// It is <warning descr="ARTICLE_MISSING">friend</warning>
/// <warning descr="PLURAL_VERB_AFTER_THIS">This guy have</warning> no useful logic; it's just a documentation example.
pub struct ExampleClassWithTypos {
    /// Name of the group
    name: String,
}

impl ExampleClassWithTypos {
    /// Creates an empty group
    ///
    /// # Argument
    ///
    /// * `name` -- the <warning descr="COMMA_WHICH">name which</warning> group
    ///
    pub fn new(name: &str) -> Person {
        Person {
            name: name.to_string(),
        }
    }

    /// It <warning descr="IT_VBZ">add</warning> a [member] to this <warning descr="MORFOLOGIK_RULE_EN_US">grooup</warning>.
    ///
    /// # Argument
    ///
    /// * `member` -- member to add
    ///
    /// # Returns
    ///
    /// Int -- the new size <warning descr="DT_DT">a the</warning> group.
    ///
    pub fn good_function(member: &str) -> Int {
        return 1 // It <warning descr="IT_VBZ">are</warning> <warning descr="MORFOLOGIK_RULE_EN_US">eror</warning> comment
    }
}

/// Er überprüfte die Rechnungen noch <warning descr="MORFOLOGIK_RULE_EN_US">einal</warning>, um ganz <warning descr="COMPOUND_INFINITIV_RULE">sicher zu gehen</warning>.
/// Das ist <warning descr="FUEHR_FUER">führ</warning> Dich!
/// Das <warning descr="MORFOLOGIK_RULE_EN_US">daert</warning> geschätzt fünf <warning descr="MANNSTUNDE">Mannstunden</warning>.

impl ForMultiLanguageSupport {
     /// В коробке лежало <warning descr="Sklonenije_NUM_NN">пять карандаша</warning>.
     /// А <warning descr="grammar_vse_li_noun">все ли ошибка</warning> найдены?
     /// Это случилось <warning descr="INVALID_DATE">31 ноября</warning> 2014 г.
     /// За весь вечер она <warning descr="ne_proronila_ni">не проронила и слово</warning>.
     /// Собрание состоится в <warning descr="RU_COMPOUNDS">конференц зале</warning>.
     /// <warning descr="WORD_REPEAT_RULE">Он он</warning> ошибка.
}
