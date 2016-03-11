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

public class DataTypeSupportTest extends TestBase {

	@Test
	public void ParseDataTypeSupportFile() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/datatypesupport.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		//check meta
		CheckMeta(cyNetwork, "2016-03-09", "Gephi.org", "", "Data Type Support Test");
		
		
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
		edgeMappingDirected.put(nodeNameId.get("NodeOne").toString() + "," + nodeNameId.get("NodeTwo"), new ArrayList(Arrays.asList(true)));
		
		CheckEdges(cyNetwork, edgeMapping, edgeMappingDirected);
		
		
		
		//check the node attributes
		String[] names =  new String[]{"integer-test", "long-test", "float-test", "double-test", "boolean-test", "string-test"};
		Class[] types = new Class[] {Integer.class, Long.class, Double.class, Double.class, Boolean.class, String.class};
		
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeOne"), names, types, new Object[] {5, 3147483647L, 1.0, 2.0, true, "foo"});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("NodeTwo"), names, types, new Object[] {10, 2547483647L, 2.0, 4.0, false, "bar"});
	}

}
