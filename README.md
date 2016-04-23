# gexf-app
Plugin that allows Cytoscape to import Gephi .gexf files through the import network menu.

## Structure
### Inheritance
Each GEXF file format version has a version specific parser that inherits from a parser base class.  Common parsing logic is contained within the GEXFParserBase.java to the greatest extent possible.  The GEXFParser.java contains the logic to parser the file header to determine the file format version and instantiate the appropriate version specific parser.  If the version is unrecognized, the latest version specific parser will be used with the understanding that it may have problems if the unrecognized format contains breaking changes.

### Testing
Automated integration tests are provided for each of the supported features.  The basic flow of the test is to open a gexf file, determine the the counts for nodes and edges are correct, evaluate the edges to ensure that the correct nodes are linked, then parses the attributes for the nodes and edges.  Common testing functionality is provided in the TestBase.java class that all the tests inherit from.