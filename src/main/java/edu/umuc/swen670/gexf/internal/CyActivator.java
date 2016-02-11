package edu.umuc.swen670.gexf.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.CyNetworkViewFactory;
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
		
		//register reader
		final CyFileFilter gexfFileFilter = new GEXFFileFilter(streamUtil);
		final GEXFNetworkReaderFactory gexfNetworkReaderFactory = new GEXFNetworkReaderFactory(gexfFileFilter, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);
		final Properties gexfNetworkReaderFactoryProperties = new Properties();
		gexfNetworkReaderFactoryProperties.put(ServiceProperties.ID, "GEXFNetworkReaderFactory");
		registerService(context, gexfNetworkReaderFactory, InputStreamTaskFactory.class, gexfNetworkReaderFactoryProperties);
		
		
		//register menu
		MenuAction action = new MenuAction(cyApplicationManager, "Hello World App");
		Properties properties = new Properties();
		registerAllServices(context, action, properties);
	}

}
