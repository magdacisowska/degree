package com.example.nativeapp;

import androidx.annotation.Nullable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FillTemplate {

    public static String fillChangesetXML() throws ParserConfigurationException, TransformerException {
        // create a new document
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        Element root = document.createElement("osm");
        root.setAttribute("version", "0.6");
        root.setAttribute("generator", "masterthesis-generator");
        document.appendChild(root);

        Element changeset = document.createElement("changeset");
        root.appendChild(changeset);

        Element tag = document.createElement("tag");
        tag.setAttribute("k", "created_by");
        tag.setAttribute("v", "m4gd4c_dev");
        changeset.appendChild(tag);

        // write the content into a string
        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);

        return writer.toString();
    }

    public static String fillTemplate(com.example.nativeapp.Requests.operation op, int currentChangeset, @Nullable com.example.nativeapp.OSM_Node node, @Nullable double latitude, @Nullable double longitude, Map<String, String> tags) throws ParserConfigurationException, TransformerException {
        // create a new document
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        // root element
        Element rootElement;
        switch(op){
            case MODIFY:
                rootElement = document.createElement("osmChange");
                break;
            default:
                rootElement = document.createElement("osm");
        }
        rootElement.setAttribute("version", "0.6");
        rootElement.setAttribute("generator", "masterthesis-generator");
        document.appendChild(rootElement);

        // "node" element
        Element nodeElement;
        switch(op){
            case MODIFY:
                // additional "modify" element required before
                Element modifyElement = document.createElement("modify");
                rootElement.appendChild(modifyElement);

                nodeElement = document.createElement("node");
                nodeElement.setAttribute("id", node.id);
                nodeElement.setAttribute("version", node.ver);
                nodeElement.setAttribute("changeset", Integer.toString(currentChangeset));
                nodeElement.setAttribute("lat", node.lat);
                nodeElement.setAttribute("lon", node.lon);
                modifyElement.appendChild(nodeElement);

                // overwrite tags of the node
                Set newTagsSet = tags.entrySet();
                Iterator i = newTagsSet.iterator();
                while (i.hasNext()){
                    Element tagElement = document.createElement("tag");
                    Map.Entry tag = (Map.Entry) i.next();
                    tagElement.setAttribute("k", tag.getKey().toString());
                    tagElement.setAttribute("v", tag.getValue().toString());
                    nodeElement.appendChild(tagElement);
                }
                break;
            case DELETE:
                nodeElement = document.createElement("node");
                nodeElement.setAttribute("id", node.id);
                nodeElement.setAttribute("version", node.ver);
                nodeElement.setAttribute("changeset", Integer.toString(currentChangeset));
                nodeElement.setAttribute("lat", node.lat);
                nodeElement.setAttribute("lon", node.lon);
                rootElement.appendChild(nodeElement);
                break;
            default:                                    // CREATE
                nodeElement = document.createElement("node");
                nodeElement.setAttribute("changeset", Integer.toString(currentChangeset));
                nodeElement.setAttribute("lat", Double.toString(latitude));
                nodeElement.setAttribute("lon", Double.toString(longitude));
                rootElement.appendChild(nodeElement);

                // add tags to the node
                Set tagsSet = tags.entrySet();
                Iterator j = tagsSet.iterator();
                while (j.hasNext()){
                    Element tagElement = document.createElement("tag");
                    Map.Entry tag = (Map.Entry) j.next();
                    tagElement.setAttribute("k", tag.getKey().toString());
                    tagElement.setAttribute("v", tag.getValue().toString());
                    nodeElement.appendChild(tagElement);
                }
        }
        // write the content into a string
        DOMSource domSource = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);

        return writer.toString();
    }
}

