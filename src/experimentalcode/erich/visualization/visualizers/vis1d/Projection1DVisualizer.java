package experimentalcode.erich.visualization.visualizers.vis1d;

import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGPlot;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGUtil;
import experimentalcode.erich.visualization.visualizers.AbstractVisualizer;
import experimentalcode.erich.visualization.visualizers.ProjectedVisualizer;

/**
 * Produces visualizations of 1-dimensional projections.
 * 
 * @author Remigius Wojdanowski
 *
 * @param <NV> Type of the NumberVector being visualized.
 */
public abstract class Projection1DVisualizer<NV extends NumberVector<NV, ?>> extends AbstractVisualizer implements ProjectedVisualizer {
  /**
   * Setup a canvas (wrapper) element for the visualization.
   * 
   * @param svgp Plot context
   * @return Wrapper element with appropriate view box.
   */
  public Element setupCanvas(SVGPlot svgp) {
    Element layer = SVGUtil.svgElement(svgp.getDocument(), SVGConstants.SVG_SVG_TAG);
    // Some default for the view box.
    SVGUtil.setAtt(layer, SVGConstants.SVG_VIEW_BOX_ATTRIBUTE, "-1.2 -1.2 2.4 2.4");    
    return layer;
  }
}