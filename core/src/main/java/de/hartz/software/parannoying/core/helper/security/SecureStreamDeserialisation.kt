package de.hartz.software.parannoying.core.helper.security

import java.io.*

object SecureStreamDeserialisation {

    // https://www.contrastsecurity.com/security-influencers/protect-your-apps-from-java-serialization-vulnerability
    /**
     * A method to replace the unsafe ObjectInputStream.readObject() method built into Java. This method
     * checks to be sure the classes referenced are safe, the number of objects is limited to something sane,
     * and the number of bytes is limited to a reasonable number. The returned Object is also cast to the
     * specified type.
     *
     * @param type Class representing the object type expected to be returned
     * @param safeClasses List of Classes allowed in serialized object being read
     * @param maxObjects long representing the maximum number of objects allowed inside the serialized object being read
     * @param maxBytes long representing the maximum number of bytes allowed to be read from the InputStream
     * @param in InputStream containing an untrusted serialized object
     * @return Object read from the stream (cast to the Class of the type parameter)
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Throws(IOException::class, ClassNotFoundException::class)
    fun <T> safeReadObject(type: Class<*>, safeClasses: List<Class<*>?>, `in`: InputStream?, maxObjects: Long = 10, maxBytes: Long= -1): T? {
        // create an input stream limited to a certain number of bytes
        val lis: InputStream = object : FilterInputStream(`in`) {
            private var len: Long = 0

            @Throws(IOException::class)
            override fun read(): Int {
                val `val`: Int = super.read()
                if (`val` != -1) {
                    len++
                    checkLength()
                }
                return `val`
            }

            @Throws(IOException::class)
            override fun read(b: ByteArray?, off: Int, len: Int): Int {
                var len = len
                val `val`: Int = super.read(b, off, len)
                if (`val` > 0) {
                    len += `val`
                    checkLength()
                }
                return `val`
            }

            @Throws(IOException::class)
            private fun checkLength() {
                if (maxBytes != -1L && len > maxBytes) {
                    throw SecurityException("Security violation: attempt to deserialize too many bytes from stream. Limit is $maxBytes")
                }
            }
        }
        // create an object input stream that checks classes and limits the number of objects to read
        val ois: ObjectInputStream = object : ObjectInputStream(lis) {
            private var objCount = 0
            var b: Boolean = enableResolveObject(true)
            @Throws(IOException::class)
            override fun resolveObject(obj: Any?): Any? {
                if (objCount++ > maxObjects) throw SecurityException("Security violation: attempt to deserialize too many objects from stream. Limit is $maxObjects")
                return super.resolveObject(obj)
            }

            @Throws(IOException::class, ClassNotFoundException::class)
            override fun resolveClass(osc: ObjectStreamClass?): Class<*>? {
                val clazz: Class<*> = super.resolveClass(osc)
                if (clazz.isArray || clazz == type || clazz == String::class.java ||
                        Number::class.java.isAssignableFrom(clazz) ||
                        safeClasses.contains(clazz)) return clazz
                throw SecurityException("Security violation: attempt to deserialize unauthorized $clazz")
            }
        }
        // use the protected ObjectInputStream to read object safely and cast to T
        return ois.readObject() as T
    }
}
