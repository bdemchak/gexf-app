package edu.umuc.swen670.gexf.internal.model;

import java.io.IOException;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

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
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

abstract class GEXFParserBase {

	protected Document _doc = null;
	protected XMLStreamReader _xmlReader = null;
	protected CyNetwork _cyNetwork = null;
	protected String _version = "";
	
	protected Hashtable<String, Long> _idMapping = new Hashtable<String, Long>();
	AttributeMapping _attNodeMapping = null;
	AttributeMapping _attEdgeMapping = null;
	
	public GEXFParserBase(Document doc, XMLStreamReader xmlReader, CyNetwork cyNetwork, String version) {
		_doc = doc;
		_xmlReader = xmlReader;
		_cyNetwork = cyNetwork;
		_version = version;
	}
	
	public abstract void ParseStream() throws XPathExpressionException, ParserConfigurationException, IOException, XMLStreamException;
	
	protected void ParseMeta() throws XPathExpressionException, InvalidClassException, XMLStreamException {
		CyTable cyTable = _cyNetwork.getDefaultNetworkTable();
		CyRow cyRow = cyTable.getRow(_cyNetwork.getSUID());
				
		List<String> attributes = GetElementAttributes();
		if(attributes.contains(GEXFMeta.LASTMODIFIEDDATE)) {
			cyTable.createColumn(GEXFMeta.LASTMODIFIEDDATE, String.class, false);
			cyRow.set(GEXFMeta.LASTMODIFIEDDATE, _xmlReader.getAttributeValue(null, GEXFMeta.LASTMODIFIEDDATE).trim());
		}
		
		String tagContent = null;
		
		while(_xmlReader.hasNext()) {
			int event = _xmlReader.next();

			switch(event) {
			case XMLStreamConstants.END_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFMeta.META)) {
					return;
				}
				else if(_xmlReader.getLocalName().trim().equalsIgnoreCase(GEXFMeta.CREATOR) || 
						_xmlReader.getLocalName().equalsIgnoreCase(GEXFMeta.DESCRIPTION) || 
						_xmlReader.getLocalName().equalsIgnoreCase(GEXFMeta.KEYWORDS)) {
					
					cyTable.createColumn(_xmlReader.getLocalName().trim().toLowerCase(), String.class, false);
					cyRow.set(_xmlReader.getLocalName().trim().toLowerCase(), tagContent.trim());
					
					break;
				}
				else {
					throw new InvalidClassException(_xmlReader.getLocalName().trim());
				}
			case XMLStreamConstants.CHARACTERS :
				tagContent = _xmlReader.getText();
				break;
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected AttributeMapping ParseAttributeHeader(String attributeClass) throws IOException, XMLStreamException {
		AttributeMapping attMapping = new AttributeMapping();

		CyTable cyTable;
		if(attributeClass.equalsIgnoreCase(GEXFAttribute.NODE)) {
			cyTable = _cyNetwork.getDefaultNodeTable();
		} else if(attributeClass.equalsIgnoreCase(GEXFAttribute.EDGE)) {
			cyTable = _cyNetwork.getDefaultEdgeTable();
		} else {
			throw new InvalidClassException(attributeClass);
		}
		
		String xId = null;
		String xTitle = null;
		String xType = null;
		
		String xDefault = null;
		Boolean hasDefault = false;
		
		while(_xmlReader.hasNext()) {
			int event = _xmlReader.next();

			switch(event) {
			case XMLStreamConstants.START_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFAttribute.ATTRIBUTE)) {
					xId = _xmlReader.getAttributeValue(null, GEXFAttribute.ID).trim();
					xTitle = _xmlReader.getAttributeValue(null, GEXFAttribute.TITLE).trim();
					xType =_xmlReader.getAttributeValue(null, GEXFAttribute.TYPE).trim();
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFAttribute.DEFAULT)) {
					hasDefault = true;
				}
				break;
			case XMLStreamConstants.CHARACTERS :
				if(hasDefault && xDefault == null) {
					xDefault = _xmlReader.getText().trim();
				}
				break;
			case XMLStreamConstants.END_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFAttribute.ATTRIBUTES)) {
					return attMapping;
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFAttribute.ATTRIBUTE)) {
					Class type = GetClass(xType);
					
					if(!hasDefault) {
						if(!type.isArray()) {
							cyTable.createColumn(xTitle, type, false);
						}
						else {
							cyTable.createListColumn(xTitle, type.getComponentType(), false);
						}
					}
					else {
						if(!type.isArray()) {
							cyTable.createColumn(xTitle, type, false, GenericParse(xDefault, type));
						}
						else {
							cyTable.createListColumn(xTitle, type.getComponentType(), false, ParseArray(xDefault, type.getComponentType()));
						}
					}
					
					attMapping.Id.put(xId, xTitle);
					attMapping.Type.put(xId, xType);
					
					
					
					//reset the storage
					xId = null;
					xTitle = null;
					xType = null;
					
					hasDefault = false;
					xDefault = null;
				}
				break;
			}
		}
		
		throw new InvalidClassException("Missing AttributeHeader tags");
	}
	
	protected void ParseNode(CyNode cyNodeParent) throws IOException, XMLStreamException {
		
		CyNode cyNode = null;
		
		while(_xmlReader.hasNext()) {
			int event = _xmlReader.next();

			switch(event) {
			case XMLStreamConstants.END_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFNode.NODES)) {
					return;
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFNode.NODE)) {
					cyNode = null;
					break;
				}
			case XMLStreamConstants.START_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFNode.NODE)) {
					String xLabel = _xmlReader.getAttributeValue(null, GEXFNode.LABEL).trim();
					String xId = _xmlReader.getAttributeValue(null, GEXFNode.ID).trim();
					
					cyNode = _cyNetwork.addNode();
					_cyNetwork.getRow(cyNode).set(CyNetwork.NAME, xLabel);

					_idMapping.put(xId, cyNode.getSUID());
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFAttribute.ATTVALUES)) {
					ParseAttributes(new CyIdentifiable[] {cyNode}, _attNodeMapping);
				}
				
				break;
			}
		}
		
		throw new InvalidClassException("Missing Node tags");
	}
	
	protected void ParseEdges(String defaultEdgeType) throws IOException, XMLStreamException {
		
		CyEdge cyEdge = null;
		CyEdge cyEdgeReverse = null;
		
		while(_xmlReader.hasNext()) {
			int event = _xmlReader.next();

			switch(event) {
			case XMLStreamConstants.END_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFEdge.EDGES)) {
					return;
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFEdge.EDGE)) {
					cyEdge = null;
					cyEdgeReverse = null;
					break;
				}
			case XMLStreamConstants.START_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFEdge.EDGE)) {
					List<String> edgeElementAttributes = GetElementAttributes();
					
					String xId = _xmlReader.getAttributeValue(null, GEXFEdge.ID).trim();
					String xSource = _xmlReader.getAttributeValue(null, GEXFEdge.SOURCE).trim();
					String xTarget = _xmlReader.getAttributeValue(null, GEXFEdge.TARGET).trim();
					String xEdgeType = edgeElementAttributes.contains(GEXFEdge.EDGETYPE) ? _xmlReader.getAttributeValue(null, GEXFEdge.EDGETYPE).trim() : defaultEdgeType;
					String xEdgeWeight = edgeElementAttributes.contains(GEXFEdge.WEIGHT) ? _xmlReader.getAttributeValue(null, GEXFEdge.WEIGHT).trim() : "";
					
					cyEdge = _cyNetwork.addEdge(_cyNetwork.getNode(_idMapping.get(xSource)), _cyNetwork.getNode(_idMapping.get(xTarget)), IsDirected(xEdgeType));
					cyEdgeReverse = IsBiDirectional(xEdgeType) ? _cyNetwork.addEdge(_cyNetwork.getNode(_idMapping.get(xTarget)), _cyNetwork.getNode(_idMapping.get(xSource)), IsDirected(xEdgeType)) : null;
					
					_cyNetwork.getRow(cyEdge).set(GEXFEdge.EDGETYPE, xEdgeType);
					if(cyEdgeReverse!=null) _cyNetwork.getRow(cyEdgeReverse).set(GEXFEdge.EDGETYPE, xEdgeType);
					
					if(edgeElementAttributes.contains(GEXFEdge.WEIGHT)) {
						_cyNetwork.getRow(cyEdge).set(GEXFEdge.WEIGHT, Double.parseDouble(xEdgeWeight));
						if(cyEdgeReverse!=null) _cyNetwork.getRow(cyEdgeReverse).set(GEXFEdge.WEIGHT, Double.parseDouble(xEdgeWeight));
					}
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFAttribute.ATTVALUES)) {
					ParseAttributes(new CyIdentifiable[] {cyEdge, cyEdgeReverse}, _attEdgeMapping);
				}
				
				break;
			}
		}
		
		throw new InvalidClassException("Missing Edge tags");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void ParseAttributes(CyIdentifiable[] cyIdentifiables, AttributeMapping attMapping) throws IOException, XMLStreamException {
		
		while(_xmlReader.hasNext()) {
			int event = _xmlReader.next();

			switch(event) {
			case XMLStreamConstants.END_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFAttribute.ATTVALUES)) {
					return;
				}
				break;
			case XMLStreamConstants.START_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFAttribute.ATTVALUE)) {
					String xFor = _xmlReader.getAttributeValue(null, GEXFAttribute.FOR).trim();
					String xValue = _xmlReader.getAttributeValue(null, GEXFAttribute.VALUE).trim();
					
					Class type = GetClass(attMapping.Type.get(xFor));
					if(!type.isArray()) {
						for(CyIdentifiable cyIdentifiable : cyIdentifiables) {
							_cyNetwork.getRow(cyIdentifiable).set(attMapping.Id.get(xFor), GenericParse(xValue, type));
						}
					}
					else {
						for(CyIdentifiable cyIdentifiable : cyIdentifiables) {
							_cyNetwork.getRow(cyIdentifiable).set(attMapping.Id.get(xFor), ParseArray(xValue, type));
						}
					}
				}
				break;
			}
		}
		
		
		throw new InvalidClassException("Missing Attribute Value tags");
		
