# coding=utf-8
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


class ForMultiLanguageSupport:
    """
    В коробке лежало <warning descr="Sklonenije_NUM_NN">пять карандаша</warning>.
    А <warning descr="grammar_vse_li_noun">все ли ошибка</warning> найдены?
    Это случилось <warning descr="INVALID_DATE">31 ноября</warning> 2014 г.
    За весь вечер она <warning descr="ne_proronila_ni">не проронила и слово</warning>.
    Собрание состоится в <warning descr="RU_COMPOUNDS">конференц зале</warning>.
    <warning descr="WORD_REPEAT_RULE">Он он</warning> ошибка.
    """

    def __init__(self):
        """
        Er überprüfte die Rechnungen noch <warning descr="MORFOLOGIK_RULE_EN_US">einal</warning>, um ganz <warning descr="COMPOUND_INFINITIV_RULE">sicher zu gehen</warning>.
        Das ist <warning descr="FUEHR_FUER">führ</warning> Dich!
        Das <warning descr="MORFOLOGIK_RULE_EN_US">daert</warning> geschätzt fünf <warning descr="MANNSTUNDE">Mannstunden</warning>.
        """
        pass
