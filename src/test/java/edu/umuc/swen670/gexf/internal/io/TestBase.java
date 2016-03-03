package edu.umuc.swen670.gexf.internal.io;

import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskMonitor;

public class TestBase {

	protected CyNetwork[] RunFile(InputStream stream) throws Exception {
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
	
	protected HashMap<String, Long> BuildNodeMap(CyNetwork cyNetwork) {
		HashMap<String, Long> nodeNameId = new HashMap<String, Long>();
		
		CyTable cyNodeTable = cyNetwork.getDefaultNodeTable();
		List<CyRow> cyNodeRows = cyNodeTable.getAllRows();
		
		for(CyRow cyNodeRow : cyNodeRows) {
			nodeNameId.put(cyNodeRow.get("name", String.class), cyNodeRow.get("SUID", Long.class));
		}
		
		return nodeNameId;
	}

}
