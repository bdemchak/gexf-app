package edu.umuc.swen670.gexf.internal.io;

import java.io.InputStream;
import java.util.List;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskMonitor;


import edu.umuc.swen670.gexf.internal.model.GEXFParser;

public class GEXFNetworkReader extends AbstractCyNetworkReader  {

	private final InputStream _inputStream;
	private final CyNetworkViewFactory _cyNetworkViewFactory;
	private final CyNetworkFactory _cyNetworkFactory;
	private final CyNetworkManager _cyNetworkManager;
	private final CyRootNetworkManager _cyRootNetworkManager;
	private CyNetwork _cyNetwork;
	private final CyEventHelper _cyEventHelper;
	private final CyGroupFactory _cyGroupFactory;
	private final CyGroupManager _cyGroupManager;
	
	private List<DelayedVizProp> _vizProps = null;


	public GEXFNetworkReader(InputStream inputStream, CyNetworkViewFactory cyNetworkViewFactory,
			CyNetworkFactory cyNetworkFactory, CyNetworkManager cyNetworkManager,
			CyRootNetworkManager cyRootNetworkManager, final CyEventHelper cyEventHelper,
			CyGroupFactory cyGroupFactory, CyGroupManager cyGroupManager) {
		super(inputStream, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);

		if (inputStream == null) throw new NullPointerException("inputStream cannot be null");
		if (cyNetworkViewFactory == null) throw new NullPointerException("cyNetworkViewFactory cannot be null");
		if (cyNetworkFactory == null) throw new NullPointerException("cyNetworkFactory cannot be null");
		if (cyNetworkManager == null) throw new NullPointerException("cyNetworkManager cannot be null");
		if (cyRootNetworkManager == null) throw new NullPointerException("cyRootNetworkManager cannot be null");
		if (cyEventHelper == null) throw new NullPointerException("cyRootNetworkManager cannot be null");
		if (cyGroupFactory == null) throw new NullPointerException("cyGroupFactory cannot be null");
		if (cyGroupManager == null) throw new NullPointerException("cyGroupManager cannot be null");

		_inputStream = inputStream;
		_cyNetworkViewFactory = cyNetworkViewFactory;
		_cyNetworkFactory = cyNetworkFactory;
		_cyNetworkManager = cyNetworkManager;
		_cyRootNetworkManager = cyRootNetworkManager;
		_cyEventHelper = cyEventHelper;
		_cyGroupFactory = cyGroupFactory;
		_cyGroupManager = cyGroupManager;
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		final CyNetworkView cyNetworkView = _cyNetworkViewFactory.createNetworkView(network);
		
		_cyEventHelper.flushPayloadEvents();
		
		DelayedVizProp.applyAll(cyNetworkView, _vizProps);
		
		cyNetworkView.updateView();

		return cyNetworkView;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		monitor.setTitle("Import GEXF");

		_cyNetwork = _cyNetworkFactory.createNetwork();

		monitor.setStatusMessage("Parsing file");
		monitor.setProgress(0.50);

		
		GEXFParser gexfParser = new GEXFParser();
		_vizProps = gexfParser.ParseStream(_inputStream, _cyNetwork, _cyGroupFactory);


		monitor.setStatusMessage("Add network");
		monitor.setProgress(1.00);

		this.networks = new CyNetwork[1];
		this.networks[0] = _cyNetwork;
	}

}
