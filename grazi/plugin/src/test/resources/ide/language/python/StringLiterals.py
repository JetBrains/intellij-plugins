oneTypo = "It is <warning descr="ARTICLE_MISSING">friend</warning> of human"
oneSpellcheckTypo = "It is <warning descr="MORFOLOGIK_RULE_EN_US">frend</warning> of human"
fewTypos = "It <warning descr="IT_VBZ">are</warning> working for <warning descr="MUCH_COUNTABLE">much</warning> warnings"
# ignoreTemplate = f"It is {fewTypos} friend"
notIgnoreOtherMistakes = "It is <warning descr="ARTICLE_MISSING">friend</warning>. <warning descr="And">But</warning> I have a {1} here"

oneTypo = 'It is <warning descr="ARTICLE_MISSING">friend</warning> of human'
oneSpellcheckTypo = 'It is <warning descr="MORFOLOGIK_RULE_EN_US">frend</warning> of human'
fewTypos = 'It <warning descr="IT_VBZ">are</warning> working for <warning descr="MUCH_COUNTABLE">much</warning> warnings'
# ignoreTemplate = f'It is {fewTypos} friend' TODO add support of template strings
notIgnoreOtherMistakes = 'It is <warning descr="ARTICLE_MISSING">friend</warning>. <warning descr="And">But</warning> I have a {1} here'

print('It is <warning descr="ARTICLE_MISSING">friend</warning> of human')
print('It is <warning descr="MORFOLOGIK_RULE_EN_US">frend</warning> of human')
print('It <warning descr="IT_VBZ">are</warning> working for <warning descr="MUCH_COUNTABLE">much</warning> warnings')
# print(f'It is {fewTypos} friend')
print('It is <warning descr="ARTICLE_MISSING">friend</warning>. <warning descr="And">But</warning> I have a {1} here')
