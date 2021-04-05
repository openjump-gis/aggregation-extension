/*
 * (C) 2021 Michaël Michaud
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * For more information, contact:
 *
 * m.michael.michaud@orange.fr
 *
 */

package fr.michaelm.jump.plugin.aggregation;

import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.StandardCategoryNames;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedBasePlugIn;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.MenuNames;
import com.vividsolutions.jump.workbench.ui.MultiInputDialog;
import fr.michaelm.jump.plugin.aggregation.function.*;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;

import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This plugin makes it possible to aggregate attribute information of one layer
 * on features of another layer using a geometry predicate.
 * For example, let "Country" and "City" be two layers. City features have
 * a population attribute.
 * For each Country of the target layer, you can compute the sum (or average,
 * or min or max...) of the population attribute, where Citiy intersects
 * Country.
 * The plugin can aggregate Geometry (with union aggregation function) as well
 * as Geometry length and Geometry area which are considered as special
 * attributes.
 * There is a special option allowing to consider only the intersecting part
 * of the source attribute to aggregate its geometry, geometry length or
 * geometry area.
 */
// History
// 1.0.0 (2021-04-05) refactoring for java8 / OpenJUMP2 / jts1.18
// 0.2.10 (2017-01-03) fix the initial list of operators
// 0.2.9 (2016-12-01) fix option new_layer which did not work
// 0.2.8 (2013-11-30) add italian translation
// 0.2.7 (2013-03-20) add finnish language file
// 0.2.6 (2013-02-17) put the result in a new layer is now an option
public class AggregatePlugIn extends ThreadedBasePlugIn {

    // Lazy initialization of I18N variables
    private static String AGGREGATION_OPTIONS; // Dialog title

    private static String SRC_LAYER;
    private static String TGT_LAYER;
    private static String RELATION;
    private static String PARAMETER;
    private static String INTERSECTION;
    private static String IGNORE_NULL;
    private static String ATTRIBUTE;
    private static String FUNCTION;

    private static String LENGTH;
    private static String AREA;

    
    private static String NEW_LAYER;

    private Layer srcLayer, tgtLayer;
    private FeatureSchema srcSchema;
    private String srcGeometryName;

    // Get plain intersects as the first predicate (more interesting,
    // with a special option only available for this predicate)
    private GeometryPredicate relation =
        GeometryPredicate.getPredicate(GeometryPredicate.getNames().get(1));
    private final double[] gp_params = new double[]{100.0};

    private String attribute = "";
    private AttributeType atype = AttributeType.GEOMETRY;
    private boolean intersection = false;
    private boolean ignore_null = true;
    private AggregationFunction function =
        AggregationFunction.getFunction(I18NPlug.getI18N("function.Count"));
    private boolean new_layer = false;

    public AggregatePlugIn() {
    }

    public void initialize(PlugInContext context) {

        String AGGREGATION  = I18NPlug.getI18N("aggregation");

        AGGREGATION_OPTIONS = I18NPlug.getI18N("aggregation-options");
        
        SRC_LAYER    = I18NPlug.getI18N("src-layer");
        TGT_LAYER    = I18NPlug.getI18N("tgt-layer");
        RELATION     = I18NPlug.getI18N("relation");
        PARAMETER    = I18NPlug.getI18N("parameter");
        INTERSECTION = I18NPlug.getI18N("intersection");
        IGNORE_NULL  = I18NPlug.getI18N("ignore-null");
        ATTRIBUTE    = I18NPlug.getI18N("attribute");
        FUNCTION     = I18NPlug.getI18N("function");

        LENGTH       = I18NPlug.getI18N("length");
        AREA         = I18NPlug.getI18N("area");
        
        NEW_LAYER    = I18NPlug.getI18N("new-layer");


        context.getFeatureInstaller().addMainMenuPlugin(
          this, new String[]{MenuNames.PLUGINS, MenuNames.TOOLS_ANALYSIS},
          AGGREGATION + "...",
          false, 
          new ImageIcon(this.getClass().getResource("images/Aggregation.png"), ""),
          new MultiEnableCheck()
          .add(context.getCheckFactory().createTaskWindowMustBeActiveCheck())
          .add(context.getCheckFactory().createAtLeastNLayersMustExistCheck(2)));
    }


