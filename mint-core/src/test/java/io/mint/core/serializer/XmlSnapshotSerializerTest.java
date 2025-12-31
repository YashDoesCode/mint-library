package io.mint.core.serializer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlSnapshotSerializerTest {

    private final XmlSnapshotSerializer serializer = new XmlSnapshotSerializer();

    public static class SampleUser {
        private String name;
        private int age;

        public SampleUser() {
        }

        public SampleUser(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    @Test
    void testSerializeNull() {
        assertEquals("<null/>", serializer.serialize(null));
    }

    @Test
    void testSerializeStringWithSpecialChars() {
        String input = "Tom & Jerry <friends> \"forever\" 'yes'";
        String expected = "<value>Tom &amp; Jerry &lt;friends&gt; &quot;forever&quot; &apos;yes&apos;</value>";
        assertEquals(expected, serializer.serialize(input));
    }

    @Test
    void testSerializeSimplePojo() {
        SampleUser user = new SampleUser("Alice", 30);
        String xml = serializer.serialize(user);
        assertTrue(xml.contains("<java") || xml.contains("<object"), "Expected XML structure but was: " + xml);
        assertTrue(xml.contains("Alice"));
    }

    @Test
    void testDeterministicOutputAcrossCalls() {
        SampleUser user1 = new SampleUser("Bob", 25);
        SampleUser user2 = new SampleUser("Bob", 25);
        String xml1 = serializer.serialize(user1);
        String xml2 = serializer.serialize(user2);
        assertEquals(xml1, xml2);
    }

    @Test
    void testFormatName() {
        assertEquals("xml", serializer.formatName());
    }
}
