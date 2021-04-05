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

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import fr.michaelm.jump.plugin.aggregation.AggregationFunction;
import fr.michaelm.jump.plugin.aggregation.I18NPlug;

import java.util.Date;
import java.util.List;


/**
 * An aggregation function to compute the max value of an attribute
 * in a feature collection.
 *
 * @author Michael Michaud
 * @version 1.0
 */

public class Max extends AggregationFunction {

    public Max() {
        super(I18NPlug.getI18N("function.Max"));
    }

    public Object aggregate(List<Feature> list, String sourceAttr, boolean ignore_null, Object param) {
        if (list.isEmpty()) return null;
        FeatureSchema schema = list.get(0).getSchema();
        int index = schema.getAttributeIndex(sourceAttr);
        AttributeType type = schema.getAttributeType(index);
        if (type == AttributeType.INTEGER) return maxInteger(list, index);
        else if (type == AttributeType.DOUBLE) return maxDouble(list, index);
        else if (type == AttributeType.DATE) return maxDate(list, index);
        else if (type == AttributeType.STRING) return maxString(list, index);
        else return null;
    }


    private Integer maxInteger(List<Feature> list, int index) {
        int max = Integer.MIN_VALUE;
        for (Feature f : list) {
            if (f.getAttribute(index)!=null && f.getInteger(index)>max) max = f.getInteger(index);
        }
        return max==Integer.MIN_VALUE?null: max;
    }


    private Double maxDouble(List<Feature> list, int index) {
        double max = Double.MIN_VALUE;
        for (Feature f : list) {
            if (f.getAttribute(index)!=null && f.getDouble(index)>max) max = f.getDouble(index);
        }
        return max==Double.MIN_VALUE?null: max;
    }


    private Date maxDate(List<Feature> list, int index) {
        long max = Long.MIN_VALUE;
        for (Feature f : list) {
            if (f.getAttribute(index)!=null && ((Date)f.getAttribute(index)).getTime()>max) max = ((Date)f.getAttribute(index)).getTime();
        }
        return max==Long.MIN_VALUE?null:new Date(max);
    }


    private String maxString(List<Feature> list, int index) {
        String max = null;
        for (Feature f : list) {
            if (f.getAttribute(index)!=null) {
                if (max==null) max = f.getString(index);
                else if (f.getString(index).compareToIgnoreCase(max)>0) max = f.getString(index);
            }
        }
        return max;
    }


    public AttributeType getReturnAttributeType(AttributeType inputType) {
        if (inputType == AttributeType.GEOMETRY) return null;
        else return inputType;
    }

}
