fn main() {
    let variableWith<warning descr="MORFOLOGIK_RULE_EN_US">Eror</warning> = "error";
}

pub struct ClassWith<warning descr="MORFOLOGIK_RULE_EN_US">Eror</warning> {
    <warning descr="MORFOLOGIK_RULE_EN_US">eror</warning>: String,
}

impl ClassWithEror {
    pub fn new(<warning descr="MORFOLOGIK_RULE_EN_US">eror</warning>: &str) -> ClassWithEror {
        ClassWithEror {
            eror: eror.to_string(),
        }
    }

    pub fn <warning descr="MORFOLOGIK_RULE_EN_US">eror</warning>Function(& self) { }
}
