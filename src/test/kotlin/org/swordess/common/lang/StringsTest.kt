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

import org.junit.Assert.assertArrayEquals
import org.junit.Test
import kotlin.test.assertEquals

class StringsTest {

    @Test
    fun testWithPrefix() {
        val prefix = "/usr"
        assertEquals("/usr", null.withPrefix(prefix))
        assertEquals("/usr", "".withPrefix(prefix))
        assertEquals("/usr", "/usr".withPrefix(prefix))
        assertEquals("/usr/bin", "/bin".withPrefix(prefix))
    }

    @Test
    fun testWithoutPrefix() {
        val prefix = "/usr"
        assertEquals("", null.withoutPrefix(prefix))
        assertEquals("", "".withoutPrefix(prefix))
        assertEquals("/local", "/usr/local".withoutPrefix(prefix))
        assertEquals("/opt/local", "/opt/local".withoutPrefix(prefix))
    }

    @Test
    fun testWithSuffix() {
        val suffix = "/bin"
        assertEquals("/bin", null.withSuffix(suffix))
        assertEquals("/bin", "".withSuffix(suffix))
        assertEquals("/bin", "/bin".withSuffix(suffix))
        assertEquals("/usr/bin", "/usr".withSuffix(suffix))
    }

    @Test
    fun testWithoutSuffix() {
        val suffix = "/local"
        assertEquals("", null.withoutSuffix(suffix))
        assertEquals("", "".withoutSuffix(suffix))
        assertEquals("/usr", "/usr/local".withoutSuffix(suffix))
        assertEquals("/usr/doc", "/usr/doc".withoutSuffix(suffix))
    }

    @Test
    fun testFilterNotEmpty() {
        assertArrayEquals(arrayOf(), arrayOf<String>().filterNotEmpty())
        assertArrayEquals(arrayOf("a"), arrayOf("a","").filterNotEmpty())
    }

}