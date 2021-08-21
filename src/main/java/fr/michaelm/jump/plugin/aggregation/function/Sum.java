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
 * An aggregation function to union geometries of input features.
 *
 * @author Michael Michaud
 * @version 1.0
 */

public class Sum extends AggregationFunction {


    public Sum() {
        super(I18N.getInstance("fr.michaelm.jump.plugin.aggregation")
            .get("function.Sum"));
    }


    public Object aggregate(List<Feature> list,
                            String sourceAttr,
                            boolean ignore_null,
                            Object param) {
        if (list.isEmpty()) return null;
        FeatureSchema schema = list.get(0).getSchema();
        int index = schema.getAttributeIndex(sourceAttr);
        AttributeType type = schema.getAttributeType(index);
        if (type == AttributeType.INTEGER) return sumInteger(list, index);
        else if (type == AttributeType.DOUBLE) return sumDouble(list, index);
        else if (type == AttributeType.DATE) return sumDate(list, index);
        else if (type == AttributeType.STRING) return sumString(list, index);
        else return null;
    }


    private Integer sumInteger(List<Feature> list, int index) {
        int sum = 0;
        for (Feature f : list) {
            if (f.getAttribute(index)!=null) sum += f.getInteger(index);
        }
        return sum;
    }


    private Double sumDouble(List<Feature> list, int index) {
        double sum = 0.0;
        for (Feature f : list) {
            if (f.getAttribute(index)!=null) sum += f.getDouble(index);
        }
        return sum;
    }


    private Date sumDate(List<Feature> list, int index) {
        long sum = 0;
        for (Feature f : list) {
            if (f.getAttribute(index)!=null) sum += ((Date)f.getAttribute(index)).getTime();
        }
        return new Date(sum);
    }


    private String sumString(List<Feature> list, int index) {
        StringBuilder sum = new StringBuilder();
        for (Feature f : list) {
            if (f.getAttribute(index)!=null) sum.append(f.getString(index)).append("|");
        }
        if (sum.length()>1)sum.setLength(sum.length()-1);
        return sum.toString();
    }


    public AttributeType getReturnAttributeType(AttributeType inputType) {
        if (inputType == AttributeType.GEOMETRY) return null;
        else return inputType;
    }

}
