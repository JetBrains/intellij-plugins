package tanvd.grazi

import com.intellij.openapi.application.ApplicationManager
import org.jetbrains.org.objectweb.asm.*
import org.jetbrains.org.objectweb.asm.Opcodes.ASM5


class EnglishChonker(cv: ClassVisitor) : ClassVisitor(ASM5, cv) {
    companion object {
        fun reloadEnglish() {
            val stream = GraziPlugin::class.java.getResourceAsStream("/org/languagetool/language/English.class")
            val reader = ClassReader(stream)
            val writer = ClassWriter(ClassWriter.COMPUTE_MAXS)
            val visitor = EnglishChonker(writer)
            reader.accept(visitor, 0)

            val cls = writer.toByteArray()
            with(ClassLoader::class.java.getDeclaredMethod("defineClass", String::class.java, ByteArray::class.java, Int::class.java, Int::class.java)) {
                isAccessible = true

                if (ApplicationManager.getApplication().isUnitTestMode) {
                    invoke(ClassLoader.getSystemClassLoader(), "org.languagetool.language.English", cls, 0, cls.size)
                } else {
                    invoke(GraziPlugin.classLoader, "org.languagetool.language.English", cls, 0, cls.size)
                }
            }
        }
    }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val mv = cv.visitMethod(access, name, descriptor, signature, exceptions)
        return if (name.equals("getChunker")) { GetChunkerMethodVisitor(mv) } else { mv }
    }

    class GetChunkerMethodVisitor(private val visitor: MethodVisitor) : MethodVisitor(ASM5, null) {
        override fun visitCode() {
            visitor.visitCode()
            visitor.visitInsn(Opcodes.ACONST_NULL)
            visitor.visitInsn(Opcodes.ARETURN)
            visitor.visitMaxs(0, 0)
            visitor.visitEnd()
        }
    }
}
