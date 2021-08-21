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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * An aggregation function to compute the min value of an attribute
 * in a feature collection.
 *
 * @author Michael Michaud
 * @version 1.0
 */

public class Median extends AggregationFunction {


    public Median() {
        super(I18N.getInstance("fr.michaelm.jump.plugin.aggregation")
            .get("function.Median"));
    }


    public Object aggregate(List<Feature> list,
                            String sourceAttr,
                            boolean ignore_null,
                            Object param) {
        if (list.isEmpty()) return null;
        FeatureSchema schema = list.get(0).getSchema();
        int index = schema.getAttributeIndex(sourceAttr);
        AttributeType type = schema.getAttributeType(index);
        if (type == AttributeType.INTEGER) return medianInteger(list, index, ignore_null, param);
        if (type == AttributeType.DOUBLE) return medianDouble(list, index, ignore_null, param);
        if (type == AttributeType.STRING) return medianString(list, index, ignore_null, param);
        if (type == AttributeType.DATE) return medianDate(list, index, ignore_null, param);
        return null;
    }

    public Integer medianInteger(List<Feature> list, int index,
        boolean ignore_null, Object param) {
        List<Integer> values = new ArrayList<>();
        for (Feature f : list) {
            Integer value = f.getInteger(index);
            if (ignore_null) {
                if (value==null || value==0) continue;
            }
            values.add(value);
        }
        Collections.sort(values);
        if (values.size()>0) return values.get(values.size()/2);
        else return null;
    }


    public Double medianDouble(List<Feature> list, int index,
        boolean ignore_null, Object param) {
        List<Double> values = new ArrayList<Double>();
        for (Feature f : list) {
            Double value = f.getDouble(index);
            if (ignore_null) {
                if (value==null || value==0) continue;
            }
            values.add(value);
        }
        Collections.sort(values);
        if (values.size()>0) return values.get(values.size()/2);
        else return null;
    }


    public String medianString(List<Feature> list, int index,
        boolean ignore_null, Object param) {
        List<String> values = new ArrayList<>();
        for (Feature f : list) {
            String value = f.getString(index);
            if (ignore_null) {
                if (value==null || value.trim().length()==0) continue;
            }
            values.add(value);
        }
        Collections.sort(values);
        if (values.size()>0) return values.get(values.size()/2);
        else return null;
    }


    public Date medianDate(List<Feature> list, int index,
        boolean ignore_null, Object param) {
        List<Date> values = new ArrayList<>();
        for (Feature f : list) {
            Date value = (Date)f.getAttribute(index);
            if (ignore_null) {
                if (value==null) continue;
            }
            values.add(value);
        }
        Collections.sort(values);
        if (values.size()>0) return values.get(values.size()/2);
        else return null;
    }


    public AttributeType getReturnAttributeType(AttributeType inputType) {
        if (inputType == AttributeType.GEOMETRY) return null;
        if (inputType == AttributeType.DOUBLE) return null;
        else return inputType;
    }

}
