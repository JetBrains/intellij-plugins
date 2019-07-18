"""Module description <warning descr="MORFOLOGIK_RULE_EN_US">eror</warning>"""


class ExampleClassWithNoTypos:
    """A group of *members*.

    This class has no useful logic; it's just a documentation example.

    Args:
        name (str): the name of this group.

    Attributes:
        name (str): the name of this group.

    """

    def __init__(self, name):
        self.name = name

    def good_function(self, member):
        """
        Adds a [member] to this group.

        Args:
            member (str): member to add to the group.

        Returns:
            int: the new size of the group.

        """
        return 1  # no error comment


class ExampleClassWithTypos:
    """It is <warning descr="ARTICLE_MISSING">friend</warning>

    <warning descr="PLURAL_VERB_AFTER_THIS">This guy have</warning> no useful logic; it's just a documentation example.

    Args:
        name (str): the <warning descr="COMMA_WHICH">name which</warning> group

    Attributes:
        name (str): the <warning descr="COMMA_WHICH">name which</warning> group

    """

    def __init__(self, name):
        self.name = name

    def bad_function(self, member):
        """
        It <warning descr="IT_VBZ">add</warning> a [member] to this <warning descr="MORFOLOGIK_RULE_EN_US">grooup</warning>.

        Args:
            member (str): member to add to the group.

        Returns:
            int: the new size of <warning descr="DT_DT">a the</warning> group.

        """
        return 1  # It <warning descr="IT_VBZ">are</warning> <warning descr="MORFOLOGIK_RULE_EN_US">eror</warning> comment
