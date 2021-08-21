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

package fr.michaelm.jump.plugin.aggregation.function;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import fr.michaelm.jump.plugin.aggregation.AggregationFunction;

import java.util.Date;
import java.util.List;


/**
 * An aggregation function to compute the min value of an attribute
 * in a feature collection.
 *
 * @author Michael Michaud
 * @version 1.0
 */

public class Min extends AggregationFunction {


    public Min() {
        super(I18N.getInstance("fr.michaelm.jump.plugin.aggregation")
            .get("function.Min"));
    }


    public Object aggregate(List<Feature> list, String sourceAttr, boolean ignore_null, Object param) {
        if (list.isEmpty()) return null;
        FeatureSchema schema = list.get(0).getSchema();
        int index = schema.getAttributeIndex(sourceAttr);
        AttributeType type = schema.getAttributeType(index);
        if (type == AttributeType.INTEGER) return minInteger(list, index);
        else if (type == AttributeType.DOUBLE) return minDouble(list, index);
        else if (type == AttributeType.DATE) return minDate(list, index);
        else if (type == AttributeType.STRING) return minString(list, index);
        else return null;
    }


    private Integer minInteger(List<Feature> list, int index) {
        int min = Integer.MAX_VALUE;
        for (Feature f : list) {
            if (f.getAttribute(index)!=null && f.getInteger(index)<min) min = f.getInteger(index);
        }
        return min==Integer.MAX_VALUE?null: min;
    }


    private Double minDouble(List<Feature> list, int index) {
        double min = Double.MAX_VALUE;
        for (Feature f : list) {
            if (f.getAttribute(index)!=null && f.getDouble(index)<min) min = f.getDouble(index);
        }
        return min==Double.MAX_VALUE?null: min;
    }


    private Date minDate(List<Feature> list, int index) {
        long min = Long.MAX_VALUE;
        for (Feature f : list) {
            if (f.getAttribute(index)!=null && ((Date)f.getAttribute(index)).getTime()<min) min = ((Date)f.getAttribute(index)).getTime();
        }
        return min==Long.MAX_VALUE?null:new Date(min);
    }


    private String minString(List<Feature> list, int index) {
        String min = null;
        for (Feature f : list) {
            if (f.getAttribute(index)!=null) {
                if (min==null) min = f.getString(index);
                else if (f.getString(index).compareToIgnoreCase(min)<0) min = f.getString(index);
            }
        }
        return min;
    }


    public AttributeType getReturnAttributeType(AttributeType inputType) {
        if (inputType == AttributeType.GEOMETRY) return null;
        else return inputType;
    }

}
