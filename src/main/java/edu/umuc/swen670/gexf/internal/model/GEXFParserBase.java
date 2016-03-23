package edu.umuc.swen670.gexf.internal.model;

import java.awt.Color;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;

import edu.umuc.swen670.gexf.internal.io.DelayedVizProp;

abstract class GEXFParserBase {

	protected XMLStreamReader _xmlReader = null;
	protected CyNetwork _cyNetwork = null;
	protected String _version = "";
	protected CyGroupFactory _cyGroupFactory = null;
	protected CyGroupManager _cyGroupManager = null;
	
	protected Hashtable<String, Long> _idMapping = new Hashtable<String, Long>();
	protected Hashtable<String, ArrayList<CyNode>> _parentIdToChildrenNodeLookup = new Hashtable<String, ArrayList<CyNode>>();
	protected Hashtable<String, ArrayList<String>> _parentIdToChildrenIdLookup = new Hashtable<String, ArrayList<String>>();
	protected Hashtable<String, ArrayList<CyEdge>> _sourceNodeIdToEdgeLookup = new Hashtable<String, ArrayList<CyEdge>>();
	protected Hashtable<String, ArrayList<CyEdge>> _targetNodeIdToEdgeLookup = new Hashtable<String, ArrayList<CyEdge>>();
	protected AttributeMapping _attNodeMapping = null;
	protected AttributeMapping _attEdgeMapping = null;
	
	protected List<DelayedVizProp> _vizProps = new ArrayList<DelayedVizProp>();
	
	public GEXFParserBase(XMLStreamReader xmlReader, CyNetwork cyNetwork, String version, CyGroupFactory cyGroupFactory, CyGroupManager cyGroupManager) {
		_xmlReader = xmlReader;
		_cyNetwork = cyNetwork;
		_version = version;
		_cyGroupFactory = cyGroupFactory;
		_cyGroupManager = cyGroupManager;
	}
	
	public abstract List<DelayedVizProp> ParseStream() throws IOException, XMLStreamException;
	
