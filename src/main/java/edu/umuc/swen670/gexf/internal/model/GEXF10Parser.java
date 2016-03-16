package edu.umuc.swen670.gexf.internal.model;

import java.io.IOException;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.cytoscape.model.CyNetwork;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GEXF10Parser extends GEXFParserBase {

	public GEXF10Parser(Document doc, XMLStreamReader xmlReader, CyNetwork cyNetwork, String version) {
		super(doc, xmlReader, cyNetwork, version);
	}

	@Override
	public void ParseStream() throws XPathExpressionException, ParserConfigurationException, IOException, XMLStreamException {
		
		String defaultEdgeType = "";
		String mode = "";
		
		_cyNetwork.getDefaultEdgeTable().createColumn(GEXFEdge.EDGETYPE, String.class, true);
		_cyNetwork.getDefaultEdgeTable().createColumn(GEXFEdge.WEIGHT, Double.class, true);
		
		while(_xmlReader.hasNext()) {
			int event = _xmlReader.next();

			switch(event) {
			case XMLStreamConstants.END_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFGraph.GRAPH)) {
					return;
				}
			case XMLStreamConstants.START_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFMeta.META)) {
					ParseMeta();
					break;
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFGraph.GRAPH)) {
					List<String> graphAttributes = GetElementAttributes();
					
					defaultEdgeType = graphAttributes.contains(GEXFGraph.DEFAULTEDGETYPE) ? _xmlReader.getAttributeValue(null, GEXFGraph.DEFAULTEDGETYPE).trim() : EdgeTypes.UNDIRECTED;
					mode = graphAttributes.contains(GEXFGraph.MODE) ? _xmlReader.getAttributeValue(null, GEXFGraph.MODE).trim() : GEXFGraph.STATIC;
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFAttribute.ATTRIBUTES)) {
					String attributeClass = _xmlReader.getAttributeValue(null, GEXFAttribute.CLASS).trim();
					if(attributeClass.equalsIgnoreCase(GEXFAttribute.NODE)) {
						_attNodeMapping = ParseAttributeHeader(GEXFAttribute.NODE);
					}
					else if (attributeClass.equalsIgnoreCase(GEXFAttribute.EDGE)) {
						_attEdgeMapping = ParseAttributeHeader(GEXFAttribute.EDGE);
					}
					else {
						throw new InvalidClassException(attributeClass);
					}
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFNode.NODES)) {
					ParseNode(null);
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFEdge.EDGES)) {
					ParseEdges(defaultEdgeType);
				}
			}
		}
	}
	
	@Override
	protected <T> List<T> ParseArray(String array, Class<T> type) throws IOException {
		String[] values = array.split("|");
		
		List<T> list = new ArrayList<T>();
		for(String value : values) {
			list.add(GenericParse(value.trim(), type));
		}
		
		return list;
	}

	@Override
	protected Boolean IsDirected(String direction) {
		if(direction.equalsIgnoreCase(EdgeTypes.DIRECTED) || direction.equalsIgnoreCase(EdgeTypes.DIR)) {
			return true;
		}
		else if(direction.equalsIgnoreCase(EdgeTypes.SIMPLE) || direction.equalsIgnoreCase(EdgeTypes.SIM)) {
			return false;
		}
		else if (direction.equalsIgnoreCase(EdgeTypes.DOUBLE) || direction.equalsIgnoreCase(EdgeTypes.DOU)) {
			return true;
		}
		else {
			throw new IllegalArgumentException(direction);
		}
	}

	@Override
	protected Boolean IsBiDirectional(String direction) {
		if (direction.equalsIgnoreCase(EdgeTypes.DOUBLE) || direction.equalsIgnoreCase(EdgeTypes.DOU)) {
			return true;
		}
		else {
			return false;
		}
	}

}
