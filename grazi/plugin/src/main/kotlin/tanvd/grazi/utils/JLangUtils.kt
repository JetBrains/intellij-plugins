package tanvd.grazi.utils

import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.strong
import org.jetbrains.org.objectweb.asm.*
import org.languagetool.Language
import org.languagetool.Languages
import org.languagetool.rules.ExampleSentence
import org.languagetool.rules.IncorrectExample
import org.languagetool.rules.Rule
import org.languagetool.rules.RuleMatch
import tanvd.grazi.GraziConfig
import tanvd.grazi.GraziPlugin
import tanvd.grazi.grammar.Typo
import tanvd.grazi.language.Lang
import java.io.File
import java.io.InputStream

fun Iterable<Typo>.spellcheckOnly(): Set<Typo> = filter { it.isSpellingTypo }.toSet()
val Typo.isSpellingTypo: Boolean
    get() = info.rule.isDictionaryBasedSpellingRule

val RuleMatch.typoCategory: Typo.Category
    get() = Typo.Category[rule.category.id.toString()]

val ExampleSentence.text: CharSequence
    get() = example

fun Rule.toDescriptionSanitized() = this.description.replace("**", "")

private fun FlowOrPhrasingContent.toHtml(example: IncorrectExample, mistakeHandler: FlowOrPhrasingContent.(String) -> Unit) {
    Regex("(.*?)<marker>(.*?)</marker>|(.*)").findAll(example.example).forEach {
        val (prefix, mistake, suffix) = it.destructured

        +prefix
        mistakeHandler(mistake)
        +suffix
    }
}

fun FlowOrPhrasingContent.toIncorrectHtml(example: IncorrectExample) {
    toHtml(example) { mistake ->
        if (mistake.isNotEmpty()) {
            strong {
                +mistake.trim()
            }
        }
    }
}

fun FlowOrPhrasingContent.toCorrectHtml(example: IncorrectExample) {
    toHtml(example) { mistake ->
        if (mistake.isNotEmpty() && example.corrections.isNotEmpty()) {
            strong {
                +example.corrections.first().trim()
            }
        }
    }
}

object Resources {
    fun getFile(file: String): File = File(GraziPlugin::class.java.getResource(file).file)
    fun getStream(file: String): InputStream = GraziPlugin::class.java.getResourceAsStream(file)
}

object LangToolInstrumentation {
    fun registerLanguage(lang: Lang) {
        if (lang in GraziConfig.get().enabledLanguagesAvailable) return

        val dynLanguages = Languages::class.java.getDeclaredField("dynLanguages")
        dynLanguages.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val langs = dynLanguages.get(null) as MutableList<Language>

        lang.descriptor.langsClasses.forEach { className ->
            val qualifiedName = "org.languagetool.language.$className"
            if (langs.all { it::class.java.canonicalName != qualifiedName }) {
                langs.add(GraziPlugin.loadClass(qualifiedName)!!.newInstance() as Language)
            }
        }
    }

    fun reloadEnglish() {
        val reader = ClassReader(Resources.getStream("/org/languagetool/language/English.class"))
        val writer = ClassWriter(ClassWriter.COMPUTE_MAXS)
        reader.accept(EnglishChunkerInstrumentation(writer), 0)

        val cls = writer.toByteArray()
        with(ClassLoader::class.java.getDeclaredMethod("defineClass", String::class.java, ByteArray::class.java, Int::class.java, Int::class.java)) {
            isAccessible = true
            invoke(GraziPlugin.classLoader, "org.languagetool.language.English", cls, 0, cls.size)
        }
    }

    private class EnglishChunkerInstrumentation(cv: ClassVisitor) : ClassVisitor(Opcodes.ASM5, cv) {
        override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
            val mv = cv.visitMethod(access, name, descriptor, signature, exceptions)
            return if (name == "getChunker") GetChunkerMethodVisitor(mv) else mv
        }

        class GetChunkerMethodVisitor(private val visitor: MethodVisitor) : MethodVisitor(Opcodes.ASM5, null) {
            override fun visitCode() {
                with(visitor) {
                    visitCode()
                    visitInsn(Opcodes.ACONST_NULL)
                    visitInsn(Opcodes.ARETURN)
                    visitMaxs(0, 0)
                    visitEnd()
                }
            }
        }
    }
}
