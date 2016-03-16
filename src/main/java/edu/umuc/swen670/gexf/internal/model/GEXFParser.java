package edu.umuc.swen670.gexf.internal.model;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.cytoscape.model.CyNetwork;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class GEXFParser {

	public void ParseStream(InputStream inputStream, CyNetwork cyNetwork) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, XMLStreamException {
		byte[] streamBytes = getBytes(inputStream);
		ByteArrayInputStream inputStream2 = new ByteArrayInputStream(streamBytes);
		
		XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
		XMLStreamReader xmlReader = xmlFactory.createXMLStreamReader(inputStream2);
		
		String version = "";
		
		loop: while(xmlReader.hasNext()) {
			int event = xmlReader.next();

			switch(event) {
			case XMLStreamConstants.START_ELEMENT :
				if(xmlReader.getLocalName().equalsIgnoreCase("gexf")) {
					version = xmlReader.getAttributeValue(null, "version").trim();
					break loop;
				}
			}
		}
		
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;

		dBuilder = dbFactory.newDocumentBuilder();

		ByteArrayInputStream inputStream3 = new ByteArrayInputStream(streamBytes);
		Document doc = dBuilder.parse(inputStream3);

		doc.getDocumentElement().normalize();
		
		GEXFParserBase parser = null;
		
		if(version.startsWith(GEXFGraph.VERSION0) || version.equalsIgnoreCase(GEXFGraph.VERSION10)) {
			parser = new GEXF10Parser(doc, xmlReader, cyNetwork, version);
		}
		else if(version.equalsIgnoreCase(GEXFGraph.VERSION11)) {
			parser = new GEXF12Parser(doc, xmlReader, cyNetwork, version);
		}
		else if(version.equalsIgnoreCase(GEXFGraph.VERSION12)) {
			parser = new GEXF12Parser(doc, xmlReader, cyNetwork, version);
		}
		else if(version.equalsIgnoreCase(GEXFGraph.VERSION13)) {
			parser = new GEXF13Parser(doc, xmlReader, cyNetwork, version);
		}
		else {
			//try to parse with the latest supported version
			parser = new GEXF13Parser(doc, xmlReader, cyNetwork, version);
		}

		parser.ParseStream();
	}
	
	private byte[] getBytes(InputStream is) throws IOException {

	    int len;
	    int size = 10240;
	    byte[] buf;

	    if (is instanceof ByteArrayInputStream) {
	      size = is.available();
	      buf = new byte[size];
	      len = is.read(buf, 0, size);
	    } else {
	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
	      buf = new byte[size];
	      while ((len = is.read(buf, 0, size)) != -1)
	        bos.write(buf, 0, len);
	      buf = bos.toByteArray();
	    }
	    return buf;
	  }
}
