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

public class VizTest extends TestBase {

	@Test
	public void ParseVizFile() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/viz12.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		//check the counts
		assertEquals(1, cyNetwork.getNodeCount());
		
		
		//build the node map
		HashMap<String, Long> nodeNameId = BuildNodeMap(cyNetwork);
		
		//check the nodes
		assertEquals(true, nodeNameId.containsKey("glossy"));
		
		//check the node attributes
		String[] names =  new String[]{"viz_color", "viz_transparency", "viz_x", "viz_y", "viz_z", "viz_size", "viz_shape"};
		Class[] types = new Class[] {String.class, Integer.class, Double.class, Double.class, Double.class, Double.class, String.class};
		
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("glossy"), names, types, new Object[] {"#efad42", 153, 15.783598d, -40.109245d, 0.0d, 2.0375757d, null});
	}
	
	@Test
	public void ParseVizTestFiveFile() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/vizTestFive.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		
		//check the counts
		assertEquals(5, cyNetwork.getNodeCount());
		assertEquals(4, cyNetwork.getEdgeCount());
		
		
		
		//build the node map
		HashMap<String, Long> nodeNameId = BuildNodeMap(cyNetwork);
		
		//check the nodes
		assertEquals(true, nodeNameId.containsKey("One"));
		assertEquals(true, nodeNameId.containsKey("Two"));
		assertEquals(true, nodeNameId.containsKey("Three"));
		assertEquals(true, nodeNameId.containsKey("Four"));
		assertEquals(true, nodeNameId.containsKey("Five"));
		
		//build the edge map
		HashMap<Long, List<Long>> edgeMapping = new HashMap<Long, List<Long>>();
		edgeMapping.put(nodeNameId.get("One"), new ArrayList(Arrays.asList(nodeNameId.get("Two"),nodeNameId.get("Four"))));
		edgeMapping.put(nodeNameId.get("Two"), new ArrayList(Arrays.asList(nodeNameId.get("Three"))));
		edgeMapping.put(nodeNameId.get("Four"), new ArrayList(Arrays.asList(nodeNameId.get("Five"))));
		
		HashMap<String, List<Boolean>> edgeMappingDirected = new HashMap<String, List<Boolean>>();
		edgeMappingDirected.put(nodeNameId.get("One").toString() + "," + nodeNameId.get("Two").toString(), new ArrayList(Arrays.asList(true)));
		edgeMappingDirected.put(nodeNameId.get("Two").toString() + "," + nodeNameId.get("Three").toString(), new ArrayList(Arrays.asList(true)));
		edgeMappingDirected.put(nodeNameId.get("One").toString() + "," + nodeNameId.get("Four").toString(), new ArrayList(Arrays.asList(true)));
		edgeMappingDirected.put(nodeNameId.get("Four").toString() + "," + nodeNameId.get("Five").toString(), new ArrayList(Arrays.asList(true)));
		
		CheckEdges(cyNetwork, edgeMapping, edgeMappingDirected);
		
		//check the node attributes
		String[] names =  new String[]{"viz_color", "viz_transparency", "viz_x", "viz_y", "viz_z", "viz_size", "viz_shape"};
		Class[] types = new Class[] {String.class, Integer.class, Double.class, Double.class, Double.class, Double.class, String.class};
		
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("One"), names, types, new Object[] {"#808000", 255, 0.0d, 0.0d, 0.0d, 10.0d, "DIAMOND"});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("Two"), names, types, new Object[] {"#ffd700", 255, 10.0d, -10.0d, 0.0d, 5.0d, "ELLIPSE"});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("Three"), names, types, new Object[] {"#ffd700", 255, 10.0d, -40.0d, 0.0d, 5.0d, "ELLIPSE"});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("Four"), names, types, new Object[] {"#8fbc8f", 255, -10.0d, 10.0d, 0.0d, 5.0d, "DIAMOND"});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("Five"), names, types, new Object[] {"#8fbc8f", 255, -10.0d, 40.0d, 0.0d, 5.0d, "TRIANGLE"});
		
		
		//check the edge attributes
		names = new String[]{"viz_color", "viz_transparency", "viz_thickness", "viz_shape"};
		types = new Class[] {String.class, Integer.class, Double.class, String.class};
		
		ValidateEdgeAttributes(cyNetwork, nodeNameId.get("One"), nodeNameId.get("Two"), names, types, new Object[] {"#a52a2a", 255, null, "SOLID"});
		ValidateEdgeAttributes(cyNetwork, nodeNameId.get("Two"), nodeNameId.get("Three"), names, types, new Object[] {"#800000", 127, null, "DOT"});
		ValidateEdgeAttributes(cyNetwork, nodeNameId.get("One"), nodeNameId.get("Four"), names, types, new Object[] {"#2f4f4f", 255, null, "PARALLEL_LINES"});
		ValidateEdgeAttributes(cyNetwork, nodeNameId.get("Four"), nodeNameId.get("Five"), names, types, new Object[] {"#ff69b4", 255, null, "EQUAL_DASH"});
	}

}
