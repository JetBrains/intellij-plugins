package tanvd.grazi.utils

fun ClassLoader.defineClass(name: String, data: ByteArray, offset: Int, size: Int) : Class<*> =
        with(ClassLoader::class.java.getDeclaredMethod("defineClass", String::class.java, ByteArray::class.java, Int::class.java, Int::class.java)) {
            isAccessible = true
            invoke(this, name, data, offset, size) as Class<*>
        }

@Suppress("UNCHECKED_CAST")
fun <Type> Class<*>.getStaticField(name: String): Type = this.getDeclaredField(name).apply { isAccessible = true }.get(null) as Type
