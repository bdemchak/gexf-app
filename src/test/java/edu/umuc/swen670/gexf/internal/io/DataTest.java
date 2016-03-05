package edu.umuc.swen670.gexf.internal.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.junit.Test;

public class DataTest extends TestBase {

	@Test
	public void ParseDataFile() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/data12.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		//check the counts
		assertEquals(4, cyNetwork.getNodeCount());
		assertEquals(5, cyNetwork.getEdgeCount());
		
		
		
		//build the node map
		HashMap<String, Long> nodeNameId = BuildNodeMap(cyNetwork);
		
		//check the nodes
		assertEquals(true, nodeNameId.containsKey("Gephi"));
		assertEquals(true, nodeNameId.containsKey("Webatlas"));
		assertEquals(true, nodeNameId.containsKey("RTGI"));
		assertEquals(true, nodeNameId.containsKey("BarabasiLab"));
		
		
		
		//build the edge map
		HashMap<Long, List<Long>> edgeMapping = new HashMap<Long, List<Long>>();
		edgeMapping.put(nodeNameId.get("Gephi"), Arrays.asList(nodeNameId.get("Webatlas"), nodeNameId.get("RTGI"), nodeNameId.get("BarabasiLab")));
		edgeMapping.put(nodeNameId.get("Webatlas"), Arrays.asList(nodeNameId.get("Gephi")));
		edgeMapping.put(nodeNameId.get("RTGI"), Arrays.asList(nodeNameId.get("Webatlas")));
		
		//check the edges
		List<CyEdge> cyEdges = cyNetwork.getEdgeList();
		for(CyEdge cyEdge : cyEdges) {
			assertEquals(true, edgeMapping.get(cyEdge.getSource().getSUID()).contains(cyEdge.getTarget().getSUID()));
		}
		
		
		
		//check the node attributes
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("Gephi"), new String[]{"url", "indegree"}, new Class[] {String.class, Double.class}, new Object[] {"http://gephi.org", 1.0});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("Webatlas"), new String[]{"url", "indegree"}, new Class[] {String.class, Double.class}, new Object[] {"http://webatlas.fr", 2.0});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("RTGI"), new String[]{"url", "indegree"}, new Class[] {String.class, Double.class}, new Object[] {"http://rtgi.fr", 1.0});
		ValidateNodeAttributes(cyNetwork, nodeNameId.get("BarabasiLab"), new String[]{"url", "indegree", "frog"}, new Class[] {String.class, Double.class, Boolean.class}, new Object[] {"http://barabasilab.com", 1.0, false});
	}

}
