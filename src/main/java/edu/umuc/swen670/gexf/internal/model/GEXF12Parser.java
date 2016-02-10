package edu.umuc.swen670.gexf.internal.model;

import java.io.IOException;
import java.io.InputStream;
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
	
	public void ParseNodes(InputStream inputStream, CyNetwork cyNetwork) {

		Hashtable<String, Long> idMapping = new Hashtable<String, Long>();
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder;
	
			dBuilder = dbFactory.newDocumentBuilder();
			
			Document doc = dBuilder.parse(inputStream);
			
			doc.getDocumentElement().normalize();

	         XPath xPath =  XPathFactory.newInstance().newXPath();

	         String expression = "/gexf/graph/nodes/node";
	         NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
	         for (int i = 0; i < nodeList.getLength(); i++) {
	            Node xNode = nodeList.item(i);
	            
	            if (xNode.getNodeType() == Node.ELEMENT_NODE) {
	            	Element xElem = (Element) xNode;
	            	
	            	String xLabel = xElem.getAttribute("label");
	            	String xId = xElem.getAttribute("id");
	            	
	            	CyNode cyNode = cyNetwork.addNode();
	            	cyNetwork.getRow(cyNode).set(CyNetwork.NAME, xLabel);

	            	idMapping.put(xId, cyNode.getSUID());
	            }
	         }
	         
	         expression = "/gexf/graph/edges/edge";
	         nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
	         for (int i = 0; i < nodeList.getLength(); i++) {
	            Node xNode = nodeList.item(i);
	            
	            if (xNode.getNodeType() == Node.ELEMENT_NODE) {
	            	Element xElem = (Element) xNode;
	            	
	            	String xSource = xElem.getAttribute("source");
	            	String xTarget = xElem.getAttribute("target");
	            	String xId = xElem.getAttribute("id");
	            	
	            	//TODO get the graph type instead of assuming that the graph is directed
	            	cyNetwork.addEdge(cyNetwork.getNode(idMapping.get(xSource)), cyNetwork.getNode(idMapping.get(xTarget)), true);
	            }
	         }

		}
		catch(ParserConfigurationException e) {
			_logger.error(e.toString());
		} catch (SAXException e) {
			_logger.error(e.toString());
		} catch (IOException e) {
			_logger.error(e.toString());
		} catch (XPathExpressionException e) {
			_logger.error(e.toString());
		}
	}
	
}