	protected void ParseMeta() throws InvalidClassException, XMLStreamException {
		CyTable cyTable = _cyNetwork.getDefaultNetworkTable();
		CyRow cyRow = cyTable.getRow(_cyNetwork.getSUID());
				
		List<String> attributes = GetElementAttributes();
		if(attributes.contains(GEXFMeta.LASTMODIFIEDDATE)) {
			if(cyTable.getColumn(GEXFMeta.LASTMODIFIEDDATE)==null) {
				cyTable.createColumn(GEXFMeta.LASTMODIFIEDDATE, String.class, false);
			}
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
					
					if(cyTable.getColumn(_xmlReader.getLocalName().trim().toLowerCase())==null) {
						cyTable.createColumn(_xmlReader.getLocalName().trim().toLowerCase(), String.class, false);
					}
					if(tagContent!=null) {cyRow.set(_xmlReader.getLocalName().trim().toLowerCase(), tagContent.trim());}
					
					tagContent = null;
					
					break;
				}
				else {
					throw new InvalidClassException(_xmlReader.getLocalName().trim());
				}
			case XMLStreamConstants.START_ELEMENT :
				if(_xmlReader.getLocalName().trim().equalsIgnoreCase(GEXFMeta.CREATOR) || 
						_xmlReader.getLocalName().equalsIgnoreCase(GEXFMeta.DESCRIPTION) || 
						_xmlReader.getLocalName().equalsIgnoreCase(GEXFMeta.KEYWORDS)) {
					//this will be null in cases where the value is contained in the character stream
					tagContent = _xmlReader.getAttributeValue(null, "text");
				}
				break;
			case XMLStreamConstants.CHARACTERS :
				if(_xmlReader.getText().trim().length() > 0) {
					tagContent = _xmlReader.getText();
				}
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
					xType = _xmlReader.getAttributeValue(null, GEXFAttribute.TYPE).trim();
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFAttribute.DEFAULT)) {
					hasDefault = true;
					xDefault = _xmlReader.getAttributeValue(null, "text");
				}
				break;
			case XMLStreamConstants.CHARACTERS :
				if(hasDefault && xDefault == null) {
					xDefault = _xmlReader.getText();
				}
				break;
			case XMLStreamConstants.END_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFAttribute.ATTRIBUTES)) {
					return attMapping;
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFAttribute.ATTRIBUTE)) {
					Class type = GetClass(xType);
					
					if(cyTable.getColumn(xTitle)==null) {
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
								cyTable.createColumn(xTitle, type, false, GenericParse(xDefault.trim(), type));
							}
							else {
								cyTable.createListColumn(xTitle, type.getComponentType(), false, ParseArray(xDefault.trim(), type.getComponentType()));
							}
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
	
	protected ArrayList<String> ParseNodes(CyNode cyNodeParent) throws IOException, XMLStreamException {
		
		ArrayList<CyNode> cyNodes = new ArrayList<CyNode>();
		ArrayList<String> cyNodeIds = new ArrayList<String>();
		CyNode cyNode = null;
		String cyNodeId = null;
		
//		Hashtable<String, ArrayList<CyNode>> parentIdToChildrenLookup = new Hashtable<String, ArrayList<CyNode>>();
		
		while(_xmlReader.hasNext()) {
			int event = _xmlReader.next();

			switch(event) {
			case XMLStreamConstants.END_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFNode.NODES)) {
//					if (cyNodeParent == null) {
//						Enumeration<String> pidEnumeration = parentIdToChildrenLookup.keys();
//						while(pidEnumeration.hasMoreElements()) {
//							String pid = pidEnumeration.nextElement();
//							CyNode parentNode = _cyNetwork.getNode(_idMapping.get(pid));
//							
//							CyGroup newGroup = _cyGroupFactory.createGroup(_cyNetwork, parentNode, parentIdToChildrenLookup.get(pid), null, true);
//							_cyGroupManager.addGroup(newGroup);
//						}
//					}
					return cyNodeIds;
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFNode.NODE)) {
					cyNodes.add(cyNode);
					cyNodeIds.add(cyNodeId);
					cyNode = null;
					cyNodeId = null;
				}
				break;
			case XMLStreamConstants.START_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFNode.NODE)) {
					String xId = _xmlReader.getAttributeValue(null, GEXFNode.ID).trim();
					String xLabel = _xmlReader.getAttributeValue(null, GEXFNode.LABEL).trim();
					String xPid = _xmlReader.getAttributeValue(null, GEXFNode.PID);
					if (xPid != null) {
						xPid = xPid.trim();
					}
					
					if(!_idMapping.containsKey(xId)) {
						cyNode = _cyNetwork.addNode();
						_idMapping.put(xId, cyNode.getSUID());
					}
					else {
						cyNode = _cyNetwork.getNode(_idMapping.get(xId));
					}
					cyNodeId = xId;
					
					if(xPid != null) {
						if(!_parentIdToChildrenNodeLookup.containsKey(xPid)) {
							_parentIdToChildrenNodeLookup.put(xPid, new ArrayList<CyNode>());
						}
						if(!_parentIdToChildrenIdLookup.containsKey(xPid)) {
							_parentIdToChildrenIdLookup.put(xPid, new ArrayList<String>());
						}
						ArrayList<CyNode> childrenForPid = (ArrayList<CyNode>)_parentIdToChildrenNodeLookup.get(xPid);
						childrenForPid.add(cyNode);
						
						ArrayList<String> childrenIdsForPid = (ArrayList<String>)_parentIdToChildrenIdLookup.get(xPid);
						childrenIdsForPid.add(xId);
					}					
					
					_cyNetwork.getRow(cyNode).set(CyNetwork.NAME, xLabel);
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFNode.NODES)) { 
					ArrayList<String> nodesToAddToGroup = ParseNodes(cyNode);
					//_parentIdToChildrenNodeLookup.put(cyNodeId, nodesToAddToGroup);
					_parentIdToChildrenIdLookup.put(cyNodeId, nodesToAddToGroup);
					
//					if (cyNode != null) {
//						CyGroup newGroup = _cyGroupFactory.createGroup(_cyNetwork, cyNode, nodesToAddToGroup, null, true);
//						_cyGroupManager.addGroup(newGroup);
//					}
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFAttribute.ATTVALUES)) {
					ParseAttributes(new CyIdentifiable[] {cyNode}, _attNodeMapping);
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFViz.COLOR)) {
					int red = Integer.parseInt(_xmlReader.getAttributeValue(null, GEXFViz.RED).trim());
					int green = Integer.parseInt(_xmlReader.getAttributeValue(null, GEXFViz.GREEN).trim());
					int blue = Integer.parseInt(_xmlReader.getAttributeValue(null, GEXFViz.BLUE).trim());
					int alpha = GetElementAttributes().contains(GEXFViz.ALPHA) ? (int)(255 * Float.parseFloat(_xmlReader.getAttributeValue(null, GEXFViz.ALPHA).trim())) : 255;
					Color color = new Color(red, green, blue, alpha);
					
					_vizProps.add(new DelayedVizProp(cyNode, BasicVisualLexicon.NODE_FILL_COLOR, color, true));
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFViz.POSITION)) {
					List<String> elementAttributes = GetElementAttributes();
					
					double x = elementAttributes.contains(GEXFViz.X) ? Double.parseDouble(_xmlReader.getAttributeValue(null, GEXFViz.X).trim()) : 0.0d;
					double y = elementAttributes.contains(GEXFViz.Y) ? -Double.parseDouble(_xmlReader.getAttributeValue(null, GEXFViz.Y).trim()) : 0.0d;
					double z = elementAttributes.contains(GEXFViz.Z) ? Double.parseDouble(_xmlReader.getAttributeValue(null, GEXFViz.Z).trim()) : 0.0d;
					
					if(elementAttributes.contains(GEXFViz.X)) {_vizProps.add(new DelayedVizProp(cyNode, BasicVisualLexicon.NODE_X_LOCATION, x, false));}
					if(elementAttributes.contains(GEXFViz.Y)) {_vizProps.add(new DelayedVizProp(cyNode, BasicVisualLexicon.NODE_Y_LOCATION, y, false));}
					if(elementAttributes.contains(GEXFViz.Z)) {_vizProps.add(new DelayedVizProp(cyNode, BasicVisualLexicon.NODE_Z_LOCATION, z, false));}
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFViz.SIZE)) {
					double value = Double.parseDouble(_xmlReader.getAttributeValue(null, GEXFViz.VALUE).trim());
					
					_vizProps.add(new DelayedVizProp(cyNode, BasicVisualLexicon.NODE_SIZE, value, true));
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFViz.SHAPE)) {
					String value = _xmlReader.getAttributeValue(null, GEXFViz.VALUE).trim();
					
					_vizProps.add(new DelayedVizProp(cyNode, BasicVisualLexicon.NODE_SHAPE, ConvertNodeShape(value), true));
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
					Enumeration<String> pidEnumeration = _parentIdToChildrenIdLookup.keys();//_parentIdToChildrenNodeLookup.keys();
					while(pidEnumeration.hasMoreElements()) {
						String pid = pidEnumeration.nextElement();
						CyNode parentNode = _cyNetwork.getNode(_idMapping.get(pid));
						
						ArrayList<CyEdge> sourceEdgeList = _sourceNodeIdToEdgeLookup.get(pid);
						ArrayList<CyEdge> targetEdgeList = _targetNodeIdToEdgeLookup.get(pid);
						ArrayList<CyEdge> combinedEdgeList = new ArrayList<CyEdge>();
						if (sourceEdgeList != null) {
							combinedEdgeList.addAll(sourceEdgeList);
						}
						if (targetEdgeList != null) {
							combinedEdgeList.addAll(targetEdgeList);
						}
						
						ArrayList<CyNode> allDescendantsOfNode = getDescendantsOfCyNodeById(pid);
						
						CyGroup newGroup = _cyGroupFactory.createGroup(_cyNetwork, parentNode, allDescendantsOfNode, null, true);//CyGroup newGroup = _cyGroupFactory.createGroup(_cyNetwork, parentNode, allDescendantsOfNode, combinedEdgeList, true);
						_cyGroupManager.addGroup(newGroup);
					}
					
					return;
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFEdge.EDGE)) {
//					AddCyEdgeToGroupIfAble(cyEdge);
//					AddCyEdgeToGroupIfAble(cyEdgeReverse);
					
					cyEdge = null;
					cyEdgeReverse = null;
				}
				
				break;
			case XMLStreamConstants.START_ELEMENT :
				if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFEdge.EDGE)) {
					List<String> edgeElementAttributes = GetElementAttributes();
					
					//String xId = _xmlReader.getAttributeValue(null, GEXFEdge.ID).trim();
					String xSource = _xmlReader.getAttributeValue(null, GEXFEdge.SOURCE).trim();
					String xTarget = _xmlReader.getAttributeValue(null, GEXFEdge.TARGET).trim();
					String xEdgeType = edgeElementAttributes.contains(GEXFEdge.EDGETYPE) ? _xmlReader.getAttributeValue(null, GEXFEdge.EDGETYPE).trim() : defaultEdgeType;
					String xEdgeWeight = edgeElementAttributes.contains(GEXFEdge.WEIGHT) ? _xmlReader.getAttributeValue(null, GEXFEdge.WEIGHT).trim() : "";
					
					if(!_idMapping.containsKey(xSource)) {
						CyNode cyNode = _cyNetwork.addNode();
						_idMapping.put(xSource, cyNode.getSUID());
					}
					
					if(!_idMapping.containsKey(xTarget)) {
						CyNode cyNode = _cyNetwork.addNode();
						_idMapping.put(xTarget, cyNode.getSUID());
					}
					
					cyEdge = _cyNetwork.addEdge(_cyNetwork.getNode(_idMapping.get(xSource)), _cyNetwork.getNode(_idMapping.get(xTarget)), IsDirected(xEdgeType));
					cyEdgeReverse = IsBiDirectional(xEdgeType) ? _cyNetwork.addEdge(_cyNetwork.getNode(_idMapping.get(xTarget)), _cyNetwork.getNode(_idMapping.get(xSource)), IsDirected(xEdgeType)) : null;
					
					_cyNetwork.getRow(cyEdge).set(GEXFEdge.EDGETYPE, xEdgeType);
					if(cyEdgeReverse!=null) _cyNetwork.getRow(cyEdgeReverse).set(GEXFEdge.EDGETYPE, xEdgeType);
					
					if(edgeElementAttributes.contains(GEXFEdge.WEIGHT)) {
						_cyNetwork.getRow(cyEdge).set(GEXFEdge.WEIGHT, Double.parseDouble(xEdgeWeight));
						if(cyEdgeReverse!=null) _cyNetwork.getRow(cyEdgeReverse).set(GEXFEdge.WEIGHT, Double.parseDouble(xEdgeWeight));
					}
					
					if (!_sourceNodeIdToEdgeLookup.containsKey(xSource)) {
						_sourceNodeIdToEdgeLookup.put(xSource, new ArrayList<CyEdge>());
					}
					ArrayList<CyEdge> tempEdgeList = _sourceNodeIdToEdgeLookup.get(xSource);
					tempEdgeList.add(cyEdge);
					
					if (!_targetNodeIdToEdgeLookup.containsKey(xTarget)) {
						_targetNodeIdToEdgeLookup.put(xTarget, new ArrayList<CyEdge>());
					}
					tempEdgeList = _targetNodeIdToEdgeLookup.get(xTarget);
					tempEdgeList.add(cyEdge);
					
					if (cyEdgeReverse != null) {
						if (!_targetNodeIdToEdgeLookup.containsKey(xSource)) {
							_targetNodeIdToEdgeLookup.put(xSource, new ArrayList<CyEdge>());
						}
						tempEdgeList = _targetNodeIdToEdgeLookup.get(xSource);
						tempEdgeList.add(cyEdgeReverse);
						
						if (!_sourceNodeIdToEdgeLookup.containsKey(xTarget)) {
							_sourceNodeIdToEdgeLookup.put(xTarget, new ArrayList<CyEdge>());
						}
						tempEdgeList = _sourceNodeIdToEdgeLookup.get(xTarget);
						tempEdgeList.add(cyEdgeReverse);
					}
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFAttribute.ATTVALUES)) {
					ParseAttributes(new CyIdentifiable[] {cyEdge, cyEdgeReverse}, _attEdgeMapping);
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFViz.COLOR)) {
					int red = Integer.parseInt(_xmlReader.getAttributeValue(null, GEXFViz.RED).trim());
					int green = Integer.parseInt(_xmlReader.getAttributeValue(null, GEXFViz.GREEN).trim());
					int blue = Integer.parseInt(_xmlReader.getAttributeValue(null, GEXFViz.BLUE).trim());
					int alpha = GetElementAttributes().contains(GEXFViz.ALPHA) ? (int)(255 * Float.parseFloat(_xmlReader.getAttributeValue(null, GEXFViz.ALPHA).trim())) : 255;
					Color color = new Color(red, green, blue, alpha);
					
					_vizProps.add(new DelayedVizProp(cyEdge, BasicVisualLexicon.EDGE_UNSELECTED_PAINT, color, true));
					if(cyEdgeReverse!=null) {_vizProps.add(new DelayedVizProp(cyEdgeReverse, BasicVisualLexicon.EDGE_UNSELECTED_PAINT, color, true));}
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFViz.THICKNESS)) {
					double value = Double.parseDouble(_xmlReader.getAttributeValue(null, GEXFViz.VALUE).trim());
					
					_vizProps.add(new DelayedVizProp(cyEdge, BasicVisualLexicon.EDGE_WIDTH, value, true));
					if(cyEdgeReverse!=null) {_vizProps.add(new DelayedVizProp(cyEdgeReverse, BasicVisualLexicon.EDGE_WIDTH, value, true));}
				}
				else if(_xmlReader.getLocalName().equalsIgnoreCase(GEXFViz.SHAPE)) {
					String value = _xmlReader.getAttributeValue(null, GEXFViz.VALUE).trim();
					
					_vizProps.add(new DelayedVizProp(cyEdge, BasicVisualLexicon.EDGE_LINE_TYPE, ConvertEdgeShape(value), true));
					if(cyEdgeReverse!=null) {_vizProps.add(new DelayedVizProp(cyEdgeReverse, BasicVisualLexicon.EDGE_LINE_TYPE, ConvertEdgeShape(value), true));}
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
					String xFor = _xmlReader.getAttributeValue(null, GEXFAttribute.FOR);
					if(xFor==null) {xFor = _xmlReader.getAttributeValue(null, GEXFAttribute.ID);}
					xFor = xFor.trim();					
					String xValue = _xmlReader.getAttributeValue(null, GEXFAttribute.VALUE).trim();
					
					Class type = GetClass(attMapping.Type.get(xFor));
					if(!type.isArray()) {
						for(CyIdentifiable cyIdentifiable : cyIdentifiables) {
							if(cyIdentifiable!=null) {_cyNetwork.getRow(cyIdentifiable).set(attMapping.Id.get(xFor), GenericParse(xValue, type));}
						}
					}
					else {
						for(CyIdentifiable cyIdentifiable : cyIdentifiables) {
							if(cyIdentifiable!=null) {_cyNetwork.getRow(cyIdentifiable).set(attMapping.Id.get(xFor), ParseArray(xValue, type));}
						}
					}
				}
				break;
			}
		}
		
		
		throw new InvalidClassException("Missing Attribute Value tags");
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
	
	protected NodeShape ConvertNodeShape(String shape) {
		if(shape.equalsIgnoreCase(GEXFViz.DISC)) {
			return NodeShapeVisualProperty.ELLIPSE;
		}
		else if(shape.equalsIgnoreCase(GEXFViz.SQUARE)) {
			return NodeShapeVisualProperty.RECTANGLE;
		}
		else if(shape.equalsIgnoreCase(GEXFViz.TRIANGLE)) {
			return NodeShapeVisualProperty.TRIANGLE;
		}
		else if(shape.equalsIgnoreCase(GEXFViz.DIAMOND)) {
			return NodeShapeVisualProperty.DIAMOND;
		}
		else if(shape.equalsIgnoreCase(GEXFViz.IMAGE)) {
			return NodeShapeVisualProperty.OCTAGON;
		}
		else {
			return NodeShapeVisualProperty.ROUND_RECTANGLE;
		}
	}
	
	protected LineType ConvertEdgeShape(String shape) {
		if(shape.equalsIgnoreCase(GEXFViz.SOLID)) {
			return LineTypeVisualProperty.SOLID;
		}
		else if(shape.equalsIgnoreCase(GEXFViz.DOTTED)) {
			return LineTypeVisualProperty.DOT;
		}
		else if(shape.equalsIgnoreCase(GEXFViz.DASHED)) {
			return LineTypeVisualProperty.EQUAL_DASH;
		}
		else if(shape.equalsIgnoreCase(GEXFViz.DOUBLE)) {
			return LineTypeVisualProperty.DASH_DOT;
		}
		else {
			return LineTypeVisualProperty.SOLID;
		}
	}
	
