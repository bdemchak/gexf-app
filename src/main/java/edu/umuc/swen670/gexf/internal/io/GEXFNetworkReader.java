package edu.umuc.swen670.gexf.internal.io;

import java.awt.Dimension;
import java.io.InputStream;
import java.util.List;
import java.util.Collection;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;


import edu.umuc.swen670.gexf.internal.model.GEXFParser;
import edu.umuc.swen670.gexf.internal.model.GEXFViz;

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
	private final CyGroupSettingsManager _cyGroupSettingsManager;
	private final VisualMappingFunctionFactory _passthroughMapper;
	private final VisualMappingManager _visualMappingManager;


	public GEXFNetworkReader(InputStream inputStream, CyNetworkViewFactory cyNetworkViewFactory, 
			CyNetworkFactory cyNetworkFactory, CyNetworkManager cyNetworkManager,
			CyRootNetworkManager cyRootNetworkManager, final CyEventHelper cyEventHelper,
			CyGroupFactory cyGroupFactory, CyGroupManager cyGroupManager, CyGroupSettingsManager cyGroupSettingsManager, 
			final VisualMappingFunctionFactory passthroughMapper, final VisualMappingManager visualMappingManager) {
		super(inputStream, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);

		if (inputStream == null) throw new NullPointerException("inputStream cannot be null");
		if (cyNetworkViewFactory == null) throw new NullPointerException("cyNetworkViewFactory cannot be null");
		if (cyNetworkFactory == null) throw new NullPointerException("cyNetworkFactory cannot be null");
		if (cyNetworkManager == null) throw new NullPointerException("cyNetworkManager cannot be null");
		if (cyRootNetworkManager == null) throw new NullPointerException("cyRootNetworkManager cannot be null");
		if (cyEventHelper == null) throw new NullPointerException("cyRootNetworkManager cannot be null");
		if (cyGroupFactory == null) throw new NullPointerException("cyGroupFactory cannot be null");
		if (cyGroupManager == null) throw new NullPointerException("cyGroupManager cannot be null");
		if (cyGroupSettingsManager == null) throw new NullPointerException("cyGroupSettingsManager cannot be null");

		_inputStream = inputStream;
		_cyNetworkViewFactory = cyNetworkViewFactory;
		_cyNetworkFactory = cyNetworkFactory;
		_cyNetworkManager = cyNetworkManager;
		_cyRootNetworkManager = cyRootNetworkManager;
		_cyEventHelper = cyEventHelper;
		_cyGroupFactory = cyGroupFactory;
		_cyGroupManager = cyGroupManager;
		_cyGroupSettingsManager = cyGroupSettingsManager;
		_passthroughMapper = passthroughMapper;
		_visualMappingManager = visualMappingManager;
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		final CyNetworkView cyNetworkView = _cyNetworkViewFactory.createNetworkView(network);
		
		_cyEventHelper.flushPayloadEvents();
		
		VisualStyle style = _visualMappingManager.getVisualStyle(cyNetworkView);

		style.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.SOLID);
		
		style.addVisualMappingFunction(_passthroughMapper.createVisualMappingFunction(GEXFViz.ATT_X, Double.class, BasicVisualLexicon.NODE_X_LOCATION));
		style.addVisualMappingFunction(_passthroughMapper.createVisualMappingFunction(GEXFViz.ATT_Y, Double.class, BasicVisualLexicon.NODE_Y_LOCATION));
		style.addVisualMappingFunction(_passthroughMapper.createVisualMappingFunction(GEXFViz.ATT_Z, Double.class, BasicVisualLexicon.NODE_Z_LOCATION));
		style.addVisualMappingFunction(_passthroughMapper.createVisualMappingFunction(GEXFViz.ATT_SHAPE, String.class, BasicVisualLexicon.NODE_SHAPE));
		style.addVisualMappingFunction(_passthroughMapper.createVisualMappingFunction(GEXFViz.ATT_COLOR, String.class, BasicVisualLexicon.NODE_FILL_COLOR));
		style.addVisualMappingFunction(_passthroughMapper.createVisualMappingFunction(GEXFViz.ATT_TRANSPARENCY, Integer.class, BasicVisualLexicon.NODE_TRANSPARENCY));
		style.addVisualMappingFunction(_passthroughMapper.createVisualMappingFunction(GEXFViz.ATT_SIZE, Double.class, BasicVisualLexicon.NODE_WIDTH));
		style.addVisualMappingFunction(_passthroughMapper.createVisualMappingFunction(GEXFViz.ATT_SIZE, Double.class, BasicVisualLexicon.NODE_HEIGHT));
		
        style.addVisualMappingFunction(_passthroughMapper.createVisualMappingFunction(GEXFViz.ATT_SHAPE, String.class, BasicVisualLexicon.EDGE_LINE_TYPE));
        style.addVisualMappingFunction(_passthroughMapper.createVisualMappingFunction(GEXFViz.ATT_COLOR, String.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT));
		style.addVisualMappingFunction(_passthroughMapper.createVisualMappingFunction(GEXFViz.ATT_TRANSPARENCY, Integer.class, BasicVisualLexicon.EDGE_TRANSPARENCY));
		style.addVisualMappingFunction(_passthroughMapper.createVisualMappingFunction(GEXFViz.ATT_THICKNESS, Double.class, BasicVisualLexicon.EDGE_WIDTH));
        
        style.apply(cyNetworkView);
		
		//https://groups.google.com/forum/#!msg/cytoscape-discuss/lnUhb6T7w5g/7qBedvdjdkUJ
		//https://groups.google.com/forum/#!topic/cytoscape-discuss/K3w4khYWnXI
        //http://wiki.cytoscape.org/Cytoscape_3/AppDeveloper/Cytoscape_3_App_Cookbook#How_to_use_the_VizMapper_programmatically.3F
        _cyEventHelper.flushPayloadEvents();
		cyNetworkView.updateView();
		
		collapseAllGroups(network, cyNetworkView);
		expandAllGroups(network, cyNetworkView);
		collapseAllGroups(network, cyNetworkView);
		expandAllGroups(network, cyNetworkView);
				
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
		gexfParser.ParseStream(_inputStream, _cyNetwork, _cyGroupFactory, _cyGroupManager, _cyGroupSettingsManager);

		monitor.setStatusMessage("Add network");
		monitor.setProgress(1.00);

		this.networks = new CyNetwork[1];
		this.networks[0] = _cyNetwork;
	}
	
	private void collapseAllGroups(CyNetwork network, CyNetworkView cyNetworkView) {
		boolean collapsedGroup = true;
		while (collapsedGroup) {
			collapsedGroup = false;
			
			//The code in this section is based off of the code in Cytoscape's GroupViewDoubleClickListener's CollapseGroupTask class
			//(Lines 170-207 of /group-impl/src/main/java/org/cytoscape/group/internal/view/GroupViewDoubleClickListener.java)
			Collection<View<CyNode>> nodeViews = cyNetworkView.getNodeViews();
			for (View<CyNode> cyNodeView : nodeViews) {
				CyNode node = cyNodeView.getModel();
				
				List<CyGroup> groups = _cyGroupManager.getGroupsForNode(node);
				if (groups != null && groups.size() > 0) {
					CyGroup group = groups.get(0);
					group.collapse(network);
					_cyEventHelper.flushPayloadEvents();
//					group.expand(network);
//					_cyEventHelper.flushPayloadEvents();
//					group.collapse(network);
//					_cyEventHelper.flushPayloadEvents();
//					updateViewAsGroupCollapsed(group);
//					_cyEventHelper.flushPayloadEvents();
					collapsedGroup = true;
				}
			}
		}
	}
	
	private void expandAllGroups(CyNetwork network, CyNetworkView cyNetworkView) {
		boolean expandedGroup = true;
		while (expandedGroup) {
			expandedGroup = false;
			
			//The code in this section is based off of the code in Cytoscape's GroupViewDoubleClickListener's CollapseGroupTask class
			//but reversed to expand all groups
			//(Lines 170-207 of /group-impl/src/main/java/org/cytoscape/group/internal/view/GroupViewDoubleClickListener.java)
			Collection<View<CyNode>> nodeViews = cyNetworkView.getNodeViews();
			for (View<CyNode> cyNodeView : nodeViews) {
				CyNode node = cyNodeView.getModel();
				
				if (_cyGroupManager.isGroup(node, network)) {
					CyGroup group = _cyGroupManager.getGroup(node, network);
					
					if (group.isCollapsed(network)) {
						group.expand(network);
						_cyEventHelper.flushPayloadEvents();
						expandedGroup = true;
					}
				}
				
				//cyNetworkView.updateView();
			}
		}
	}
}