    public boolean execute(final PlugInContext context) {

        final MultiInputDialog dialog = new MultiInputDialog(
            context.getWorkbenchFrame(), AGGREGATION_OPTIONS, true);

        // Keep layer as default if still exists
        if (tgtLayer==null || context.getLayerManager().indexOf(tgtLayer)==-1) {
            tgtLayer = context.getCandidateLayer(0);
        }
        if (srcLayer==null || context.getLayerManager().indexOf(srcLayer)==-1) {
            srcLayer = context.getCandidateLayer(1);
        }
        srcSchema = srcLayer.getFeatureCollectionWrapper().getFeatureSchema();
        srcGeometryName = srcSchema.getAttributeName(srcSchema.getGeometryIndex());

        // Choose Source layer and destination layer
        @SuppressWarnings("unchecked")
        final JComboBox<Layer> jcb_srcLayer =
            dialog.addLayerComboBox(SRC_LAYER, srcLayer, null, context.getLayerManager());

        dialog.addLayerComboBox(TGT_LAYER, tgtLayer, null, context.getLayerManager());
            
        dialog.addCheckBox(NEW_LAYER, new_layer, "");

        // Choose Spatial predicate
        final JComboBox<String> jcb_relation =
            dialog.addComboBox(RELATION, relation.getName(), GeometryPredicate.getNames(), "");
        final JTextField jtf_param = dialog.addDoubleField(PARAMETER, gp_params[0], 12, "");
        jtf_param.setEnabled(relation.getParameterCount() > 0);

        // Choose an Attribute and the aggregation function
        final JComboBox<String> jcb_attribute = dialog.addComboBox(ATTRIBUTE,
            getAttributes().get(0), getAttributes(), "");
        attribute = dialog.getText(ATTRIBUTE);

        final JCheckBox jcb_intersection = dialog.addCheckBox(INTERSECTION, intersection, "");
        jcb_intersection.setSelected(false);
        jcb_intersection.setEnabled(isIntersectionOptionUseful());

        final JCheckBox jcb_ignore_null = dialog.addCheckBox(IGNORE_NULL, ignore_null, "");
        jcb_ignore_null.setSelected(true);
        jcb_ignore_null.setEnabled(false);

        final JComboBox<String> jcb_aggregation =
            dialog.addComboBox(FUNCTION, function.getName(),
                getFunctions(srcSchema.getAttributeType(srcSchema.getAttributeIndex(attribute))), "");

        dialog.setSideBarImage(createSideBarImage());
        dialog.setSideBarDescription(getDescription());

        ////////////////////////////////////////////////////////////////////////
        // UI ELEMENTS INTERACTIONS
        ////////////////////////////////////////////////////////////////////////

        jcb_srcLayer.addActionListener(e -> {
            srcLayer = dialog.getLayer(SRC_LAYER);
            srcSchema = srcLayer.getFeatureCollectionWrapper().getFeatureSchema();
            srcGeometryName = srcSchema.getAttributeName(srcSchema.getGeometryIndex());

            // Select all possible attributes for source layer
            List<String> listA = getAttributes();
            jcb_attribute.setModel(new DefaultComboBoxModel<>(
                listA.toArray(new String[0])));
            attribute = listA.get(0);
            atype = srcSchema.getAttributeType(0);
            // select all possible aggregation function for default attribute
            List<String> listF = getFunctions(atype);
            jcb_aggregation.setModel(new DefaultComboBoxModel<>(
                listF.toArray(new String[0])));
            function = AggregationFunction.getFunction(listF.get(0));
            jcb_intersection.setSelected(getIntersectionOptionDefault());
            jcb_intersection.setEnabled(isIntersectionOptionUseful());
            intersection = dialog.getBoolean(INTERSECTION);
            jcb_ignore_null.setSelected(getIgnoreNullDefault());
            jcb_ignore_null.setEnabled(isIgnoreNullOptionUseful());
            ignore_null = dialog.getBoolean(IGNORE_NULL);
            dialog.setSideBarImage(createSideBarImage());
            dialog.setSideBarDescription(getDescription());
        });


        jcb_attribute.addActionListener(e -> {
            attribute = dialog.getText(ATTRIBUTE);
            // available functions for length and area attributes
            // are the same as the one for double attributes
            if (attribute.endsWith("."+LENGTH) ||
                attribute.endsWith("."+AREA)) {
                atype = AttributeType.DOUBLE;
            }
            else {
                atype = srcSchema.getAttributeType(srcSchema.getAttributeIndex(attribute));
            }
            List<String> listF = getFunctions(atype);
            jcb_aggregation.setModel(new DefaultComboBoxModel<>(
                listF.toArray(new String[0])));
            function = AggregationFunction.getFunction(listF.get(0));
            // check if intersection option must be made available
            jcb_intersection.setSelected(getIntersectionOptionDefault());
            jcb_intersection.setEnabled(isIntersectionOptionUseful());
            intersection = dialog.getBoolean(INTERSECTION);
            jcb_ignore_null.setSelected(getIgnoreNullDefault());
            jcb_ignore_null.setEnabled(isIgnoreNullOptionUseful());
            ignore_null = dialog.getBoolean(IGNORE_NULL);
            dialog.setSideBarImage(createSideBarImage());
            dialog.setSideBarDescription(getDescription());
        });

        jcb_relation.addActionListener(e -> {
            relation = GeometryPredicate.getPredicate(dialog.getText(RELATION));
            if (relation != null && relation.getParameterCount() > 0) {
                jtf_param.setEnabled(true);
            }
            else jtf_param.setEnabled(false);
            jcb_intersection.setSelected(getIntersectionOptionDefault());
            jcb_intersection.setEnabled(isIntersectionOptionUseful());
            intersection = dialog.getBoolean(INTERSECTION);
            dialog.setSideBarImage(createSideBarImage());
            dialog.setSideBarDescription(getDescription());
        });

        jcb_intersection.addActionListener(e -> {
            intersection = jcb_intersection.isSelected();
            dialog.setSideBarImage(createSideBarImage());
            dialog.setSideBarDescription(getDescription());
        });

        jcb_ignore_null.addActionListener(e -> {
            ignore_null = jcb_ignore_null.isSelected();
            dialog.setSideBarImage(createSideBarImage());
            dialog.setSideBarDescription(getDescription());
        });

        jcb_aggregation.addActionListener(e -> {
            function = AggregationFunction.getFunction(dialog.getText(FUNCTION));
            jcb_intersection.setSelected(getIntersectionOptionDefault());
            jcb_intersection.setEnabled(isIntersectionOptionUseful());
            intersection = dialog.getBoolean(INTERSECTION);
            jcb_ignore_null.setSelected(getIgnoreNullDefault());
            jcb_ignore_null.setEnabled(isIgnoreNullOptionUseful());
            ignore_null = dialog.getBoolean(IGNORE_NULL);
            dialog.setSideBarImage(createSideBarImage());
            dialog.setSideBarDescription(getDescription());
        });

        GUIUtil.centreOnWindow(dialog);
        dialog.setVisible(true);
        if (dialog.wasOKPressed()) {
            srcLayer = dialog.getLayer(SRC_LAYER);
            srcSchema = srcLayer.getFeatureCollectionWrapper().getFeatureSchema();
            srcGeometryName = srcSchema.getAttributeName(srcSchema.getGeometryIndex());
            tgtLayer = dialog.getLayer(TGT_LAYER);
            relation = GeometryPredicate.getPredicate(dialog.getText(RELATION));
            gp_params[0] = dialog.getDouble(PARAMETER);
            intersection = jcb_intersection.isEnabled() && dialog.getBoolean(INTERSECTION);
            ignore_null = dialog.getBoolean(IGNORE_NULL);
            attribute = dialog.getText(ATTRIBUTE);
            if (attribute.startsWith(srcGeometryName+".")) atype = AttributeType.DOUBLE;
            else atype = srcSchema.getAttributeType(srcSchema.getAttributeIndex(attribute));
            function = AggregationFunction.getFunction(dialog.getText(FUNCTION));
            new_layer = dialog.getBoolean(NEW_LAYER);
            return true;
        }
        else return false;
    }
 