//	protected void AddCyEdgeToGroupIfAble(CyEdge edgeToAdd) {
//		if (edgeToAdd == null) {
//			return;
//		}
//		
//		ArrayList<CyEdge> tempEdgeList = new ArrayList<CyEdge>();
//		tempEdgeList.add(edgeToAdd);
//		
//		CyNode sourceNode = edgeToAdd.getSource();
//		CyNode targetNode = edgeToAdd.getTarget();
//		
//		CyGroup sourceNodeGroup = _cyGroupManager.getGroup(sourceNode, _cyNetwork);
//		CyGroup targetNodeGroup = _cyGroupManager.getGroup(targetNode, _cyNetwork);
//		
////		if (sourceNodeGroup != null && targetNodeGroup != null && sourceNodeGroup.getGroupNode().getSUID() == targetNodeGroup.getGroupNode().getSUID()) {
////			ArrayList<CyEdge> tempEdgeList = new ArrayList<CyEdge>();
////			tempEdgeList.add(edgeToAdd);
////			sourceNodeGroup.addEdges(tempEdgeList);
////		}
//		if (sourceNodeGroup != null) {
//			sourceNodeGroup.addEdges(tempEdgeList);
//		}
//		
//		if (targetNodeGroup != null) {
//			targetNodeGroup.addEdges(tempEdgeList);
//		}
//	}
	
	protected ArrayList<CyNode> getDescendantsOfCyNodeById(String pid) {
		ArrayList<CyNode> result = new ArrayList<CyNode>();
		if (!_parentIdToChildrenIdLookup.containsKey(pid)) {
			return null;
		}
		ArrayList<String> childIds = _parentIdToChildrenIdLookup.get(pid);
		for (int i=0; i<childIds.size(); i++) {
			ArrayList<CyNode> childNodes = getDescendantsOfCyNodeById(childIds.get(i));
			if (childNodes != null) {
				result.add(_cyNetwork.getNode(_idMapping.get(childIds.get(i))));
				result.addAll(childNodes);
			} else {
				result.add(_cyNetwork.getNode(_idMapping.get(childIds.get(i))));
			}
		}
		return result;
	}
	
	protected abstract Boolean IsDirected(String direction);
	
	protected abstract Boolean IsBiDirectional(String direction);
}