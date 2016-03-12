package edu.umuc.swen670.gexf.internal.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.junit.Test;

public class EdgeTypeTest extends TestBase {

	@Test
	public void ParseEdgeTestFile() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/edgetest.gexf");
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
		
		
		//check the edge attributes
		String[] names = new String[]{"weight"};
		Class[] types = new Class[] {Double.class};
		
		ValidateEdgeAttributes(cyNetwork, nodeNameId.get("NodeOne"), nodeNameId.get("NodeTwo"), names, types, new Object[] {2.0});
	}

	@Test
	public void ParseEdgeTypeDirFile() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/edgetypedir.gexf");
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
		assertEquals(true, nodeNameId.containsKey("Hello"));
		assertEquals(true, nodeNameId.containsKey("World"));
		
		
		//build the edge map
		HashMap<Long, List<Long>> edgeMapping = new HashMap<Long, List<Long>>();
		edgeMapping.put(nodeNameId.get("Hello"), new ArrayList(Arrays.asList(nodeNameId.get("World"))));
		
		HashMap<String, List<Boolean>> edgeMappingDirected = new HashMap<String, List<Boolean>>();
		edgeMappingDirected.put(nodeNameId.get("Hello").toString() + "," + nodeNameId.get("World").toString(), new ArrayList(Arrays.asList(true)));
		
		
		CheckEdges(cyNetwork, edgeMapping, edgeMappingDirected);
		
		
		//check the edge attributes
		String[] names = new String[]{"type"};
		Class[] types = new Class[] {String.class};
		
		ValidateEdgeAttributes(cyNetwork, nodeNameId.get("Hello"), nodeNameId.get("World"), names, types, new Object[] {"dir"});
	}
	
	@Test
	public void ParseEdgeTypeDirectedFile() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/edgetypedirected.gexf");
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
		assertEquals(true, nodeNameId.containsKey("Hello"));
		assertEquals(true, nodeNameId.containsKey("World"));
		
		
		//build the edge map
		HashMap<Long, List<Long>> edgeMapping = new HashMap<Long, List<Long>>();
		edgeMapping.put(nodeNameId.get("Hello"), new ArrayList(Arrays.asList(nodeNameId.get("World"))));
		
		HashMap<String, List<Boolean>> edgeMappingDirected = new HashMap<String, List<Boolean>>();
		edgeMappingDirected.put(nodeNameId.get("Hello").toString() + "," + nodeNameId.get("World").toString(), new ArrayList(Arrays.asList(true)));
		
		
		CheckEdges(cyNetwork, edgeMapping, edgeMappingDirected);
		
		
		//check the edge attributes
		String[] names = new String[]{"type"};
		Class[] types = new Class[] {String.class};
		
		ValidateEdgeAttributes(cyNetwork, nodeNameId.get("Hello"), nodeNameId.get("World"), names, types, new Object[] {"directed"});
	}
	
	@Test
	public void ParseEdgeTypeDouFile() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/edgetypedou.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		
		//check the counts
		assertEquals(2, cyNetwork.getNodeCount());
		assertEquals(2, cyNetwork.getEdgeCount());
		
		
		
		//build the node map
		HashMap<String, Long> nodeNameId = BuildNodeMap(cyNetwork);
		
		//check the nodes
		assertEquals(true, nodeNameId.containsKey("Hello"));
		assertEquals(true, nodeNameId.containsKey("World"));
		
		
		//build the edge map
		HashMap<Long, List<Long>> edgeMapping = new HashMap<Long, List<Long>>();
		edgeMapping.put(nodeNameId.get("Hello"), new ArrayList(Arrays.asList(nodeNameId.get("World"))));
		edgeMapping.put(nodeNameId.get("World"), new ArrayList(Arrays.asList(nodeNameId.get("Hello"))));
		
		HashMap<String, List<Boolean>> edgeMappingDirected = new HashMap<String, List<Boolean>>();
		edgeMappingDirected.put(nodeNameId.get("Hello").toString() + "," + nodeNameId.get("World").toString(), new ArrayList(Arrays.asList(true)));
		edgeMappingDirected.put(nodeNameId.get("World").toString() + "," + nodeNameId.get("Hello").toString(), new ArrayList(Arrays.asList(true)));
		
		
		CheckEdges(cyNetwork, edgeMapping, edgeMappingDirected);
		
		
		//check the edge attributes
		String[] names = new String[]{"type"};
		Class[] types = new Class[] {String.class};
		
		ValidateEdgeAttributes(cyNetwork, nodeNameId.get("Hello"), nodeNameId.get("World"), names, types, new Object[] {"dou"});
		ValidateEdgeAttributes(cyNetwork, nodeNameId.get("World"), nodeNameId.get("Hello"), names, types, new Object[] {"dou"});
	}
	
	@Test
	public void ParseEdgeTypeDoubleFile() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/edgetypedouble.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		
		//check the counts
		assertEquals(2, cyNetwork.getNodeCount());
		assertEquals(2, cyNetwork.getEdgeCount());
		
		
		
		//build the node map
		HashMap<String, Long> nodeNameId = BuildNodeMap(cyNetwork);
		
		//check the nodes
		assertEquals(true, nodeNameId.containsKey("Hello"));
		assertEquals(true, nodeNameId.containsKey("World"));
		
		
		//build the edge map
		HashMap<Long, List<Long>> edgeMapping = new HashMap<Long, List<Long>>();
		edgeMapping.put(nodeNameId.get("Hello"), new ArrayList(Arrays.asList(nodeNameId.get("World"))));
		edgeMapping.put(nodeNameId.get("World"), new ArrayList(Arrays.asList(nodeNameId.get("Hello"))));
		
		HashMap<String, List<Boolean>> edgeMappingDirected = new HashMap<String, List<Boolean>>();
		edgeMappingDirected.put(nodeNameId.get("Hello").toString() + "," + nodeNameId.get("World").toString(), new ArrayList(Arrays.asList(true)));
		edgeMappingDirected.put(nodeNameId.get("World").toString() + "," + nodeNameId.get("Hello").toString(), new ArrayList(Arrays.asList(true)));
		
		
		CheckEdges(cyNetwork, edgeMapping, edgeMappingDirected);
		
		
		//check the edge attributes
		String[] names = new String[]{"type"};
		Class[] types = new Class[] {String.class};
		
		ValidateEdgeAttributes(cyNetwork, nodeNameId.get("Hello"), nodeNameId.get("World"), names, types, new Object[] {"double"});
		ValidateEdgeAttributes(cyNetwork, nodeNameId.get("World"), nodeNameId.get("Hello"), names, types, new Object[] {"double"});
	}
	
	@Test
	public void ParseEdgeTypeMutualFile() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/edgetypemutual.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		
		//check the counts
		assertEquals(2, cyNetwork.getNodeCount());
		assertEquals(2, cyNetwork.getEdgeCount());
		
		
		
		//build the node map
		HashMap<String, Long> nodeNameId = BuildNodeMap(cyNetwork);
		
		//check the nodes
		assertEquals(true, nodeNameId.containsKey("Hello"));
		assertEquals(true, nodeNameId.containsKey("World"));
		
		
		//build the edge map
		HashMap<Long, List<Long>> edgeMapping = new HashMap<Long, List<Long>>();
		edgeMapping.put(nodeNameId.get("Hello"), new ArrayList(Arrays.asList(nodeNameId.get("World"))));
		edgeMapping.put(nodeNameId.get("World"), new ArrayList(Arrays.asList(nodeNameId.get("Hello"))));
		
		HashMap<String, List<Boolean>> edgeMappingDirected = new HashMap<String, List<Boolean>>();
		edgeMappingDirected.put(nodeNameId.get("Hello").toString() + "," + nodeNameId.get("World").toString(), new ArrayList(Arrays.asList(true)));
		edgeMappingDirected.put(nodeNameId.get("World").toString() + "," + nodeNameId.get("Hello").toString(), new ArrayList(Arrays.asList(true)));
		
		
		CheckEdges(cyNetwork, edgeMapping, edgeMappingDirected);
		
		
		//check the edge attributes
		String[] names = new String[]{"type"};
		Class[] types = new Class[] {String.class};
		
		ValidateEdgeAttributes(cyNetwork, nodeNameId.get("Hello"), nodeNameId.get("World"), names, types, new Object[] {"mutual"});
		ValidateEdgeAttributes(cyNetwork, nodeNameId.get("World"), nodeNameId.get("Hello"), names, types, new Object[] {"mutual"});
	}
	
	@Test
	public void ParseEdgeTypeSimFile() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/edgetypesim.gexf");
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
		assertEquals(true, nodeNameId.containsKey("Hello"));
		assertEquals(true, nodeNameId.containsKey("World"));
		
		
		//build the edge map
		HashMap<Long, List<Long>> edgeMapping = new HashMap<Long, List<Long>>();
		edgeMapping.put(nodeNameId.get("Hello"), new ArrayList(Arrays.asList(nodeNameId.get("World"))));
		
		HashMap<String, List<Boolean>> edgeMappingDirected = new HashMap<String, List<Boolean>>();
		edgeMappingDirected.put(nodeNameId.get("Hello").toString() + "," + nodeNameId.get("World").toString(), new ArrayList(Arrays.asList(false)));
		
		
		CheckEdges(cyNetwork, edgeMapping, edgeMappingDirected);
		
		
		//check the edge attributes
		String[] names = new String[]{"type"};
		Class[] types = new Class[] {String.class};
		
		ValidateEdgeAttributes(cyNetwork, nodeNameId.get("Hello"), nodeNameId.get("World"), names, types, new Object[] {"sim"});
	}
}
