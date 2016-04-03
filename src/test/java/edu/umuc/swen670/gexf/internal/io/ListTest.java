package edu.umuc.swen670.gexf.internal.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.junit.Test;

public class ListTest extends TestBase {

	@Test
	public void ParseListString12() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/liststring12.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		//check the counts
		assertEquals(4, cyNetwork.getNodeCount());
		assertEquals(1, cyNetwork.getEdgeCount());
		
		
		
		//build the node map
		HashMap<String, Long> nodeNameId = BuildNodeMap(cyNetwork);
		
		//check the nodes
		assertEquals(true, nodeNameId.containsKey("NodeOne"));
		assertEquals(true, nodeNameId.containsKey("NodeTwo"));
		assertEquals(true, nodeNameId.containsKey("NodeThree"));
		assertEquals(true, nodeNameId.containsKey("NodeFour"));
		
		
		//build the edge map
		HashMap<Long, List<Long>> edgeMapping = new HashMap<Long, List<Long>>();
		edgeMapping.put(nodeNameId.get("NodeOne"), new ArrayList(Arrays.asList(nodeNameId.get("NodeTwo"))));
		
		HashMap<String, List<Boolean>> edgeMappingDirected = new HashMap<String, List<Boolean>>();
		edgeMappingDirected.put(nodeNameId.get("NodeOne").toString() + "," + nodeNameId.get("NodeTwo").toString(), new ArrayList(Arrays.asList(true)));
		
