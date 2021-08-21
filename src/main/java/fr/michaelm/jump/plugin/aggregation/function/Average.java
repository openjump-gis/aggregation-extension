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
 * An aggregation function to compute the average value of an attribute
 * in a feature collection.
 *
 * @author Michael Michaud
 * @version 1.0
 */

public class Average extends AggregationFunction {

    public Average() {
        super(I18N.getInstance("fr.michaelm.jump.plugin.aggregation")
            .get("function.Average"));
    }


    public Object aggregate(List<Feature> list, String sourceAttr, boolean ignore_null, Object param) {
        if (list.isEmpty()) return null;
        FeatureSchema schema = list.get(0).getSchema();
        int index = schema.getAttributeIndex(sourceAttr);
        AttributeType type = schema.getAttributeType(index);
        if (type == AttributeType.INTEGER) return averageInteger(list, index);
        else if (type == AttributeType.DOUBLE) return averageDouble(list, index);
        else if (type == AttributeType.DATE) return averageDate(list, index);
        else return null;
    }


    private Double averageInteger(List<Feature> list, int index) {
        int n = 0;
        double sum = 0.0;
        for (Feature f : list) {
            if (f.getAttribute(index)!=null) {
                sum += f.getInteger(index);
                n++;
            }
        }
        return sum / n;
    }


    private Double averageDouble(List<Feature> list, int index) {
        int n = 0;
        double sum = 0.0;
        for (Feature f : list) {
            if (f.getAttribute(index)!=null) {
                sum += f.getDouble(index);
                n++;
            }
        }
        return sum / n;
    }


    private Date averageDate(List<Feature> list, int index) {
        long l = 0;
        int n = 0;
        for (Feature f : list) {
            if (f.getAttribute(index)!=null) {
                l += ((Date)f.getAttribute(index)).getTime();
                n++;
            }
        }
        return new Date(l/n);
    }


    public AttributeType getReturnAttributeType(AttributeType inputType) {
        if (inputType == AttributeType.GEOMETRY) return null;
        else if (inputType == AttributeType.STRING) return null;
        else if (inputType == AttributeType.INTEGER ||
                 inputType == AttributeType.DOUBLE) return AttributeType.DOUBLE;
        else if (inputType == AttributeType.DATE) return AttributeType.DATE;
        else return null;
    }

}
