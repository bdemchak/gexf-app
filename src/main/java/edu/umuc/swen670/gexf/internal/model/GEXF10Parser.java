package edu.umuc.swen670.gexf.internal.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.model.CyNetwork;


public class GEXF10Parser extends GEXFParserBase {

	public GEXF10Parser(XMLStreamReader xmlReader, CyNetwork cyNetwork, String version,
			CyGroupFactory cyGroupFactory, CyGroupManager cyGroupManager, CyGroupSettingsManager cyGroupSettingsManager) {
		super(xmlReader, cyNetwork, version, cyGroupFactory, cyGroupManager, cyGroupSettingsManager);
	}
	
	@Override
	protected <T> List<T> ParseArray(String array, Class<T> type) throws IOException {
		String[] values = array.split("\\,|\\;|\\|");
		
		List<T> list = new ArrayList<T>();
		for(String value : values) {
			list.add(GenericParse(value.trim(), type));
		}
		
		return list;
	}

	@Override
	protected Boolean IsDirected(String direction) {
		if(direction.equalsIgnoreCase(EdgeTypes.DIRECTED) || direction.equalsIgnoreCase(EdgeTypes.DIR)) {
			return true;
		}
		else if(direction.equalsIgnoreCase(EdgeTypes.SIMPLE) || direction.equalsIgnoreCase(EdgeTypes.SIM)) {
			return false;
		}
		else if (direction.equalsIgnoreCase(EdgeTypes.DOUBLE) || direction.equalsIgnoreCase(EdgeTypes.DOU)) {
			return true;
		}
		else {
			throw new IllegalArgumentException(direction);
		}
	}

	@Override
	protected Boolean IsBiDirectional(String direction) {
		if (direction.equalsIgnoreCase(EdgeTypes.DOUBLE) || direction.equalsIgnoreCase(EdgeTypes.DOU)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	protected String DefaultEdgeDirection() {
		return EdgeTypes.SIMPLE;
	}

}