/*
		XPath xPath =  XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(_doc, XPathConstants.NODESET);

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node xAttNode = nodeList.item(i);
			if (xAttNode.getNodeType() == Node.ELEMENT_NODE) {
				Element xElem = (Element) xAttNode;
				String xFor = xElem.getAttribute(GEXFAttribute.FOR).trim();
				String xValue = xElem.getAttribute(GEXFAttribute.VALUE).trim();

				Class type = GetClass(attMapping.Type.get(xFor));
				if(!type.isArray()) {
					_cyNetwork.getRow(cyIdentifiable).set(attMapping.Id.get(xFor), GenericParse(xValue, type));
				}
				else {
					_cyNetwork.getRow(cyIdentifiable).set(attMapping.Id.get(xFor), ParseArray(xValue, type));
				}
			}
		}
*/
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T GenericParse(String value, Class<T> type) throws InvalidClassException {
		if(type.equals(Integer.class)) {
			return (T)(Integer)Integer.parseInt(value);
		}
		else if(type.equals(Long.class)) {
			return (T)(Long)Long.parseLong(value);
		}
		else if(type.equals(Double.class)) {
			return (T)(Double)Double.parseDouble(value);
		}
		else if(type.equals(Boolean.class)) {
			return (T)(Boolean)Boolean.parseBoolean(value);
		}
		else if(type.equals(String.class)) {
			return (T)value;
		}
		else {
			throw new InvalidClassException(type.getName());
		}
	}
	
	protected abstract <T> List<T> ParseArray(String array, Class<T> type) throws IOException;
	
	@SuppressWarnings("rawtypes")
	protected Class GetClass(String type) throws InvalidClassException {
		if(type.equalsIgnoreCase(DataTypes.INTEGER)) {
			return Integer.class;
		}
		if(type.equalsIgnoreCase(DataTypes.LONG)) {
			return Long.class;
		}
		else if(type.equalsIgnoreCase(DataTypes.FLOAT)) {
			//float not supported
			return Double.class;
		}
		else if(type.equalsIgnoreCase(DataTypes.DOUBLE)) {
			return Double.class;
		}
		else if(type.equalsIgnoreCase(DataTypes.BOOLEAN)) {
			return Boolean.class;
		}
		else if(type.equalsIgnoreCase(DataTypes.STRING)) {
			return String.class;
		}
		else if(type.equalsIgnoreCase(DataTypes.LISTINTEGER)) {
			return Integer[].class;
		}
		else if(type.equalsIgnoreCase(DataTypes.LISTLONG)) {
			return Long[].class;
		}
		else if(type.equalsIgnoreCase(DataTypes.LISTFLOAT)) {
			return Double[].class;
		}
		else if(type.equalsIgnoreCase(DataTypes.LISTDOUBLE)) {
			return Double[].class;
		}
		else if(type.equalsIgnoreCase(DataTypes.LISTBOOLEAN)) {
			return Boolean[].class;
		}
		else if(type.equalsIgnoreCase(DataTypes.LISTSTRING)) {
			return String[].class;
		}
		else {
			throw new InvalidClassException(type);
		}
	}
	
	protected List<String> GetElementAttributes() {
		List<String> attributes = new ArrayList<String>();
		
		int count = _xmlReader.getAttributeCount();
		for(int i=0; i<count; i++) {
			attributes.add(_xmlReader.getAttributeLocalName(i));
		}
		
		return attributes;
	}
	
	protected abstract Boolean IsDirected(String direction);
	
	protected abstract Boolean IsBiDirectional(String direction);
}