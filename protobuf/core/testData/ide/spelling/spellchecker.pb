# Typo inspection doesn't apply to the schema comments below
#
# proto-file: "ide/spelling/poorly_spelld.proto"
# proto-message: PoorlySpelld
#
# But it applies here: <TYPO descr="Typo: In word 'gdsgj'">gdsgj</TYPO>
# this line has <TYPO descr="Typo: In word 'badd'">badd</TYPO> <TYPO descr="Typo: In word 'spellng'">spellng</TYPO>

string: "Good spelling"
string: "<TYPO descr="Typo: In word 'badd'">badd</TYPO> <TYPO descr="Typo: In word 'spellng'">spellng</TYPO> here"
string: "<TYPO descr="Typo: In word 'badd'">badd</TYPO>\n<TYPO descr="Typo: In word 'spellng'">spellng</TYPO>\x20<TYPO descr="Typo: In word 'herre'">herre</TYPO>"

# There are typos in some of the identifiers below, but they're all usages and not declarations.
# Typos are only highlighted in declarations, which are all in .proto files.

foo: HELLO
foo: ADFDS

type {
  good_spelling: ""
  bad_spellng: ""
}
