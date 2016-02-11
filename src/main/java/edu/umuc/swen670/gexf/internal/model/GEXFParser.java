package edu.umuc.swen670.gexf.internal.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.cytoscape.model.CyNetwork;
import org.xml.sax.SAXException;

public class GEXFParser {

	public void ParseStream(InputStream inputStream, CyNetwork cyNetwork) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		//TODO Check the GEXF file version as use the appropriate parser
		
		GEXF12Parser parser = new GEXF12Parser();
		
		parser.ParseStream(inputStream, cyNetwork);
	}
}
