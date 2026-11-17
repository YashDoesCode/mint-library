package io.mint.core.serializer;

import io.mint.core.SnapshotException;
import io.mint.core.SnapshotSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Snapshot serializer producing deterministic XML representations using standard JDK {@code java.xml} APIs.
 *
 * <p>This serializer operates without dependencies on {@code java.desktop} or {@code java.beans},
 * providing cross-JVM stability and preventing version leaks into snapshot baselines.</p>
 */
public final class XmlSnapshotSerializer implements SnapshotSerializer {

    /**
     * Constructs a new {@code XmlSnapshotSerializer}.
     */
    public XmlSnapshotSerializer() {
    }

    /**
     * Serializes an object to a deterministic XML snapshot representation.
     *
     * @param value the object to serialize, may be null
     * @return the formatted XML snapshot string, never null
     * @throws SnapshotException if XML processing or object inspection fails
     */
    @Override
    public String serialize(Object value) throws SnapshotException {
        if (value == null) {
            return "<null/>";
        }
        if (value instanceof Document document) {
            return formatDocument(document);
        }
        if (value instanceof Node node) {
            return formatNode(node);
        }
        if (value instanceof CharSequence charSequence) {
            String text = charSequence.toString().trim();
            if (text.startsWith("<") && text.endsWith(">")) {
                try {
                    DocumentBuilderFactory dbf = createDocumentBuilderFactory();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document doc = db.parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
                    return formatDocument(doc);
                } catch (Exception ignored) {
                    // Fall back to escaped string wrapper when text is not a well-formed XML document
                }
            }
            return "<value>" + escapeXml(charSequence.toString()) + "</value>";
        }

        try {
            DocumentBuilderFactory dbf = createDocumentBuilderFactory();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            Element root = buildElement(doc, value, visited);
            doc.appendChild(root);
            return formatDocument(doc);
        } catch (SnapshotException e) {
            throw e;
        } catch (Exception e) {
            throw new SnapshotException("Failed to serialize object to XML: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the format identifier for XML serialization.
     *
     * @return {@code "xml"}
     */
    @Override
    public String formatName() {
        return "xml";
    }

    private static Element buildElement(Document doc, Object value, Set<Object> visited) throws SnapshotException {
        if (value == null) {
            return doc.createElement("null");
        }
        if (isSimple(value)) {
            Element elem = doc.createElement("value");
            elem.setTextContent(formatSimpleValue(value));
            return elem;
        }
        if (value.getClass().isArray()) {
            Element listElem = doc.createElement("list");
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Element itemElem = doc.createElement("item");
                attachPropertyValue(doc, itemElem, Array.get(value, i), visited);
                listElem.appendChild(itemElem);
            }
            return listElem;
        }
        if (value instanceof Iterable<?> iterable) {
            Element listElem = doc.createElement("list");
            for (Object item : iterable) {
                Element itemElem = doc.createElement("item");
                attachPropertyValue(doc, itemElem, item, visited);
                listElem.appendChild(itemElem);
            }
            return listElem;
        }
        if (value instanceof Map<?, ?> map) {
            Element mapElem = doc.createElement("map");
            Map<String, Object> sorted = new TreeMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                sorted.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            for (Map.Entry<String, Object> entry : sorted.entrySet()) {
                Element entryElem = doc.createElement("entry");
                entryElem.setAttribute("key", entry.getKey());
                attachPropertyValue(doc, entryElem, entry.getValue(), visited);
                mapElem.appendChild(entryElem);
            }
            return mapElem;
        }

        if (!visited.add(value)) {
            throw new SnapshotException("Circular reference detected in object graph for: " + value.getClass().getName());
        }
        try {
            Element objElem = doc.createElement("object");
            objElem.setAttribute("class", value.getClass().getName());

            if (value.getClass().isRecord()) {
                RecordComponent[] components = value.getClass().getRecordComponents();
                Arrays.sort(components, Comparator.comparing(RecordComponent::getName));
                for (RecordComponent rc : components) {
                    Element propElem = doc.createElement("property");
                    propElem.setAttribute("name", rc.getName());
                    try {
                        Object propVal = rc.getAccessor().invoke(value);
                        attachPropertyValue(doc, propElem, propVal, visited);
                    } catch (SnapshotException e) {
                        throw e;
                    } catch (Exception e) {
                        propElem.setTextContent("[unreadable: " + e.getMessage() + "]");
                    }
                    objElem.appendChild(propElem);
                }
            } else {
                Map<String, Method> getters = findGetters(value.getClass());
                if (!getters.isEmpty()) {
                    for (Map.Entry<String, Method> entry : getters.entrySet()) {
                        Element propElem = doc.createElement("property");
                        propElem.setAttribute("name", entry.getKey());
                        try {
                            Object propVal = entry.getValue().invoke(value);
                            attachPropertyValue(doc, propElem, propVal, visited);
                        } catch (SnapshotException e) {
                            throw e;
                        } catch (Exception e) {
                            propElem.setTextContent("[unreadable: " + e.getMessage() + "]");
                        }
                        objElem.appendChild(propElem);
                    }
                } else {
                    Map<String, Field> fields = findFields(value.getClass());
                    for (Map.Entry<String, Field> entry : fields.entrySet()) {
                        Element propElem = doc.createElement("property");
                        propElem.setAttribute("name", entry.getKey());
                        try {
                            Field field = entry.getValue();
                            field.trySetAccessible();
                            Object propVal = field.get(value);
                            attachPropertyValue(doc, propElem, propVal, visited);
                        } catch (SnapshotException e) {
                            throw e;
                        } catch (Exception e) {
                            propElem.setTextContent("[unreadable: " + e.getMessage() + "]");
                        }
                        objElem.appendChild(propElem);
                    }
                }
            }
            return objElem;
        } finally {
            visited.remove(value);
        }
    }

    private static void attachPropertyValue(Document doc, Element parent, Object value, Set<Object> visited) throws SnapshotException {
        if (value == null) {
            parent.appendChild(doc.createElement("null"));
        } else if (isSimple(value)) {
            parent.setTextContent(formatSimpleValue(value));
        } else {
            parent.appendChild(buildElement(doc, value, visited));
        }
    }

    private static boolean isSimple(Object value) {
        return value instanceof Number
                || value instanceof Boolean
                || value instanceof Character
                || value instanceof CharSequence
                || value instanceof Enum<?>
                || value instanceof UUID
                || value instanceof Temporal
                || value instanceof Date
                || value instanceof URI
                || value instanceof URL;
    }

    private static String formatSimpleValue(Object value) {
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        return String.valueOf(value);
    }

    private static Map<String, Method> findGetters(Class<?> clazz) {
        Map<String, Method> getters = new TreeMap<>();
        for (Method method : clazz.getMethods()) {
            if (method.getParameterCount() != 0 || Modifier.isStatic(method.getModifiers()) || method.getDeclaringClass() == Object.class) {
                continue;
            }
            String name = method.getName();
            if (name.startsWith("get") && name.length() > 3 && method.getReturnType() != void.class) {
                String propertyName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
                getters.put(propertyName, method);
            } else if (name.startsWith("is") && name.length() > 2 && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
                String propertyName = Character.toLowerCase(name.charAt(2)) + name.substring(3);
                getters.put(propertyName, method);
            }
        }
        return getters;
    }

    private static Map<String, Field> findFields(Class<?> clazz) {
        Map<String, Field> fields = new TreeMap<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                fields.putIfAbsent(field.getName(), field);
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    private static String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&apos;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String formatDocument(Document doc) throws SnapshotException {
        removeEmptyTextNodes(doc);
        doc.normalize();
        return transformToString(new DOMSource(doc));
    }

    private static String formatNode(Node node) throws SnapshotException {
        removeEmptyTextNodes(node);
        node.normalize();
        return transformToString(new DOMSource(node));
    }

    private static String transformToString(DOMSource source) throws SnapshotException {
        try {
            TransformerFactory tf = createTransformerFactory();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            StringWriter writer = new StringWriter();
            transformer.transform(source, new StreamResult(writer));
            return writer.toString().replace("\r\n", "\n").replace('\r', '\n').trim();
        } catch (Exception e) {
            throw new SnapshotException("Failed to format XML: " + e.getMessage(), e);
        }
    }

    private static DocumentBuilderFactory createDocumentBuilderFactory() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
            return dbf;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure secure DocumentBuilderFactory", e);
        }
    }

    private static TransformerFactory createTransformerFactory() {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            return tf;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure secure TransformerFactory", e);
        }
    }

    private static void removeEmptyTextNodes(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE && child.getNodeValue().trim().isEmpty()) {
                node.removeChild(child);
            } else if (child.hasChildNodes()) {
                removeEmptyTextNodes(child);
            }
        }
    }
}
