package edu.umuc.swen670.gexf.internal.model;

import java.io.IOException;
import java.io.InvalidClassException;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
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
	protected CyNetwork _cyNetwork = null;
	protected String _version = "";
	
	protected Hashtable<String, Long> _idMapping = new Hashtable<String, Long>();
	AttributeMapping _attNodeMapping = null;
	AttributeMapping _attEdgeMapping = null;
	
	public GEXFParserBase(Document doc, CyNetwork cyNetwork, String version) {
		_doc = doc;
		_cyNetwork = cyNetwork;
		_version = version;
	}
	
	public abstract void ParseStream() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException;
	
	protected void ParseMeta() throws XPathExpressionException, InvalidClassException {
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "/gexf/meta";
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(_doc, XPathConstants.NODESET);
		
		if(nodeList.getLength() == 1) {
			Node metaNode = nodeList.item(0);
			
			CyTable cyTable = _cyNetwork.getDefaultNetworkTable();
			CyRow cyRow = cyTable.getRow(_cyNetwork.getSUID());
			
			Element metaElement = (Element) metaNode;
			if(metaElement.hasAttribute(GEXFMeta.LASTMODIFIEDDATE)) {
				cyTable.createColumn(GEXFMeta.LASTMODIFIEDDATE, String.class, false);
				
				cyRow.set(GEXFMeta.LASTMODIFIEDDATE, metaElement.getAttribute(GEXFMeta.LASTMODIFIEDDATE).trim());
			}
			
			if(metaNode.hasChildNodes()) {
				NodeList childNodes = metaNode.getChildNodes();
				for(int i=0; i<childNodes.getLength(); i++) {
					Node childNode = childNodes.item(i);
					
					//skip TEXT_NODE items
					if(childNode.getNodeType() != Node.ELEMENT_NODE) continue;
					
					if(childNode.getNodeName().trim().equalsIgnoreCase(GEXFMeta.CREATOR) || 
							childNode.getNodeName().trim().equalsIgnoreCase(GEXFMeta.KEYWORDS) || 
							childNode.getNodeName().trim().equalsIgnoreCase(GEXFMeta.DESCRIPTION)) {
						cyTable.createColumn(childNode.getNodeName().trim().toLowerCase(), String.class, false);
						
						cyRow.set(childNode.getNodeName().trim().toLowerCase(), childNode.getTextContent().trim());
					}
					else {
						throw new InvalidClassException(childNode.getNodeName().trim());
					}
				}
			}
			
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected AttributeMapping ParseAttributeHeader(String attributeClass) throws XPathExpressionException, IOException {
		AttributeMapping attMapping = new AttributeMapping();

		CyTable cyTable;
		if(attributeClass.equalsIgnoreCase("node")) {
			cyTable = _cyNetwork.getDefaultNodeTable();
		} else if(attributeClass.equalsIgnoreCase("edge")) {
			cyTable = _cyNetwork.getDefaultEdgeTable();
		} else {
			throw new InvalidClassException(attributeClass);
		}
		

		XPath xPath =  XPathFactory.newInstance().newXPath();
		String expression = "/gexf/graph/attributes[@class='" + attributeClass +"']/attribute";
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(_doc, XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node xNode = nodeList.item(i);

			if (xNode.getNodeType() == Node.ELEMENT_NODE) {
				Element xElem = (Element) xNode;

				String xId = xElem.getAttribute(GEXFAttribute.ID).trim();
				String xTitle = xElem.getAttribute(GEXFAttribute.TITLE).trim();
				String xType = xElem.getAttribute(GEXFAttribute.TYPE).trim();
				String xDefault = null;

				if(xNode.hasChildNodes()) {
					NodeList childNodes = xNode.getChildNodes();
					for(int j=0; j< childNodes.getLength(); j++) {
						Node childNode = childNodes.item(j);
						if(childNode.getNodeName().equalsIgnoreCase(GEXFAttribute.DEFAULT)) {
							xDefault = childNode.getTextContent().trim();
						}
					}
				}
				
				Class type = GetClass(xType);

				if(xDefault == null || xDefault.length() == 0) {
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
			}
		}

		return attMapping;
	}
	
	protected void ParseNode(Node xNode, String expression) throws XPathExpressionException, IOException {
		if (xNode.getNodeType() == Node.ELEMENT_NODE) {
			Element xElem = (Element) xNode;

			String xLabel = xElem.getAttribute(GEXFNode.LABEL);
			String xId = xElem.getAttribute(GEXFNode.ID);

			CyNode cyNode = _cyNetwork.addNode();
			_cyNetwork.getRow(cyNode).set(CyNetwork.NAME, xLabel);

			_idMapping.put(xId, cyNode.getSUID());

			if(xNode.hasChildNodes()) {
				String attExpression = expression + "[@id='" + xId + "']/attvalues/attvalue";

				ParseAttributes(cyNode, _attNodeMapping, attExpression);
			}
		}
	}
	
	protected void ParseEdges(String defaultEdgeType) throws XPathExpressionException, IOException {
		XPath xPath =  XPathFactory.newInstance().newXPath();
		String expression = "/gexf/graph/edges/edge";
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(_doc, XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node xNode = nodeList.item(i);

			if (xNode.getNodeType() == Node.ELEMENT_NODE) {
				Element xElem = (Element) xNode;

				String xId = xElem.getAttribute(GEXFEdge.ID).trim();
				String xSource = xElem.getAttribute(GEXFEdge.SOURCE).trim();
				String xTarget = xElem.getAttribute(GEXFEdge.TARGET).trim();
				String xEdgeType = xElem.hasAttribute(GEXFEdge.EDGETYPE) ? xElem.getAttribute(GEXFEdge.EDGETYPE).trim() : defaultEdgeType;
				String xEdgeWeight = xElem.hasAttribute(GEXFEdge.WEIGHT) ? xElem.getAttribute(GEXFEdge.WEIGHT).trim() : "";
				

				CyEdge cyEdge = _cyNetwork.addEdge(_cyNetwork.getNode(_idMapping.get(xSource)), _cyNetwork.getNode(_idMapping.get(xTarget)), IsDirected(xEdgeType));
				CyEdge cyEdgeReverse = IsBiDirectional(xEdgeType) ? _cyNetwork.addEdge(_cyNetwork.getNode(_idMapping.get(xTarget)), _cyNetwork.getNode(_idMapping.get(xSource)), IsDirected(xEdgeType)) : null;
				
				_cyNetwork.getRow(cyEdge).set(GEXFEdge.EDGETYPE, xEdgeType);
				if(cyEdgeReverse!=null) _cyNetwork.getRow(cyEdgeReverse).set(GEXFEdge.EDGETYPE, xEdgeType);
				
				if(xElem.hasAttribute(GEXFEdge.WEIGHT)) {
					_cyNetwork.getRow(cyEdge).set(GEXFEdge.WEIGHT, Double.parseDouble(xEdgeWeight));
					if(cyEdgeReverse!=null) _cyNetwork.getRow(cyEdgeReverse).set(GEXFEdge.WEIGHT, Double.parseDouble(xEdgeWeight));
				}
				
				if(xNode.hasChildNodes()) {
					String attExpression = expression + "[@id='" + xId + "']/attvalues/attvalue";
					
					ParseAttributes(cyEdge, _attEdgeMapping, attExpression);
					if(cyEdgeReverse!=null) ParseAttributes(cyEdgeReverse, _attEdgeMapping, attExpression);
				}
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void ParseAttributes(CyIdentifiable cyIdentifiable, AttributeMapping attMapping, String expression) throws XPathExpressionException, IOException {
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
	
	protected abstract Boolean IsDirected(String direction);
	
	protected abstract Boolean IsBiDirectional(String direction);
}