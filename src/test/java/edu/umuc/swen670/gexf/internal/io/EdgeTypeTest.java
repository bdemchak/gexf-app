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

}
