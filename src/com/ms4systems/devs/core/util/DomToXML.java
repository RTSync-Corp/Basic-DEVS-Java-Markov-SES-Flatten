package com.ms4systems.devs.core.util;

import java.io.File;
import java.io.IOException;
import org.w3c.dom.*;
import java.io.*;
import javax.swing.*;

public class DomToXML {

    public static void writeToFile(Document doc) {
        File file = null;
// create a file-chooser, to be used below
        JFileChooser chooser = new JFileChooser(new File("."));
        // keep doing this
        while (true) {
            // pop up the file chooser so the user may select which
            // files to convert
            int result = chooser.showOpenDialog(null);
            // if the user clicked 'ok' in the file chooser
            if (result == JFileChooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();
            }
            if (file != null) {
                break;
            }
        }
        try {
            OutputStream stream = new FileOutputStream(file);
            String s = write(doc);
            stream.write(s.getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(String myFile, Document doc) {
        File file = new File(myFile);

        try {
            OutputStream stream = new FileOutputStream(file);
            String s = write(doc);
            stream.write(s.getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String write(Document doc) {
        return write(doc.getDocumentElement(),
                doc.getDocumentElement().getNodeName());
    }

    public static String write(Node root, String rootElem) {
        return "<?xml version='1.0' encoding='UTF-8'?>\n"
                + "<!DOCTYPE " + rootElem + " SYSTEM "
                + quote("RuleCombo.dtd") + " []>\n"
                + writeElementChild(root);
    }

    public static String writeXsd(Document doc) {
        return "<?xml version='1.0' encoding='UTF-8'?>\n"
                + writeElementChild(doc.getDocumentElement());
    }

    public static String writeSchema(Node root, String rootElem, String filesch) {
        Element rootNode = (Element) root;
        String rootElemName = rootElem;
        if (rootElem.equals("top")) {
            NodeList nl = root.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeName().equals("entity")) {
                    rootNode = (Element) nl.item(i);
                }
                rootElemName = rootNode.getAttribute("name");
                break;
            }
        }
        String s = writeElementChild(rootNode);
        String attr = writeAttributes(rootNode);

        String ns = s.substring(attr.length() + rootElemName.length() + 1);
        if (!ns.equals(">")) {
            s = ns;
        } else {
            s = "></" + rootElem + ">";
        }
        String xmlns = " xmlns:xsi =  "
                + quote("http://www.w3.org/2001/XMLSchema-instance")
                + " xsi:noNamespaceSchemaLocation=" + quote(filesch);
        if (attr.indexOf("xmlns") > -1) {
            xmlns = "";
        }
        String res = attr + s;
        return "<?xml version=" + quote("1.0") + " encoding=" + quote("UTF-8")
                + "?>\n"
                + "<" + rootElem + xmlns
                + attr + s;
    }

    public static String write(Node root, String rootElem, String filedtd) {
        return "<?xml version='1.0' encoding='UTF-8'?>\n"
                + "<!DOCTYPE " + rootElem + " SYSTEM "
                + quote(filedtd) + " []>\n"
                + writeElementChild(root);
    }

    public static String writeElementChildren(Node n) {
        if (n == null) {
            return " ";
        }
        String s = " ";
        NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node nc = nl.item(i);
            if (nc.getNodeType() == Node.ELEMENT_NODE) {
                s += "\n" + writeElementChild(nc);
            }
        }
        return s;
    }

    public static String writeElementChild(Node n) {
        if (n == null) {
            return " ";
        }
        String firstLeft = "", firstRight = "";

        Element ne = (Element) n;
        String children = writeElementChildren(ne);
        firstLeft = "<" + ne.getNodeName();
        String middle = writeTextChildren(n);
        String attrs = writeAttributes(ne);
        if (middle.equals(" ") && attrs.equals(" ") && children.equals(" ")) {
            firstRight = "/>";
        } else {
            firstRight = "\n" + middle + "\n" + "</" + ne.getNodeName() + ">";
            firstLeft += attrs + ">" + "\n";
        }

        if (children.equals(" ")) {
            return firstLeft + firstRight;
        } else {
            return firstLeft + children + firstRight;
        }
    }

    public static String quote(String s) {
        String ss = " " + s + " ";
        char[] ca = ss.toCharArray();
        ca[0] = ca[ca.length - 1] = '"';
        String r = "";
        for (int i = 0; i < ca.length; i++) {
            r += ca[i];
        }
        return r;
    }

    public static boolean hasAttrWithName(NamedNodeMap m, String attrNm) {
        for (int i = 0; i < m.getLength(); i++) {
            Attr n = (Attr) m.item(i);
            if (n.getName().equals(attrNm)) {
                return true;
            }
        }
        return false;
    }

    public static String writeAttributes(Element e) {
        String s = " ";
        NamedNodeMap m = e.getAttributes();

        if (m == null) {
            return "";
        }
        for (int i = 0; i < m.getLength(); i++) {
            Attr n = (Attr) m.item(i);
            if (e.getNodeName().endsWith("MultiAsp") && n.getName().equals("coupling")) {
                continue;
            }
            String val = n.getNodeValue();
            if (val.endsWith("Value")) {
                val = val.substring(0, val.length()
                        - "Value".length());
           }
            String attrNm = n.getName();
            if (hasAttrWithName(m, attrNm + "DefaultValue")) {
                continue;
            }
            String shortattrNm = attrNm;
            if (attrNm.endsWith("DefaultValue")) {
                shortattrNm = attrNm.substring(0, attrNm.length() - "DefaultValue".length());
            }
                if (e.hasAttribute(shortattrNm)) {
                    s += " " + shortattrNm + " = " + quote(val);
                } else {
                    s += " " + attrNm + " = " + quote(val);
                }
            }

        return s;
    }

    public static String writeTextChildren(Node n) {
        if (n == null) {
            return " ";
        }
        String s = " ";
        NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node nc = nl.item(i);
            if (!nc.hasChildNodes()) {
                if (nc.getNodeName().equals("#text")) {
                    s += " " + nc.getNodeValue();
                }
            }
        }
        return s;
    }
}
