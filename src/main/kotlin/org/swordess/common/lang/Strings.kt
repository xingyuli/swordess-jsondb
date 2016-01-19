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

package org.swordess.common.lang

import java.io.File
import java.io.InputStream
import java.net.URL
import java.net.URLDecoder

fun String?.withPrefix(prefix: String) =
        if (this == null || isEmpty()) prefix else if (startsWith(prefix)) this else prefix + this

fun String?.withoutPrefix(prefix: String) = if (this == null) "" else removePrefix(prefix)

fun String?.withSuffix(suffix: String) =
        if (this == null || isEmpty()) suffix else if (endsWith(suffix)) this else this + suffix

fun String?.withoutSuffix(suffix: String) = if (this == null) "" else removeSuffix(suffix)

fun String.resourceNameAsStream(): InputStream {
    val file = File(this)
    if (file.exists()) {
        return file.inputStream()
    }

    var input: InputStream? = javaClass.getResourceAsStream(this)
    if (input == null) {
        input = javaClass.classLoader?.getResourceAsStream(this)
    }
    if (input == null) {
        input = Thread.currentThread().contextClassLoader.getResourceAsStream(this)
    }

    return input ?: throw RuntimeException("resource not found: $this")
}

fun String.resourceNameAsURL(): URL {
    val file = File(this)
    if (file.exists()) {
        return file.toURI().toURL()
    }

    var url: URL? = javaClass.getResource(this)
    if (url == null) {
        url = javaClass.classLoader?.getResource(this)
    }
    if (url == null) {
        url = Thread.currentThread().contextClassLoader.getResource(this)
    }

    return url ?: throw RuntimeException("resource not found: $this")
}

fun String.resourceNameAsFilename(): String {
    if (File(this).exists()) {
        return this
    }

    val url = resourceNameAsURL()
    return URLDecoder.decode(url.file, "UTF-8")
}

fun Array<String>.filterNotEmpty() = if (isEmpty()) this else filter { it.isNotEmpty() }.toTypedArray()
