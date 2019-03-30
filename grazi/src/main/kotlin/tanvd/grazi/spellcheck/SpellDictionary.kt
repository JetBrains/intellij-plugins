package tanvd.grazi.spellcheck

import java.io.File

class SpellDictionary(val file: File, words: List<String> = emptyList()) {
    private val _words = ArrayList(words)
    val words: List<String> = _words


    companion object {
        var graziFolder: File? = null

        fun usersCustom(): SpellDictionary {
            if (graziFolder == null) return SpellDictionary(File(""))
            return load(File(graziFolder!!, "custom_dict.txt"))
        }

        private fun load(file: File): SpellDictionary {
            file.parentFile.mkdirs()
            if (!file.exists()) {
                file.createNewFile()
            }
            return SpellDictionary(file, file.readLines())
        }
    }

    fun add(word: String) {
        _words.add(word)
        serialize()
    }

    private fun serialize() {
        file.parentFile.mkdirs()
        if (!file.exists()) file.createNewFile()
        file.writeText(words.joinToString(separator = "\n"))
    }
}
