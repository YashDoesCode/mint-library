package io.mint.core.serializer;

import io.mint.core.SnapshotException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    public record SampleRecord(String title, int count) {}

    public static class CircularNode {
        private String name;
        private CircularNode next;

        public CircularNode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public CircularNode getNext() {
            return next;
        }

        public void setNext(CircularNode next) {
            this.next = next;
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
        assertTrue(xml.contains("<object"), "Expected XML structure but was: " + xml);
        assertTrue(xml.contains("Alice"));
        assertTrue(xml.contains("30"));
        assertFalse(xml.contains("java version"), "Must not leak JVM version into snapshot: " + xml);
        assertFalse(xml.contains("java.beans"), "Must not depend on java.beans decoder: " + xml);
    }

    @Test
    void testSerializeRecord() {
        SampleRecord record = new SampleRecord("Report", 42);
        String xml = serializer.serialize(record);
        assertTrue(xml.contains("<object"), "Expected XML object root but was: " + xml);
        assertTrue(xml.contains("Report"));
        assertTrue(xml.contains("42"));
        assertFalse(xml.contains("java version"));
    }

    @Test
    void testSerializeCollectionsAndMaps() {
        List<String> list = List.of("alpha", "beta");
        String listXml = serializer.serialize(list);
        assertTrue(listXml.contains("<list>"));
        assertTrue(listXml.contains("<item>alpha</item>"));
        assertTrue(listXml.contains("<item>beta</item>"));

        Map<String, Object> map = Map.of("b", "second", "a", "first");
        String mapXml = serializer.serialize(map);
        assertTrue(mapXml.contains("<map>"));
        int idxA = mapXml.indexOf("key=\"a\"");
        int idxB = mapXml.indexOf("key=\"b\"");
        assertTrue(idxA < idxB, "Map keys must be sorted deterministically in output: " + mapXml);
    }

    @Test
    void testSerializeValidXmlStringPrettyPrinted() {
        String rawXml = "<root><child id=\"1\">test</child></root>";
        String formatted = serializer.serialize(rawXml);
        assertEquals("<root>\n  <child id=\"1\">test</child>\n</root>", formatted);
    }

    @Test
    void testCircularReferenceDetection() {
        CircularNode node1 = new CircularNode("node1");
        CircularNode node2 = new CircularNode("node2");
        node1.setNext(node2);
        node2.setNext(node1);

        assertThrows(SnapshotException.class, () -> serializer.serialize(node1));
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
