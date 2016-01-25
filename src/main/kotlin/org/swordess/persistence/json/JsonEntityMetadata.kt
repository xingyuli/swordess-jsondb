/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Vic Lau
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.swordess.persistence.json

import org.swordess.persistence.EntityMetadata
import org.swordess.persistence.Id
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.memberProperties

class JsonEntityMetadata(override val belongingClass: KClass<*>) : EntityMetadata(belongingClass) {

    val filename = determineFilename(belongingClass)
    val idProperty = determineIdProperty(belongingClass)

    private fun determineFilename(belongingClass: KClass<*>): String =
            belongingClass.java.getAnnotation(JsonEntity::class.java).filename

    private fun determineIdProperty(belongingClass: KClass<*>): KMutableProperty<*> {
        val idProperty = belongingClass.memberProperties
                .firstOrNull { it.getter.javaMethod?.isAnnotationPresent(Id::class.java) ?: false }
        idProperty ?: throw RuntimeException("Annotation of type ${Id::class.java.name} should present at least once in ${belongingClass.java.name}")

        return if (idProperty is KMutableProperty<*>) {
            idProperty
        } else {
            throw RuntimeException("${idProperty.name} must be mutable in ${belongingClass.java.name}")
        }
    }

    override fun toString() = "{\n\tfilename = $filename,\n\tidProperty = ${idProperty.name}\n}"

}
