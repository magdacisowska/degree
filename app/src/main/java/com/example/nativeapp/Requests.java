package com.example.nativeapp;

import androidx.annotation.Nullable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static com.example.nativeapp.FillTemplate.fillChangesetXML;
import static com.example.nativeapp.FillTemplate.fillTemplate;

/**
 * Class handling the OpenStreetMaps REST API
 * Static methods enable downloading bounding box, creating new changeset and editing nodes
 */
public class Requests {

    public enum operation{
        CREATE, MODIFY, DELETE
    }

    public static List<OSM_Node> getBoundBox(String basicAuthPayload, double left, double bottom, double right, double top) throws IOException, ParserConfigurationException, SAXException {
        // Connect to the server to download points
        URL serverUrl = new URL("https://master.apis.dev.openstreetmap.org/api/0.6/map?bbox="+left+","+bottom+","+right+","+top);
        HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.addRequestProperty("Authorization", basicAuthPayload);

        // Read response from web server, which will trigger HTTP Basic Authentication request to be sent.
        BufferedReader httpResponseReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        // Save response as a String
        String lineRead;
        StringBuilder builder = new StringBuilder();
        while ((lineRead = httpResponseReader.readLine()) != null) {
            builder.append(lineRead);
        }
        httpResponseReader.close();

        // Parse the xml response
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(builder.toString()));
        Document doc = dBuilder.parse(is);

        List<OSM_Node> serverNodesList = new ArrayList<>();      // list of all OSM_Node objects within the bbox

        // Iterate through nodes
        NodeList nodeList = doc.getElementsByTagName("node");
        for (int i = 0; i < nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            Element nodeElement = (Element) node;              // cast node as an Element object
            OSM_Node newNode = new OSM_Node(                   // create a new OSM_Node object
                    nodeElement.getAttribute("id"),
                    nodeElement.getAttribute("lat"),
                    nodeElement.getAttribute("lon"),
                    nodeElement.getAttribute("version")
            );
            // Iterate through tags
            NodeList tagList = nodeElement.getElementsByTagName("tag");
            for (int j = 0; j < tagList.getLength(); j++){
                Node tag = tagList.item(j);
                Element tagElement = (Element) tag;
                newNode.append_tags(
                        tagElement.getAttribute("k"),
                        tagElement.getAttribute("v")
                );
            }
            // ignore non traffic_sign nodes
            if (newNode.tags.containsKey("traffic_sign")) {
                serverNodesList.add(newNode);
            }
        }
        // return list of OSM_Node objects
        return serverNodesList;
    }

    public static Integer createChangeset(String basicAuthPayload) throws IOException, TransformerException, ParserConfigurationException {
        URL serverUrl = new URL("https://master.apis.dev.openstreetmap.org/api/0.6/changeset/create");
        HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();

        // request configuration
        urlConnection.setRequestMethod("PUT");
        urlConnection.setRequestProperty("Accept", "text/xml");
        urlConnection.addRequestProperty("Authorization", basicAuthPayload);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        // set the xml body of the request
        String body = fillChangesetXML();

        // output stream buffer - stores what the app sends to the server
        OutputStream output = new BufferedOutputStream(urlConnection.getOutputStream());
        output.write(body.getBytes());
        output.flush();
        // input stream buffer - stores what the app receives from the server
        BufferedReader httpResponseReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        // return response -  ID of the newly created changeset
        String response = httpResponseReader.readLine();
        return Integer.valueOf(response);
    }

    public static String modifyNode(String basicAuthPayload, operation op, int currentChangeset, @Nullable OSM_Node node, @Nullable double latitude, @Nullable double longitude, Map<String, String> tags) throws IOException, ParserConfigurationException, TransformerException {
        URL serverUrl;
        HttpsURLConnection urlConnection;

        switch (op){
            case MODIFY:
                serverUrl = new URL("https://master.apis.dev.openstreetmap.org/api/0.6/changeset/" + currentChangeset + "/upload");
                urlConnection = (HttpsURLConnection) serverUrl.openConnection();
                urlConnection.setRequestMethod("POST");
                break;
            case DELETE:
                serverUrl = new URL("https://master.apis.dev.openstreetmap.org/api/0.6/node/" + node.id);
                urlConnection = (HttpsURLConnection) serverUrl.openConnection();
                urlConnection.setRequestMethod("DELETE");
                break;
            default:                                            // create is the default
                serverUrl = new URL("https://master.apis.dev.openstreetmap.org/api/0.6/node/create");
                urlConnection = (HttpsURLConnection) serverUrl.openConnection();
                urlConnection.setRequestMethod("PUT");
                break;
        }

        // finish request configuration
        urlConnection.setRequestProperty("Accept", "text/xml");
        urlConnection.addRequestProperty("Authorization", basicAuthPayload);
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        // set the xml body of the request
        String body = fillTemplate(op, currentChangeset, node, latitude, longitude, tags);

        // output stream buffer - stores what the app sends to the server
        OutputStream output = new BufferedOutputStream(urlConnection.getOutputStream());
        output.write(body.getBytes());
        output.flush();
        // input stream buffer - stores what the app receives from the server
        BufferedReader httpResponseReader;

        int responseCode = urlConnection.getResponseCode();
        if (responseCode == 409){
            // return error message in case of Http 409 Error
            httpResponseReader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
        }
        else {
            // return new node ID when no errors occurred
            httpResponseReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        }
        return httpResponseReader.readLine();
    }
}
