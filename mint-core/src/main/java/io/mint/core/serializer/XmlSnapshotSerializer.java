package io.mint.core.serializer;

import io.mint.core.SnapshotException;
import io.mint.core.SnapshotSerializer;
import org.w3c.dom.Document;
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
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * Snapshot serializer formatting objects as XML using JDK built-in {@code java.xml} components.
 *
 * <p><strong>Warning:</strong> Only serializes; does not deserialize. Do not
 * round-trip untrusted XML through this class.
 */
public final class XmlSnapshotSerializer implements SnapshotSerializer {

    /**
     * Constructs a new {@code XmlSnapshotSerializer}.
     */
    public XmlSnapshotSerializer() {
    }

    /**
     * Serializes an object to an XML representation.
     *
     * @param value the object to serialize, may be null
     * @return the XML serialized snapshot string, never null
     * @throws SnapshotException if XML processing fails
     */
    @Override
    public String serialize(Object value) throws SnapshotException {
        if (value == null) {
            return "<null/>";
        }
        if (value instanceof CharSequence) {
            return "<value>" + escapeXml(value.toString()) + "</value>";
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean encoderSuccess = false;
            try (XMLEncoder encoder = new XMLEncoder(baos)) {
                encoder.setExceptionListener(e -> {
                    throw new RuntimeException(e);
                });
                encoder.writeObject(value);
                encoderSuccess = true;
            } catch (Exception ignored) {
                encoderSuccess = false;
            }

            if (!encoderSuccess || baos.size() == 0) {
                return "<value>" + escapeXml(String.valueOf(value)) + "</value>";
            }

            return formatXml(baos.toByteArray());
        } catch (SnapshotException e) {
            throw e;
        } catch (Exception e) {
            return "<value>" + escapeXml(String.valueOf(value)) + "</value>";
        }
    }

    /**
     * Returns the format name for XML serialization.
     *
     * @return {@code "xml"}
     */
    @Override
    public String formatName() {
        return "xml";
    }

    private static String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String formatXml(byte[] xmlBytes) throws SnapshotException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setExpandEntityReferences(false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xmlBytes));
            removeEmptyTextNodes(doc);
            doc.normalize();

            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            tf.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString().trim();
        } catch (Exception e) {
            throw new SnapshotException("Failed to format XML: " + e.getMessage(), e);
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
