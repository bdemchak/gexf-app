package edu.umuc.swen670.gexf.internal.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.umuc.swen670.gexf.internal.io.GEXFFileFilter;

public class GEXF12Parser {
	
	private static final Logger _logger = LoggerFactory.getLogger(GEXF12Parser.class);
	
	public void ParseStream(InputStream inputStream, CyNetwork cyNetwork) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {

		Hashtable<String, Long> idMapping = new Hashtable<String, Long>();
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;

		dBuilder = dbFactory.newDocumentBuilder();
		
		Document doc = dBuilder.parse(inputStream);
		
		doc.getDocumentElement().normalize();
		
		AttributeMapping attMapping;
		attMapping = ParseNodeAttributesHeader(doc, cyNetwork);
		

         XPath xPath =  XPathFactory.newInstance().newXPath();

         String expression = "/gexf/graph/nodes/node";
         NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
         for (int i = 0; i < nodeList.getLength(); i++) {
            Node xNode = nodeList.item(i);
            
            ParseNode(xNode, cyNetwork, idMapping, attMapping, doc, expression);
         }
         
         ParseEdges(doc, cyNetwork, idMapping);
	}
	
	private AttributeMapping ParseNodeAttributesHeader(Document doc, CyNetwork cyNetwork) throws XPathExpressionException, InvalidClassException {
		AttributeMapping attMapping = new AttributeMapping();
		
		CyTable cyTable = cyNetwork.getDefaultNodeTable();
		
		XPath xPath =  XPathFactory.newInstance().newXPath();
		String expression = "/gexf/graph/attributes[@class='node']/attribute";
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
           Node xNode = nodeList.item(i);
           
           if (xNode.getNodeType() == Node.ELEMENT_NODE) {
              Element xElem = (Element) xNode;
           	
              String xId = xElem.getAttribute("id");
              String xTitle = xElem.getAttribute("title");
              String xType = xElem.getAttribute("type");
              String xDefault = null;
              
              if(xNode.hasChildNodes()) {
            	  NodeList childNodes = xNode.getChildNodes();
            	  for(int j=0; j< childNodes.getLength(); j++) {
            		  Node childNode = childNodes.item(j);
            		  if(childNode.getNodeName().equalsIgnoreCase("default")) {
            			  xDefault = childNode.getNodeValue().trim();
            		  }
            	  }
              }
              
              if(xDefault == null || xDefault.length() == 0) {
            	  cyTable.createColumn(xTitle, GetClass(xType), false);
              }
              else {
            	  cyTable.createColumn(xTitle, GetClass(xType), false, xDefault);
              }
              
              attMapping.Id.put(xId, xTitle);
              attMapping.Type.put(xId, xType);
           }
        }
        
        return attMapping;
	}
	
	private void ParseNode(Node xNode, CyNetwork cyNetwork, Hashtable<String, Long> idMapping, AttributeMapping attMapping, Document doc, String expression) throws XPathExpressionException, InvalidClassException {
		if (xNode.getNodeType() == Node.ELEMENT_NODE) {
        	Element xElem = (Element) xNode;
        	
        	String xLabel = xElem.getAttribute("label");
        	String xId = xElem.getAttribute("id");
        	
        	CyNode cyNode = cyNetwork.addNode();
        	cyNetwork.getRow(cyNode).set(CyNetwork.NAME, xLabel);

        	idMapping.put(xId, cyNode.getSUID());
        	
        	if(xNode.hasChildNodes()) {
               String attExpression = expression + "[@id='" + xId + "']/attvalues/attvalue";
               
               ParseNodeAttributes(cyNode, cyNetwork, attMapping, doc, attExpression);
            }
        }
	}
	
	private void ParseNodeAttributes(CyNode cyNode, CyNetwork cyNetwork, AttributeMapping attMapping, Document doc, String expression) throws XPathExpressionException, InvalidClassException {
        XPath xPath =  XPathFactory.newInstance().newXPath();
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        
        for (int i = 0; i < nodeList.getLength(); i++) {
             Node xAttNode = nodeList.item(i);
             if (xAttNode.getNodeType() == Node.ELEMENT_NODE) {
            	 Element xElem = (Element) xAttNode;
            	 String xFor = xElem.getAttribute("for");
            	 String xValue = xElem.getAttribute("value");
            	 
            	 if(attMapping.Type.get(xFor) == "integer") {
            		 cyNetwork.getRow(cyNode).set(attMapping.Id.get(xFor), Integer.parseInt(xValue));
            	 }
            	 else if(attMapping.Type.get(xFor) == "double") {
            		 cyNetwork.getRow(cyNode).set(attMapping.Id.get(xFor), Double.parseDouble(xValue));
            	 }
            	 else if(attMapping.Type.get(xFor) == "float") {
            		 cyNetwork.getRow(cyNode).set(attMapping.Id.get(xFor), Float.parseFloat(xValue));
            	 }
            	 else if(attMapping.Type.get(xFor) == "boolean") {
            		 cyNetwork.getRow(cyNode).set(attMapping.Id.get(xFor), Boolean.parseBoolean(xValue));
            	 }
            	 else if(attMapping.Type.get(xFor) == "string") {
            		 cyNetwork.getRow(cyNode).set(attMapping.Id.get(xFor), xValue);
            	 }
            	 else if(attMapping.Type.get(xFor) == "liststring") {
            		//TODO liststring is crazy and will require special processing to handle
         			throw new InvalidClassException("liststring");
            	 }
             }
        }
	}
	
	private void ParseEdges(Document doc, CyNetwork cyNetwork, Hashtable<String, Long> idMapping) throws XPathExpressionException {
		XPath xPath =  XPathFactory.newInstance().newXPath();
		String expression = "/gexf/graph/edges/edge";
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
           Node xNode = nodeList.item(i);
           
           if (xNode.getNodeType() == Node.ELEMENT_NODE) {
           	Element xElem = (Element) xNode;
           	
           	String xSource = xElem.getAttribute("source").trim();
           	String xTarget = xElem.getAttribute("target").trim();
           	String xId = xElem.getAttribute("id").trim();
           	
           	//TODO get the graph type instead of assuming that the graph is directed
           	cyNetwork.addEdge(cyNetwork.getNode(idMapping.get(xSource)), cyNetwork.getNode(idMapping.get(xTarget)), true);
           }
        }
	}
	
	private Class GetClass(String type) throws InvalidClassException {
		if(type.equalsIgnoreCase("integer")) {
			return Integer.class;
		}
		else if(type.equalsIgnoreCase("double")) {
			return Double.class;
		}
		else if(type.equalsIgnoreCase("float")) {
			return Float.class;
		}
		else if(type.equalsIgnoreCase("boolean")) {
			return Boolean.class;
		}
		else if(type.equalsIgnoreCase("string")) {
			return String.class;
		}
		else if(type.equalsIgnoreCase("liststring")) {
			//TODO liststring is crazy and will require special processing to handle
			throw new InvalidClassException(type);
		}
		else {
			throw new InvalidClassException(type);
		}
	}
	
}
