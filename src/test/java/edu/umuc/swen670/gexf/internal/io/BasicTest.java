package edu.umuc.swen670.gexf.internal.io;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import static org.mockito.Mockito.mock;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;


public class BasicTest {
	
	@Test
	public void ParseBasicFile() throws Exception {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("testData/gexf/basic.gexf");
		assertNotNull(stream);
		
		CyNetwork[] cyNetworks = RunFile(stream);
		
		assertNotNull(cyNetworks);
		assertEquals(1, cyNetworks.length);
		
		CyNetwork cyNetwork = cyNetworks[0];
		
		//check the counts
		assertEquals(2, cyNetwork.getNodeCount());
		assertEquals(1, cyNetwork.getEdgeCount());
		
		//check the nodes
		CyTable cyTable = cyNetwork.getDefaultNodeTable();
		CyColumn nameColumn = cyTable.getColumn("name");
		List<String> names = nameColumn.getValues(String.class);
		assertEquals(true, names.contains("Hello"));
		assertEquals(true, names.contains("Word"));
		
		//check the edges
		//???
	}
	
	private CyNetwork[] RunFile(InputStream stream) throws Exception {
		CyNetworkFactory cyNetworkFactory = new NetworkTestSupport().getNetworkFactory();
		CyNetworkViewFactory cyNetworkViewFactory = new NetworkViewTestSupport().getNetworkViewFactory();
		CyNetworkManager cyNetworkManager = new NetworkTestSupport().getNetworkManager();
		CyRootNetworkManager cyRootNetworkManager = new NetworkTestSupport().getRootNetworkFactory();
		
		CyNetworkReader reader = new GEXFNetworkReader(stream, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);
		
		TaskMonitor monitor = mock(TaskMonitor.class);
		
		reader.run(monitor);
		stream.close();
		
		CyNetwork[] cyNetworks = reader.getNetworks();
		
		return cyNetworks;
	}
}