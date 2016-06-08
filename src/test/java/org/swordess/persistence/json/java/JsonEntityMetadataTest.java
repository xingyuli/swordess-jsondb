package org.swordess.persistence.json.java;

import kotlin.jvm.JvmClassMappingKt;
import org.junit.Test;
import org.swordess.persistence.Id;
import org.swordess.persistence.json.JsonEntityMetadata;
import org.swordess.persistence.json.java.test.brokenmodel.Foo;
import org.swordess.persistence.json.java.test.brokenmodel.Group;
import org.swordess.persistence.json.java.test.brokenmodel.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class JsonEntityMetadataTest {

    @Test
    public void testExceptionShouldBeThrownIfIdAnnotationAbsent() {
        try {
            new JsonEntityMetadata(JvmClassMappingKt.getKotlinClass(Foo.class));
            fail("exception expected");
        } catch (RuntimeException e) {
            assertEquals("Annotation of type " + Id.class.getName() + " should present at least once in " + Foo.class.getName(), e.getMessage());
        }
    }

    @Test
    public void testExceptionShouldBeThrownIfSetterForIdIsMissing() {
        try {
            new JsonEntityMetadata(JvmClassMappingKt.getKotlinClass(User.class));
            fail("exception expected");
        } catch (RuntimeException e) {
            assertEquals("no setter was found for property id, please add a setter", e.getMessage());
        }
    }

    @Test
    public void testExceptionShouldBeThrownIfGetterForIdIsNeitherPrefixedWithGetNorIs() {
        try {
            new JsonEntityMetadata(JvmClassMappingKt.getKotlinClass(Group.class));
            fail("exception expected");
        } catch (RuntimeException e) {
            assertEquals(
                    "unable to determine property name for public java.lang.Long org.swordess.persistence.json.java.test.brokenmodel.Group.fetchId()",
                    e.getMessage()
            );
        }
    }

}
