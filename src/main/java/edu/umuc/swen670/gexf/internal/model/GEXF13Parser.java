package edu.umuc.swen670.gexf.internal.model;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.cytoscape.model.CyNetwork;

import edu.umuc.swen670.gexf.internal.io.DelayedVizProp;

public class GEXF13Parser extends GEXFParserBase {

	public GEXF13Parser(XMLStreamReader xmlReader, CyNetwork cyNetwork, String version) {
		super(xmlReader, cyNetwork, version);
	}

	@Override
	public List<DelayedVizProp> ParseStream() throws IOException, XMLStreamException {
		
		String defaultEdgeType = "";
		String mode = "";
		
		_cyNetwork.getDefaultEdgeTable().createColumn(GEXFEdge.EDGETYPE, String.class, true);
		_cyNetwork.getDefaultEdgeTable().createColumn(GEXFEdge.WEIGHT, Double.class, true);
		
		while(_xmlReader.hasNext()) {
			int event = _xmlReader.next();

			switch(event) {
			case XMLStreamConstants.END_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFGraph.GRAPH)) {
					return _vizProps;
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
					ParseNode(null);
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
		List<T> list = new ArrayList<T>();
		
		StringReader reader = new StringReader(array + ' '); //pad the string
		
		int r;
		char c;
		while((r=reader.read()) != -1) {
			c = (char)r;
			
			if(c=='[' || c=='(' || c==']' || c==')' || c==',' || c==' ' || c=='\t' || c=='\r' || c=='\n') {
				//keep processing
			}
			else if(c=='"' || c=='\'') {
				String literal = "";
				
				do {
					literal = literal + c;
					
					c = (char)reader.read();
				}while(c!='"' && c!='\'');
				
				list.add(GenericParse(literal, type));
				reader.skip(-1);
			}
			else {
				String value = "";
				
				do {
					value = value + c;
					
					c = (char)reader.read();
				}while(c!=']' && c!=')' && c!=',' && c!=' ' && c!='\t' && c!='\r' && c!='\n');
				
				if(value.equals("null")) {
					list.add(null);
				}
				else {
					list.add(GenericParse(value, type));
				}
				
				reader.skip(-1);
			}
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
