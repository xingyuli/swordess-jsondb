package org.swordess.persistence.json

import org.junit.Test
import org.swordess.persistence.Id
import org.swordess.persistence.json.java.test.brokenmodel.User
import org.swordess.persistence.json.test.brokenmodel.Foo
import kotlin.test.assertEquals
import kotlin.test.fail

class JsonEntityMetadataTest {

    @Test
    fun testExceptionShouldBeThrownIfIdAnnotationAbsent() {
        try {
            JsonEntityMetadata(Foo::class)
            fail("exception expected")
        } catch (e: RuntimeException) {
            assertEquals("Annotation of type ${Id::class.java.name} should present at least once in ${Foo::class.java.name}", e.message)
        }
    }

    @Test
    fun testExceptionShouldBeThrownIfSetterForIdIsMissing() {
        try {
            JsonEntityMetadata(User::class)
            fail("exception expected")
        } catch (e: RuntimeException) {
            assertEquals("no setter was found for property id, please add a setter", e.message)
        }
    }

}