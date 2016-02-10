package edu.umuc.swen670.gexf.internal.io;

import java.io.InputStream;

import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umuc.swen670.gexf.internal.model.GEXFParser;

public class GEXFNetworkReader extends AbstractCyNetworkReader  {

	private final InputStream _inputStream;
	private final CyNetworkViewFactory _cyNetworkViewFactory;
	private final CyNetworkFactory _cyNetworkFactory;
	private final CyNetworkManager _cyNetworkManager;
	private final CyRootNetworkManager _cyRootNetworkManager;
	private CyNetwork _cyNetwork;
	
	//private static final Logger _logger = LoggerFactory.getLogger(GEXFNetworkReader.class);
	
	public GEXFNetworkReader(InputStream inputStream, CyNetworkViewFactory cyNetworkViewFactory,
			CyNetworkFactory cyNetworkFactory, CyNetworkManager cyNetworkManager,
			CyRootNetworkManager cyRootNetworkManager) {
		super(inputStream, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);
		
		if (inputStream == null) throw new NullPointerException("inputStream cannot be null");
		if (cyNetworkViewFactory == null) throw new NullPointerException("cyNetworkViewFactory cannot be null");
		if (cyNetworkFactory == null) throw new NullPointerException("cyNetworkFactory cannot be null");
		if (cyNetworkManager == null) throw new NullPointerException("cyNetworkManager cannot be null");
		if (cyRootNetworkManager == null) throw new NullPointerException("cyRootNetworkManager cannot be null");
		
		_inputStream = inputStream;
		_cyNetworkViewFactory = cyNetworkViewFactory;
		_cyNetworkFactory = cyNetworkFactory;
		_cyNetworkManager = cyNetworkManager;
		_cyRootNetworkManager = cyRootNetworkManager;
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		final CyNetworkView cyNetworkView = _cyNetworkViewFactory.createNetworkView(network);
		
		return cyNetworkView;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		monitor.setTitle("Import GEXF");
		
		_cyNetwork = _cyNetworkFactory.createNetwork();
		
		monitor.setStatusMessage("Create nodes");
		//Thread.sleep(1000);
		monitor.setProgress(0.50);
		
		GEXFParser gexfParser = new GEXFParser();
		gexfParser.ParseStream(_inputStream, _cyNetwork);
		

		monitor.setStatusMessage("Add network");
		//Thread.sleep(1000);
		monitor.setProgress(1.00);
		
		this.networks = new CyNetwork[1];
		this.networks[0] = _cyNetwork;
	}

}
