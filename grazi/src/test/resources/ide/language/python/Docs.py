"""Module description <warning>eror</warning>"""


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
    """It is <warning>friend</warning>

    <warning>This guy have</warning> no useful logic; it's just a documentation example.

    Args:
        name (str): the <warning>name which</warning> group

    Attributes:
        name (str): the <warning>name which</warning> group

    """

    def __init__(self, name):
        self.name = name

    def bad_function(self, member):
        """
        It <warning>add</warning> a [member] to this <warning>grooup</warning>.

        Args:
            member (str): member to add to the group.

        Returns:
            int: the new size of <warning>a the</warning> group.

        """
        return 1  # <warning>eror</warning> comment
