package edu.umuc.swen670.gexf.internal.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.umuc.swen670.gexf.internal.io.GEXFFileFilter;

public class GEXF12Parser extends GEXFParserBase {
	
	public GEXF12Parser(Document doc, XMLStreamReader xmlReader, CyNetwork cyNetwork, String version) {
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

/*
		XPath xPath =  XPathFactory.newInstance().newXPath();
		String expression = "/gexf/graph/nodes/node";
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(_doc, XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node xNode = nodeList.item(i);

			ParseNode(xNode, expression);
		}
*/

		//ParseEdges(defaultEdgeType);
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
		if(direction.equalsIgnoreCase(EdgeTypes.DIRECTED)) {
			return true;
		}
		else if(direction.equalsIgnoreCase(EdgeTypes.UNDIRECTED)) {
			return false;
		}
		else if (direction.equalsIgnoreCase(EdgeTypes.MUTUAL)) {
			return true;
		}
		else {
			throw new IllegalArgumentException(direction);
		}
	}

	@Override
	protected Boolean IsBiDirectional(String direction) {
		if (direction.equalsIgnoreCase(EdgeTypes.MUTUAL)) {
			return true;
		}
		else {
			return false;
		}
	}

}
