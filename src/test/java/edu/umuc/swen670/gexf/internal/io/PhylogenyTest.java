package edu.umuc.swen670.gexf.internal.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.HashMap;

import org.cytoscape.model.CyNetwork;
import org.junit.Test;

public class PhylogenyTest extends TestBase {

	@Test
	public void ParseVizFile() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/phylogeny_simple.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		//check the counts
//		assertEquals(3, cyNetwork.getNodeCount());
		
		
		//build the node map
		HashMap<String, Long> nodeNameId = BuildNodeMap(cyNetwork);
		
		//check the nodes
//		assertEquals(true, nodeNameId.containsKey("lemon"));
//		assertEquals(true, nodeNameId.containsKey("meringue"));
//		assertEquals(true, nodeNameId.containsKey("pie"));
	}

}
