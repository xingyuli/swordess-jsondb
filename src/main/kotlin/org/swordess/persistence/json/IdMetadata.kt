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

import org.apache.commons.lang3.StringUtils
import java.lang.reflect.Method

// TODO make use of kotlin-reflection api
class IdMetadata(val getter: Method) {

    val propertyName: String
    val setter: Method

    init {
        propertyName = determinePropertyName(getter)
        setter = determineSetter()
    }

    private fun determinePropertyName(getter: Method) =
            if (getter.name.startsWith("get")) {
                StringUtils.uncapitalize(getter.name.substring(3))
            } else if (getter.name.startsWith("is")) {
                StringUtils.uncapitalize(getter.name.substring(2))
            } else {
                throw RuntimeException("unable to determine property name for $getter")
            }

    private fun determineSetter() =
            try {
                getter.declaringClass.getDeclaredMethod("set" + StringUtils.capitalize(propertyName),
                        getter.returnType)
            } catch (e: NoSuchMethodException) {
                throw RuntimeException("no setter was found for property $propertyName, please add a setter")
            }

    override fun toString() = "{ propertyName=$propertyName }"

}