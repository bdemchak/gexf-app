package edu.umuc.swen670.gexf.internal.model;

import java.io.IOException;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;


public class GEXF10Parser extends GEXFParserBase {

	public GEXF10Parser(XMLStreamReader xmlReader, CyNetwork cyNetwork, String version, CyGroupFactory cyGroupFactory, CyGroupManager cyGroupManager) {
		super(xmlReader, cyNetwork, version, cyGroupFactory, cyGroupManager);
	}

	@Override
	public void ParseStream() throws IOException, XMLStreamException {
		
		String defaultEdgeType = "";
		String mode = "";
		
		_cyNetwork.getDefaultEdgeTable().createColumn(GEXFEdge.EDGETYPE, String.class, true);
		_cyNetwork.getDefaultEdgeTable().createColumn(GEXFEdge.WEIGHT, Double.class, true);
		
		SetupVisualMapping();
		
		while(_xmlReader.hasNext()) {
			int event = _xmlReader.next();

			switch(event) {
			case XMLStreamConstants.END_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFGraph.GRAPH)) {
					CreateGroups();
					
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
					if(mode.equalsIgnoreCase(GEXFGraph.DYNAMIC)) {throw new InvalidClassException("Dynamic graphs are not supported.");}
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
					ParseNodes(null);
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFEdge.EDGES)) {
					ParseEdges(defaultEdgeType);
				}
			}
		}
		
		throw new InvalidClassException("Missing Graph tags");
	}
	
	@Override
	protected <T> List<T> ParseArray(String array, Class<T> type) throws IOException {
		String[] values = array.split("\\,|\\;|\\|");
		
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