    public void run(TaskMonitor monitor, PlugInContext context) {
        monitor.allowCancellationRequests();
        monitor.report(I18NPlug.getI18N("monitor.aggregation-of") +
            srcLayer.getName() + I18NPlug.getI18N("monitor.on") + tgtLayer.getName());
 
        FeatureSchema tgtSchema = tgtLayer.getFeatureCollectionWrapper().getFeatureSchema();
        int feature_count = tgtLayer.getFeatureCollectionWrapper().size();
 
        // Creation du schema pour la couche agrégée
        FeatureSchema newSchema = tgtSchema.clone();
        AttributeType aType;
        if (attribute.equals(srcGeometryName)) aType = AttributeType.GEOMETRY;
        else if (attribute.equals(srcGeometryName+"."+LENGTH)) aType = AttributeType.DOUBLE;
        else if (attribute.equals(srcGeometryName+"."+AREA)) aType = AttributeType.DOUBLE;
        else aType = srcSchema.getAttributeType(srcSchema.getAttributeIndex(attribute));
 
        // Special schema to aggregate geometry related information
        FeatureSchema geometry_schema = new FeatureSchema();
        geometry_schema.addAttribute(srcGeometryName, AttributeType.GEOMETRY);
        FeatureSchema length_schema = new FeatureSchema();
        length_schema.addAttribute(srcGeometryName+"."+LENGTH, AttributeType.DOUBLE);
        FeatureSchema area_schema = new FeatureSchema();
        area_schema.addAttribute(srcGeometryName+"."+AREA, AttributeType.DOUBLE);
 
        AttributeType returnType = function.getReturnAttributeType(aType);
        String tgtAttributeName = attribute.replaceAll("^"+srcGeometryName+"(\\.)?","");
        tgtAttributeName = tgtAttributeName.length()>0?"."+tgtAttributeName:tgtAttributeName;
        if (returnType != AttributeType.GEOMETRY) {
            newSchema.addAttribute(function.getName() + "_" +
                srcLayer.getName() + tgtAttributeName, returnType);
        }

        FeatureCollection newDataset = new FeatureDataset(newSchema);
        
        IndexedFeatureCollection ifc = new IndexedFeatureCollection(
            srcLayer.getFeatureCollectionWrapper(), new STRtree());
        int count = 0;
 
        // Main loop over target feature collection
        for (Feature feature : tgtLayer.getFeatureCollectionWrapper().getFeatures()) {
            Geometry geometry = feature.getGeometry();
            monitor.report(++count, feature_count, I18NPlug.getI18N("monitor.features"));
            Feature newFeature = new BasicFeature(newSchema);
            for (int i = 0 ; i < tgtSchema.getAttributeCount() ; i++) {
                newFeature.setAttribute(i, feature.getAttribute(i));
            }
            Envelope queryEnvelope = feature.getGeometry().getEnvelopeInternal();
            if (relation.getParameterCount()==1) queryEnvelope.expandBy(gp_params[0]);
            List<?> candidates = ifc.query(feature.getGeometry().getEnvelopeInternal());
            List<Feature> relatedFeatures = new ArrayList<>();
            for (Object o : candidates) {
                Feature f = (Feature)o;
                Geometry g = intersection?
                    f.getGeometry().intersection(geometry):
                    f.getGeometry();
                if (relation.isTrue(f.getGeometry(), feature.getGeometry(), gp_params)) {
                    if (attribute.equals(srcGeometryName)) {
                        f = new BasicFeature(geometry_schema);
                        f.setGeometry(g);
                    }
                    else if (attribute.equals(srcGeometryName+"."+LENGTH)) {
                        f = new BasicFeature(length_schema);
                        f.setAttribute(srcGeometryName+"."+LENGTH, g.getLength());
                    }
                    else if (attribute.equals(srcGeometryName+"."+AREA)) {
                        f = new BasicFeature(area_schema);
                        f.setAttribute(srcGeometryName+"."+AREA, g.getArea());
                    }
                    relatedFeatures.add(f);
                }
            }
            Object agg = function.aggregate(relatedFeatures, attribute, ignore_null, null);
            if (agg instanceof Geometry) {
                if (((Geometry)agg).isEmpty()) newFeature = null;
                else newFeature.setGeometry((Geometry)agg);
            }
            else newFeature.setAttribute(function.getName() + "_" +
                srcLayer.getName() + tgtAttributeName, agg);
            if (newFeature != null) newDataset.add(newFeature);
        }
        if (new_layer) {
            context.getLayerManager().addLayer(StandardCategoryNames.RESULT,
                tgtLayer.getName() + "_" + function.getName() + "_" +
                srcLayer.getName() + tgtAttributeName, newDataset);
        } else {
            tgtLayer.setFeatureCollection(newDataset);
        }
    }
 
