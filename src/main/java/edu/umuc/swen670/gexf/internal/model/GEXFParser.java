package edu.umuc.swen670.gexf.internal.model;

import java.io.InputStream;
import java.util.Hashtable;

import org.cytoscape.model.CyNetwork;

public class GEXFParser {

	public void ParseStream(InputStream inputStream, CyNetwork cyNetwork) {
		//TODO Check the GEXF file version as use the appropriate parser
		
		GEXF12Parser parser = new GEXF12Parser();
		
		parser.ParseNodes(inputStream, cyNetwork);
	}
}
