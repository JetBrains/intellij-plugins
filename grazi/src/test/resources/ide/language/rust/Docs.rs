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

/// It is <warning>friend</warning>
/// <warning>This guy have</warning> no useful logic; it's just a documentation example.
pub struct ExampleClassWithTypos {
    /// Name of the group
    name: String,
}

impl ExampleClassWithTypos {
    /// Creates an empty group
    ///
    /// # Argument
    ///
    /// * `name` -- the <warning>name which</warning> group
    ///
    pub fn new(name: &str) -> Person {
        Person {
            name: name.to_string(),
        }
    }

    /// It <warning>add</warning> a [member] to this <warning>grooup</warning>.
    ///
    /// # Argument
    ///
    /// * `member` -- member to add
    ///
    /// # Returns
    ///
    /// Int -- the new size <warning>a the</warning> group.
    ///
    pub fn good_function(member: &str) -> Int {
        return 1 // <warning>eror</warning> comment
    }
}
