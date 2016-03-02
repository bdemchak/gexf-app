package edu.umuc.swen670.gexf.internal.model;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.cytoscape.model.CyNetwork;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GEXFParser {

	public void ParseStream(InputStream inputStream, CyNetwork cyNetwork) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;

		dBuilder = dbFactory.newDocumentBuilder();

		Document doc = dBuilder.parse(inputStream);

		doc.getDocumentElement().normalize();
		
		XPath xPath =  XPathFactory.newInstance().newXPath();
		
		String expression = "/gexf";
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
		
		Element xElem = (Element) nodeList.item(0);
		
		String version = xElem.getAttribute(GEXFGraph.VERSION).trim();
		String variant = xElem.hasAttribute(GEXFGraph.VARIANT) ? xElem.getAttribute(GEXFGraph.VARIANT).trim() : "";
		
		GEXFParserBase parser = null;
		
		if(version.startsWith(GEXFGraph.VERSION0) || version.equalsIgnoreCase(GEXFGraph.VERSION10)) {
			parser = new GEXF10Parser(doc, cyNetwork, version);
		}
		else if(version.equalsIgnoreCase(GEXFGraph.VERSION11)) {
			parser = new GEXF12Parser(doc, cyNetwork, version);
		}
		else if(version.equalsIgnoreCase(GEXFGraph.VERSION12)) {
			parser = new GEXF12Parser(doc, cyNetwork, version);
		}
		else if(version.equalsIgnoreCase(GEXFGraph.VERSION13)) {
			parser = new GEXF13Parser(doc, cyNetwork, version);
		}
		else {
			//try to parse with the latest supported version
			parser = new GEXF13Parser(doc, cyNetwork, version);
		}

		parser.ParseStream();
	}
}