    private List<String> getAttributes() {
        List<String> list = new ArrayList<>();
        list.add(srcGeometryName);
        list.add(srcGeometryName + "." + LENGTH);
        list.add(srcGeometryName + "." + AREA);
        for (int i = 0 ; i < srcSchema.getAttributeCount() ; i++) {
            if (srcSchema.getAttributeType(i) != AttributeType.GEOMETRY) {
                list.add(srcSchema.getAttributeName(i));
            }
        }
        return list;
    }
 
    private List<String> getFunctions(AttributeType type) {
        return AggregationFunction.getNames(type);
    }

    private boolean isIgnoreNullOptionUseful() {
        return function instanceof Count && !(attribute.startsWith(srcGeometryName));

    }

    private boolean getIgnoreNullDefault() {
        if (isIgnoreNullOptionUseful()) return true;
        else return true;
    }

    private boolean isIntersectionOptionUseful() {
        // Option taking only the intersection part of the related geometry
        // is useful with PlainIntersects predicate. It would be equivalent
        // with Intersects predicate
        return ((relation.getName().equals(I18NPlug.getI18N("predicate.PlainIntersects"))) &&
                (attribute.equals(srcGeometryName) ||
                 attribute.equals(srcGeometryName+"."+LENGTH) ||
                 attribute.equals(srcGeometryName+"."+AREA)));
    }

