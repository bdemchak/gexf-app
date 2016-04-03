package edu.umuc.swen670.gexf.internal.io;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.io.InvalidClassException;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.GroupTestSupport;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;

import edu.umuc.swen670.gexf.internal.model.GEXFMeta;

public class TestBase {

	protected CyNetwork[] RunFile(InputStream stream) throws Exception {
		CyNetworkFactory cyNetworkFactory = new NetworkTestSupport().getNetworkFactory();
		CyNetworkViewFactory cyNetworkViewFactory = new NetworkViewTestSupport().getNetworkViewFactory();
		CyNetworkManager cyNetworkManager = new NetworkTestSupport().getNetworkManager();
		CyRootNetworkManager cyRootNetworkManager = new NetworkTestSupport().getRootNetworkFactory();
		CyEventHelper cyEventHelper = mock(CyEventHelper.class);
		CyGroupFactory cyGroupFactory = new GroupTestSupport().getGroupFactory();
		CyGroupManager cyGroupManager = new GroupTestSupport().getGroupManager();
		VisualMappingFunctionFactory passthroughMapper = mock(VisualMappingFunctionFactory.class);
		VisualMappingManager visualMappingManager = mock(VisualMappingManager.class);
		
		CyNetworkReader reader = new GEXFNetworkReader(stream, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager, 
													   cyRootNetworkManager, cyEventHelper, cyGroupFactory, cyGroupManager, 
													   passthroughMapper, visualMappingManager);
		
		TaskMonitor monitor = mock(TaskMonitor.class);
		
		reader.run(monitor);
		stream.close();
		
		CyNetwork[] cyNetworks = reader.getNetworks();
		
		return cyNetworks;
	}
	
	protected HashMap<String, Long> BuildNodeMap(CyNetwork cyNetwork) {
		HashMap<String, Long> nodeNameId = new HashMap<String, Long>();
		
		CyTable cyNodeTable = cyNetwork.getDefaultNodeTable();
		List<CyRow> cyNodeRows = cyNodeTable.getAllRows();
		
		for(CyRow cyNodeRow : cyNodeRows) {
			nodeNameId.put(cyNodeRow.get("name", String.class), cyNodeRow.get("SUID", Long.class));
		}
		
		return nodeNameId;
	}
	
	@SuppressWarnings("rawtypes")
	protected void ValidateNodeAttributes(CyNetwork cyNetwork, Long SUID, String[] attributeNames, Class[] attributeTypes, Object[] attributeValues) throws InvalidClassException {
		CyTable cyNodeTable = cyNetwork.getDefaultNodeTable();
		
		ValidateAttributes(cyNodeTable, SUID, attributeNames, attributeTypes, attributeValues);
	}
	
	@SuppressWarnings("rawtypes")
	protected void ValidateEdgeAttributes(CyNetwork cyNetwork, Long sourceSUID, Long targetSUID, String[] attributeNames, Class[] attributeTypes, Object[] attributeValues) throws InvalidClassException {
		List<CyEdge> cyEdges = cyNetwork.getEdgeList();
		for(CyEdge cyEdge : cyEdges) {
			if(cyEdge.getSource().getSUID()==sourceSUID && cyEdge.getTarget().getSUID()==targetSUID) {
				CyTable cyEdgeTable = cyNetwork.getDefaultEdgeTable();
				
				ValidateAttributes(cyEdgeTable, cyEdge.getSUID(), attributeNames, attributeTypes, attributeValues);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void ValidateAttributes(CyTable cyTable, Long SUID, String[] attributeNames, Class[] attributeTypes, Object[] attributeValues) throws InvalidClassException {
		CyRow cyRow = cyTable.getRow(SUID);
		
		for (int i=0; i<attributeNames.length; i++) {
			if(!attributeTypes[i].isArray()) {
				Object value = cyRow.get(attributeNames[i], attributeTypes[i]);
				GenericCompare(value, attributeValues[i], attributeTypes[i]);
			}
			else {
				List values = cyRow.getList(attributeNames[i], attributeTypes[i].getComponentType());
				GenericArrayCompare(values.toArray(), (Object[])attributeValues[i], attributeTypes[i].getComponentType());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> void GenericCompare(Object value1, Object value2, Class<T> type) throws InvalidClassException {
		if(value1==null || value2==null) {
			assertEquals(value1, value2);
		}
		else if(type.equals(Integer.class)) {
			assertEquals((T)value1, (T)value2);
		}
		else if(type.equals(Long.class)) {
			assertEquals((T)value1, (T)value2);
		}
		else if(type.equals(Double.class)) {
			assertEquals((Double)value1, (Double)value2, (Double)0.0001);
		}
		else if(type.equals(Boolean.class)) {
			assertEquals((T)value1, (T)value2);
		}
		else if(type.equals(String.class)) {
			assertEquals((T)value1, (T)value2);
		}
		else {
			throw new InvalidClassException(type.getName());
		}
	}
	
	protected <T> void GenericArrayCompare(Object[] value1, Object[] value2, Class<T> type) throws InvalidClassException {
		assertEquals(value1.length, value2.length);
		
		//do not use arrayAssertEquals because the array could be double[]
		for(int i=0; i<value1.length; i++) {
			GenericCompare(value1[i], value2[i], type);
		}
	}
	
	protected void CheckMeta(CyNetwork cyNetwork, String lastmodifieddate, String creator, String keywords, String description) {
		CyTable cyTable = cyNetwork.getDefaultNetworkTable();
		CyRow cyRow = cyTable.getRow(cyNetwork.getSUID());
		
		if(lastmodifieddate.length()!=0) {
			assertEquals(lastmodifieddate, cyRow.get(GEXFMeta.LASTMODIFIEDDATE, String.class));
		}
		
		if(creator.length()!=0) {
			assertEquals(creator, cyRow.get(GEXFMeta.CREATOR, String.class));
		}
		
		if(keywords.length()!=0) {
			assertEquals(keywords, cyRow.get(GEXFMeta.KEYWORDS, String.class));
		}
		
		if(description.length()!=0) {
			assertEquals(description, cyRow.get(GEXFMeta.DESCRIPTION, String.class));
		}
	}
	
	protected void CheckEdges(CyNetwork cyNetwork, HashMap<Long, List<Long>> edgeMapping, HashMap<String, List<Boolean>> edgeMappingDirected) {
		//check the edges
		List<CyEdge> cyEdges = cyNetwork.getEdgeList();
		for(CyEdge cyEdge : cyEdges) {
			assertEquals(true, edgeMapping.get(cyEdge.getSource().getSUID()).contains(cyEdge.getTarget().getSUID()));
			assertEquals(true, edgeMappingDirected.get(cyEdge.getSource().getSUID().toString() + "," + cyEdge.getTarget().getSUID().toString()).contains(cyEdge.isDirected()));
			
			//remove the items from the arrays in case there is duplication
			assertEquals(true, edgeMapping.get(cyEdge.getSource().getSUID()).remove(cyEdge.getTarget().getSUID()));
			assertEquals(true, edgeMappingDirected.get(cyEdge.getSource().getSUID() + "," + cyEdge.getTarget().getSUID()).remove(cyEdge.isDirected()));
		}
		
		//check that the arrays in the maps are now empty
		ValidateEmptyHashValueArray(edgeMapping);
		ValidateEmptyHashValueArray(edgeMappingDirected);
	}
	
	protected <T,U> void ValidateEmptyHashValueArray(HashMap<T, List<U>> map) {
		for(T key : map.keySet()) {
			assertEquals(0, map.get(key).size());
		}
	}

}
