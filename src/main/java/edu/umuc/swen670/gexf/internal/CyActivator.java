package edu.umuc.swen670.gexf.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.ServiceProperties;
import org.osgi.framework.BundleContext;

import edu.umuc.swen670.gexf.internal.io.GEXFFileFilter;
import edu.umuc.swen670.gexf.internal.io.GEXFNetworkReaderFactory;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context) throws Exception {

		final StreamUtil streamUtil = getService(context, StreamUtil.class);
		final CyApplicationManager cyApplicationManager = getService(context, CyApplicationManager.class);
		final CyNetworkViewFactory cyNetworkViewFactory = getService(context, CyNetworkViewFactory.class);
		final CyNetworkFactory cyNetworkFactory = getService(context, CyNetworkFactory.class);
		final CyNetworkManager cyNetworkManager = getService(context, CyNetworkManager.class);
		final CyRootNetworkManager cyRootNetworkManager = getService(context, CyRootNetworkManager.class);
		final CyEventHelper cyEventHelper = getService(context, CyEventHelper.class);
		final CyGroupFactory cyGroupFactory = getService(context, CyGroupFactory.class);
		final CyGroupManager cyGroupManager = getService(context, CyGroupManager.class);
		final CyGroupSettingsManager cyGroupSettingsManager = getService(context, CyGroupSettingsManager.class);
		final VisualMappingFunctionFactory passthroughMapper = getService(context, VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
		final VisualMappingManager visualMappingManager = getService(context, VisualMappingManager.class);

		//register reader
		final CyFileFilter gexfFileFilter = new GEXFFileFilter(streamUtil);
		final GEXFNetworkReaderFactory gexfNetworkReaderFactory = new GEXFNetworkReaderFactory(gexfFileFilter, cyNetworkViewFactory, cyNetworkFactory, 
																							   cyNetworkManager, cyRootNetworkManager, cyEventHelper, 
																							   cyGroupFactory, cyGroupManager, cyGroupSettingsManager, passthroughMapper, 
																							   visualMappingManager);
		final Properties gexfNetworkReaderFactoryProperties = new Properties();
		gexfNetworkReaderFactoryProperties.put(ServiceProperties.ID, "GEXFNetworkReaderFactory");
		registerService(context, gexfNetworkReaderFactory, InputStreamTaskFactory.class, gexfNetworkReaderFactoryProperties);
	}

}
