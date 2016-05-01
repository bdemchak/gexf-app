package edu.umuc.swen670.gexf.internal.model;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.model.CyNetwork;


public class GEXF13Parser extends GEXFParserBase {

	public GEXF13Parser(XMLStreamReader xmlReader, CyNetwork cyNetwork, String version,
			CyGroupFactory cyGroupFactory, CyGroupManager cyGroupManager, CyGroupSettingsManager cyGroupSettingsManager) {
		super(xmlReader, cyNetwork, version, cyGroupFactory, cyGroupManager, cyGroupSettingsManager);
	}

	@Override
	protected <T> List<T> ParseArray(String array, Class<T> type) throws IOException {
		List<T> list = new ArrayList<T>();
		
		StringReader reader = new StringReader(array + ' '); //pad the string
		
		int r;
		char c;
		while((r=reader.read()) != -1) {
			c = (char)r;
			
			if(c=='[' || c=='(' || c==']' || c==')' || c==',' || c==' ' || c=='\t' || c=='\r' || c=='\n') {
				//keep processing
			}
			else if(c=='"' || c=='\'') {
				String literal = "";
				
				do {
					literal = literal + c;
					
					c = (char)reader.read();
				}while(c!='"' && c!='\'');
				
				list.add(GenericParse(literal.substring(1, literal.length()), type));
			}
			else {
				String value = "";
				
				do {
					value = value + c;
					
					c = (char)reader.read();
				}while(c!=']' && c!=')' && c!=',' && c!=' ' && c!='\t' && c!='\r' && c!='\n');
				
				if(value.equals("null")) {
					list.add(null);
				}
				else {
					list.add(GenericParse(value, type));
				}
				
				reader.skip(-1);
			}
		}

		return list;
	}

	@Override
	protected Boolean IsDirected(String direction) {
		if(direction.equalsIgnoreCase(EdgeTypes.DIRECTED)) {
			return true;
		}
		else if(direction.equalsIgnoreCase(EdgeTypes.UNDIRECTED)) {
			return false;
		}
		else if (direction.equalsIgnoreCase(EdgeTypes.MUTUAL)) {
			return true;
		}
		else {
			throw new IllegalArgumentException(direction);
		}
	}

	@Override
	protected Boolean IsBiDirectional(String direction) {
		if (direction.equalsIgnoreCase(EdgeTypes.MUTUAL)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	protected String DefaultEdgeDirection() {
		return EdgeTypes.UNDIRECTED;
	}

}