		CheckEdges(cyNetwork, edgeMapping, edgeMappingDirected);
		
		
		//check the node attributes
		String[] names = new String[]{"integer-test", "liststring-test"};
		Class[] types = new Class[] {Integer.class, String[].class};
		
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeOne"), names, types, new Object[] {5, new String[]{"foo","bar"}});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeTwo"), names, types, new Object[] {10, new String[]{"bar"}});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeThree"), names, types, new Object[] {15, new String[]{"foo","bar"}});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeFour"), names, types, new Object[] {20, new String[]{"foo","bar"}});
	}
	
	@Test
	public void ParseListString13() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/liststring13.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		//check the counts
		assertEquals(3, cyNetwork.getNodeCount());
		assertEquals(1, cyNetwork.getEdgeCount());
		
		
		
		//build the node map
		HashMap<String, Long> nodeNameId = BuildNodeMap(cyNetwork);
		
		//check the nodes
		assertEquals(true, nodeNameId.containsKey("NodeOne"));
		assertEquals(true, nodeNameId.containsKey("NodeTwo"));
		assertEquals(true, nodeNameId.containsKey("NodeThree"));
		
		
		//build the edge map
		HashMap<Long, List<Long>> edgeMapping = new HashMap<Long, List<Long>>();
		edgeMapping.put(nodeNameId.get("NodeOne"), new ArrayList(Arrays.asList(nodeNameId.get("NodeTwo"))));
		
		HashMap<String, List<Boolean>> edgeMappingDirected = new HashMap<String, List<Boolean>>();
		edgeMappingDirected.put(nodeNameId.get("NodeOne").toString() + "," + nodeNameId.get("NodeTwo").toString(), new ArrayList(Arrays.asList(true)));
		
		CheckEdges(cyNetwork, edgeMapping, edgeMappingDirected);
		
		
		//check the node attributes
		String[] names = new String[]{"integer-test", "liststring-test"};
		Class[] types = new Class[] {Integer.class, String[].class};
		
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeOne"), names, types, new Object[] {5, new String[]{"foo","bar"}});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeTwo"), names, types, new Object[] {10, new String[]{"bar"}});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeThree"), names, types, new Object[] {15, new String[]{"embedded space","bar"}});
	}
	
	@Test
	public void ParseListBoolean12() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/listboolean12.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		//check the counts
		assertEquals(2, cyNetwork.getNodeCount());
		assertEquals(1, cyNetwork.getEdgeCount());
		
		
		
		//build the node map
		HashMap<String, Long> nodeNameId = BuildNodeMap(cyNetwork);
		
		//check the nodes
		assertEquals(true, nodeNameId.containsKey("NodeOne"));
		assertEquals(true, nodeNameId.containsKey("NodeTwo"));
		
		
		//build the edge map
		HashMap<Long, List<Long>> edgeMapping = new HashMap<Long, List<Long>>();
		edgeMapping.put(nodeNameId.get("NodeOne"), new ArrayList(Arrays.asList(nodeNameId.get("NodeTwo"))));
		
		HashMap<String, List<Boolean>> edgeMappingDirected = new HashMap<String, List<Boolean>>();
		edgeMappingDirected.put(nodeNameId.get("NodeOne").toString() + "," + nodeNameId.get("NodeTwo").toString(), new ArrayList(Arrays.asList(true)));
		
		CheckEdges(cyNetwork, edgeMapping, edgeMappingDirected);
		
		
		//check the node attributes
		String[] names = new String[]{"integer-test", "listboolean-test"};
		Class[] types = new Class[] {Integer.class, Boolean[].class};
		
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeOne"), names, types, new Object[] {5, new Boolean[]{true,false}});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeTwo"), names, types, new Object[] {10, new Boolean[]{false}});
	}
	
	@Test
	public void ParseListDouble12() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/listdouble12.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		//check the counts
		assertEquals(2, cyNetwork.getNodeCount());
		assertEquals(1, cyNetwork.getEdgeCount());
		
		
		
		//build the node map
		HashMap<String, Long> nodeNameId = BuildNodeMap(cyNetwork);
		
		//check the nodes
		assertEquals(true, nodeNameId.containsKey("NodeOne"));
		assertEquals(true, nodeNameId.containsKey("NodeTwo"));
		
		
		//build the edge map
		HashMap<Long, List<Long>> edgeMapping = new HashMap<Long, List<Long>>();
		edgeMapping.put(nodeNameId.get("NodeOne"), new ArrayList(Arrays.asList(nodeNameId.get("NodeTwo"))));
		
		HashMap<String, List<Boolean>> edgeMappingDirected = new HashMap<String, List<Boolean>>();
		edgeMappingDirected.put(nodeNameId.get("NodeOne").toString() + "," + nodeNameId.get("NodeTwo").toString(), new ArrayList(Arrays.asList(true)));
		
		CheckEdges(cyNetwork, edgeMapping, edgeMappingDirected);
		
		
		//check the node attributes
		String[] names = new String[]{"integer-test", "listdouble-test"};
		Class[] types = new Class[] {Integer.class, Double[].class};
		
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeOne"), names, types, new Object[] {5, new Double[]{24.2d,36.9d}});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeTwo"), names, types, new Object[] {10, new Double[]{44.0}});
	}
	
	@Test
	public void ParseListFloat12() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/listfloat12.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		//check the counts
		assertEquals(2, cyNetwork.getNodeCount());
		assertEquals(1, cyNetwork.getEdgeCount());
		
		
		
		//build the node map
		HashMap<String, Long> nodeNameId = BuildNodeMap(cyNetwork);
		
		//check the nodes
		assertEquals(true, nodeNameId.containsKey("NodeOne"));
		assertEquals(true, nodeNameId.containsKey("NodeTwo"));
		
		
		//build the edge map
		HashMap<Long, List<Long>> edgeMapping = new HashMap<Long, List<Long>>();
		edgeMapping.put(nodeNameId.get("NodeOne"), new ArrayList(Arrays.asList(nodeNameId.get("NodeTwo"))));
		
		HashMap<String, List<Boolean>> edgeMappingDirected = new HashMap<String, List<Boolean>>();
		edgeMappingDirected.put(nodeNameId.get("NodeOne").toString() + "," + nodeNameId.get("NodeTwo").toString(), new ArrayList(Arrays.asList(true)));
		
		CheckEdges(cyNetwork, edgeMapping, edgeMappingDirected);
		
		
		//check the node attributes
		String[] names = new String[]{"integer-test", "listfloat-test"};
		Class[] types = new Class[] {Integer.class, Double[].class};
		
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeOne"), names, types, new Object[] {5, new Double[]{2.1d,96.23d}});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeTwo"), names, types, new Object[] {10, new Double[]{4.9}});
	}
	
	@Test
	public void ParseListInteger12() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/listinteger12.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		//check the counts
		assertEquals(2, cyNetwork.getNodeCount());
		assertEquals(1, cyNetwork.getEdgeCount());
		
		
		
		//build the node map
		HashMap<String, Long> nodeNameId = BuildNodeMap(cyNetwork);
		
		//check the nodes
		assertEquals(true, nodeNameId.containsKey("NodeOne"));
		assertEquals(true, nodeNameId.containsKey("NodeTwo"));
		
		
		//build the edge map
		HashMap<Long, List<Long>> edgeMapping = new HashMap<Long, List<Long>>();
		edgeMapping.put(nodeNameId.get("NodeOne"), new ArrayList(Arrays.asList(nodeNameId.get("NodeTwo"))));
		
		HashMap<String, List<Boolean>> edgeMappingDirected = new HashMap<String, List<Boolean>>();
		edgeMappingDirected.put(nodeNameId.get("NodeOne").toString() + "," + nodeNameId.get("NodeTwo").toString(), new ArrayList(Arrays.asList(true)));
		
		CheckEdges(cyNetwork, edgeMapping, edgeMappingDirected);
		
		
		//check the node attributes
		String[] names = new String[]{"integer-test", "listinteger-test"};
		Class[] types = new Class[] {Integer.class, Integer[].class};
		
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeOne"), names, types, new Object[] {5, new Integer[]{1,46}});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeTwo"), names, types, new Object[] {10, new Integer[]{2}});
	}
	
	@Test
	public void ParseListLong12() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/listlong12.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		//check the counts
		assertEquals(2, cyNetwork.getNodeCount());
		assertEquals(1, cyNetwork.getEdgeCount());
		
		
		
		//build the node map
		HashMap<String, Long> nodeNameId = BuildNodeMap(cyNetwork);
		
		//check the nodes
		assertEquals(true, nodeNameId.containsKey("NodeOne"));
		assertEquals(true, nodeNameId.containsKey("NodeTwo"));
		
		
		//build the edge map
		HashMap<Long, List<Long>> edgeMapping = new HashMap<Long, List<Long>>();
		edgeMapping.put(nodeNameId.get("NodeOne"), new ArrayList(Arrays.asList(nodeNameId.get("NodeTwo"))));
		
		HashMap<String, List<Boolean>> edgeMappingDirected = new HashMap<String, List<Boolean>>();
		edgeMappingDirected.put(nodeNameId.get("NodeOne").toString() + "," + nodeNameId.get("NodeTwo").toString(), new ArrayList(Arrays.asList(true)));
		
		CheckEdges(cyNetwork, edgeMapping, edgeMappingDirected);
		
		
		//check the node attributes
		String[] names = new String[]{"integer-test", "listlong-test"};
		Class[] types = new Class[] {Integer.class, Long[].class};
		
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeOne"), names, types, new Object[] {5, new Long[]{3147483647l,87l}});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeTwo"), names, types, new Object[] {10, new Long[]{4147483647l}});
	}

}