    private boolean getIntersectionOptionDefault() {
        // Intersection computation is proposed as a default option for
        // PlainIntersects predicate
        // Geometry, Geometry.Length and Geometry.Area attributes
        // function different from count
        return ((relation.getName().equals(I18NPlug.getI18N("predicate.PlainIntersects"))) &&
                (attribute.equals(srcGeometryName) ||
                 attribute.equals(srcGeometryName+"."+LENGTH) ||
                 attribute.equals(srcGeometryName+"."+AREA)) &&
                !(function instanceof Count));
    }
    
    private String getDescription() {
        String descr = "";
        descr += I18NPlug.getI18N("example") + " \"";
        descr += function.getName() + "\":\nf";
        if (function instanceof Union) descr += "(Polygon1,Polygon2) = MultiPolygon";
        else if (function instanceof Count && ignore_null) descr += "(a1,a2,null) = 2";
        else if (function instanceof Count && !ignore_null) descr += "(a1,a2,null) = 3";
        else if (function instanceof Sum) descr += "(1,5,null) = 6";
        else if (function instanceof Average) descr += "(1,5,null) = 3";
        else if (function instanceof Min) descr += "(1,5,null) = 1";
        else if (function instanceof Max) descr += "(-1,-5,null) = -1";
        else if (function instanceof StandardDeviation) descr += "(-1,0,0,1,null) = 0.82";
        else if (function instanceof Median) descr += "(1,2,4,8,16,null,null) = 4";
        else if (function instanceof Minority) descr += "(1,2,2,3,3,3,null) = 1";
        else if (function instanceof Majority) descr += "(1,2,2,3,3,3,null) = 3";
        else if (function instanceof ConcatenateAll) descr += "(A,null,D,A) = \"A|D|A\"";
        else if (function instanceof ConcatenateDistinct) descr += "(A,null,D,A) = \"A|D\"";
        return descr + "";
    }

    private Icon createSideBarImage() {
        String path = "";
        String gp = relation.getClass().getName().substring(relation.getClass().getName().lastIndexOf("$") + 1);
        if ((relation.getName().equals(I18NPlug.getI18N("predicate.Intersects")) ||
            relation.getName().equals(I18NPlug.getI18N("predicate.PlainIntersects"))) &&
            isIntersectionOptionUseful() && intersection) {
            gp += "I";
        }
        path = "images/" + path + gp + ".png";
        URL imgURL = this.getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, "");
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

}
