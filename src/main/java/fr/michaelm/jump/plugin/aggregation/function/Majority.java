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

import java.util.*;


/**
 * An aggregation function to compute the min value of an attribute
 * in a feature collection.
 *
 * @author Michael Michaud
 * @version 1.0
 */

public class Majority extends AggregationFunction {

    public Majority() {
        super(I18N.getInstance("fr.michaelm.jump.plugin.aggregation")
            .get("function.Majority"));
    }

    public Object aggregate(List<Feature> list,
                            String sourceAttr,
                            boolean ignore_null,
                            Object param) {
        if (list.isEmpty()) return null;
        FeatureSchema schema = list.get(0).getSchema();
        int index = schema.getAttributeIndex(sourceAttr);

        // count features for each attribute value
        HashMap<Object,Integer> map = new HashMap<>();
        for (Feature f : list) {
            Object value = f.getAttribute(index);
            if (ignore_null) {
                if (value==null) continue;
                String sval = value.toString().trim();
                if (sval.length()==0 || sval.equals("0") || sval.equals("0.0")) continue;
            }
            if (!map.containsKey(value)) map.put(value, 1);
            else map.put(value, map.get(value) + 1);
        }
        // find the min occurrence 
        Integer max = Collections.max(map.values());
        // find the first attribute value having min occurrence
        for (Map.Entry<Object,Integer> entry : map.entrySet()) {
            if (entry.getValue().equals(max)) return entry.getKey();
        }
        return null;
    }

    public AttributeType getReturnAttributeType(AttributeType inputType) {
        if (inputType == AttributeType.GEOMETRY) return null;
        if (inputType == AttributeType.DOUBLE) return null;
        else return inputType;
    }

}
