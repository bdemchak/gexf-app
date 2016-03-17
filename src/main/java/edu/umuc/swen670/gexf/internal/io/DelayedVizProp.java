package edu.umuc.swen670.gexf.internal.io;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;

public class DelayedVizProp {
  final CyIdentifiable netObj;
  final VisualProperty<?> prop;
  final Object value;
  final boolean isLocked;

  /**
   * Specify the desired visual property value for a node or edge.
   * @param netObj A CyNode or CyEdge
   * @param prop The visual property whose value you want to assign
   * @param value The visual property value you want to assign
   * @param isLocked true if you want the value to be set as a bypass value, false if you want the value to persist until a visual style is applied to the network view
   */
  public DelayedVizProp(final CyIdentifiable netObj, final VisualProperty<?> prop, final Object value, final boolean isLocked) {
    this.netObj = netObj;
    this.prop = prop;
    this.value = value;
    this.isLocked = isLocked;
  }

  /**
   * Assign the visual properties stored in delayedProps to the given CyNetworkView.
   * @param netView The CyNetworkView that contains the nodes and edges for which you want to assign the visual properties
   * @param delayedProps A series of DelayedVizProps that specifies the visual property values of nodes and edges
   */
  public static void applyAll(final CyNetworkView netView, final Iterable<DelayedVizProp> delayedProps) {
    for (final DelayedVizProp delayedProp : delayedProps) {
      final Object value = delayedProp.value;
      if (value == null)
        continue;
      
      View<?> view = null;
      if (delayedProp.netObj instanceof CyNode) {
        final CyNode node = (CyNode) delayedProp.netObj;
        view = netView.getNodeView(node);
      } else if (delayedProp.netObj instanceof CyEdge) {
        final CyEdge edge = (CyEdge) delayedProp.netObj;
        view = netView.getEdgeView(edge);
      }

      if (delayedProp.isLocked) {
        view.setLockedValue(delayedProp.prop, value);
      } else {
        view.setVisualProperty(delayedProp.prop, value);
      }
    }
  }
}