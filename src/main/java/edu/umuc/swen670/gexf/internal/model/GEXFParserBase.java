package edu.umuc.swen670.gexf.internal.model;

import java.io.IOException;
import java.io.InvalidClassException;
import java.util.Hashtable;

import javax.xml.parsers.ParserConfigurationException;
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
	
	protected AttributeMapping ParseAttributeHeader(String attributeClass) throws XPathExpressionException, InvalidClassException {
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

				if(xDefault == null || xDefault.length() == 0) {
					cyTable.createColumn(xTitle, GetClass(xType), false);
				}
				else {
					if(xType.equalsIgnoreCase(DataTypes.INTEGER)) {
						cyTable.createColumn(xTitle, GetClass(xType), false, Integer.parseInt(xDefault));
					} else if(xType.equalsIgnoreCase(DataTypes.DOUBLE)) {
						cyTable.createColumn(xTitle, GetClass(xType), false, Double.parseDouble(xDefault));
					} else if(xType.equalsIgnoreCase(DataTypes.FLOAT)) {
						//float not supported
						cyTable.createColumn(xTitle, GetClass(xType), false, Double.parseDouble(xDefault));
					} else if(xType.equalsIgnoreCase(DataTypes.BOOLEAN)) {
						cyTable.createColumn(xTitle, GetClass(xType), false, Boolean.parseBoolean(xDefault));
					} else if(xType.equalsIgnoreCase(DataTypes.STRING)) {
						cyTable.createColumn(xTitle, GetClass(xType), false, xDefault);
					} else if(xType.equalsIgnoreCase(DataTypes.LISTSTRING)) {
						//TODO liststring is crazy and will require special processing to handle
						throw new InvalidClassException(DataTypes.LISTSTRING);
					}
				}

				attMapping.Id.put(xId, xTitle);
				attMapping.Type.put(xId, xType);
			}
		}

		return attMapping;
	}
	
	protected void ParseNode(Node xNode, String expression) throws XPathExpressionException, InvalidClassException {
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
	
	protected void ParseEdges(String defaultEdgeType) throws XPathExpressionException, InvalidClassException {
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
				

				CyEdge cyEdge = _cyNetwork.addEdge(_cyNetwork.getNode(_idMapping.get(xSource)), _cyNetwork.getNode(_idMapping.get(xTarget)), IsDirected(xEdgeType));
				
				if(xElem.hasAttribute(GEXFEdge.EDGETYPE)) {
					_cyNetwork.getRow(cyEdge).set(GEXFEdge.EDGETYPE, xEdgeType);
				}
				
				if(xNode.hasChildNodes()) {
					String attExpression = expression + "[@id='" + xId + "']/attvalues/attvalue";
					
					ParseAttributes(cyEdge, _attEdgeMapping, attExpression);
				}
			}
		}
	}
	
	protected void ParseAttributes(CyIdentifiable cyIdentifiable, AttributeMapping attMapping, String expression) throws XPathExpressionException, InvalidClassException {
		XPath xPath =  XPathFactory.newInstance().newXPath();
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(_doc, XPathConstants.NODESET);

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node xAttNode = nodeList.item(i);
			if (xAttNode.getNodeType() == Node.ELEMENT_NODE) {
				Element xElem = (Element) xAttNode;
				String xFor = xElem.getAttribute(GEXFAttribute.FOR).trim();
				String xValue = xElem.getAttribute(GEXFAttribute.VALUE).trim();

				String type = attMapping.Type.get(xFor);
				if(type.equalsIgnoreCase(DataTypes.INTEGER)) {
					_cyNetwork.getRow(cyIdentifiable).set(attMapping.Id.get(xFor), Integer.parseInt(xValue));
				}
				else if(type.equalsIgnoreCase(DataTypes.DOUBLE)) {
					_cyNetwork.getRow(cyIdentifiable).set(attMapping.Id.get(xFor), Double.parseDouble(xValue));
				}
				else if(type.equalsIgnoreCase(DataTypes.FLOAT)) {
					//float not supported
					_cyNetwork.getRow(cyIdentifiable).set(attMapping.Id.get(xFor), Double.parseDouble(xValue));
				}
				else if(type.equalsIgnoreCase(DataTypes.BOOLEAN)) {
					_cyNetwork.getRow(cyIdentifiable).set(attMapping.Id.get(xFor), Boolean.parseBoolean(xValue));
				}
				else if(type.equalsIgnoreCase(DataTypes.STRING)) {
					_cyNetwork.getRow(cyIdentifiable).set(attMapping.Id.get(xFor), xValue);
				}
				else if(type.equalsIgnoreCase(DataTypes.LISTSTRING)) {
					//TODO liststring is crazy and will require special processing to handle
					throw new InvalidClassException(DataTypes.LISTSTRING);
				}
			}
		}
	}
	
	protected Class GetClass(String type) throws InvalidClassException {
		if(type.equalsIgnoreCase(DataTypes.INTEGER)) {
			return Integer.class;
		}
		else if(type.equalsIgnoreCase(DataTypes.DOUBLE)) {
			return Double.class;
		}
		else if(type.equalsIgnoreCase(DataTypes.FLOAT)) {
			//float not supported
			return Double.class;
		}
		else if(type.equalsIgnoreCase(DataTypes.BOOLEAN)) {
			return Boolean.class;
		}
		else if(type.equalsIgnoreCase(DataTypes.STRING)) {
			return String.class;
		}
		else if(type.equalsIgnoreCase(DataTypes.LISTSTRING)) {
			//TODO liststring is crazy and will require special processing to handle
			throw new InvalidClassException(type);
		}
		else {
			throw new InvalidClassException(type);
		}
	}
	
	protected abstract Boolean IsDirected(String direction);
}