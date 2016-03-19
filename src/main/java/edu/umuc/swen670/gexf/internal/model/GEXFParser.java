package edu.umuc.swen670.gexf.internal.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.cytoscape.model.CyNetwork;

import edu.umuc.swen670.gexf.internal.io.DelayedVizProp;


public class GEXFParser {

	public List<DelayedVizProp> ParseStream(InputStream inputStream, CyNetwork cyNetwork) throws IOException, XMLStreamException {
		XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
		XMLStreamReader xmlReader = xmlFactory.createXMLStreamReader(inputStream);
		
		List<DelayedVizProp> vizProps = null;
		
		while(xmlReader.hasNext()) {
			int event = xmlReader.next();

			switch(event) {
			case XMLStreamConstants.START_ELEMENT :
				if(xmlReader.getLocalName().equalsIgnoreCase("gexf")) {
					String version = xmlReader.getAttributeValue(null, "version");
					if(version!=null) {version = version.trim();}
					
					GEXFParserBase parser = null;
					
					if(version == null) {
						//no version declared, try to parse with the latest supported version
						parser = new GEXF13Parser(xmlReader, cyNetwork, version);
					}
					else if(version.startsWith(GEXFGraph.VERSION0) || version.equalsIgnoreCase(GEXFGraph.VERSION10)) {
						parser = new GEXF10Parser(xmlReader, cyNetwork, version);
					}
					else if(version.equalsIgnoreCase(GEXFGraph.VERSION11)) {
						parser = new GEXF12Parser(xmlReader, cyNetwork, version);
					}
					else if(version.equalsIgnoreCase(GEXFGraph.VERSION12)) {
						parser = new GEXF12Parser(xmlReader, cyNetwork, version);
					}
					else if(version.equalsIgnoreCase(GEXFGraph.VERSION13)) {
						parser = new GEXF13Parser(xmlReader, cyNetwork, version);
					}
					else {
						//try to parse with the latest supported version
						parser = new GEXF13Parser(xmlReader, cyNetwork, version);
					}

					vizProps = parser.ParseStream();
				}
			}
		}
		
		return vizProps;
	}
}
