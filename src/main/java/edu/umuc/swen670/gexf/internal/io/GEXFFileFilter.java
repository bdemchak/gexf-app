package edu.umuc.swen670.gexf.internal.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.swing.JOptionPane;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GEXFFileFilter extends BasicCyFileFilter {

	private static final Logger _logger = LoggerFactory.getLogger(GEXFFileFilter.class);

	public GEXFFileFilter(StreamUtil streamUtil) {
		super(new String[] {"gexf"}, new String[] {"application/gexf+xml"}, "Graph Exchange XML Format (gexf)", DataCategory.NETWORK, streamUtil);
	}

	@Override
	public boolean accepts(final URI uri, final DataCategory category) {
		try {
			return accepts(uri.toURL().openStream(), category);
		} catch (IOException e) {
			_logger.error("Error while opening stream: " + uri, e);
			return false;
		}
	}

	@Override
	public boolean accepts(final InputStream stream, final DataCategory category) {
		final String header = getHeader(stream, 5);
		_logger.debug("File header: " + header);

		//JOptionPane.showMessageDialog(null, header);

		return true;
	}

}
