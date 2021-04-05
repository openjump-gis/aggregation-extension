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

import fr.michaelm.jump.plugin.aggregation.AggregationFunction;
import fr.michaelm.jump.plugin.aggregation.I18NPlug;

import java.util.*;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;

/**
 * An aggregation function to concatenate Strings.
 *
 * @author Michael Michaud
 * @version 1.0
 */

public class ConcatenateAll extends AggregationFunction {

    public ConcatenateAll() {
        super(I18NPlug.getI18N("function.ConcatenateAll"));
    }

    public Object aggregate(List<Feature> list, String sourceAttr, boolean ignore_null, Object param) {
        if (list.isEmpty()) return null;
        FeatureSchema schema = list.get(0).getSchema();
        int index = schema.getAttributeIndex(sourceAttr);
        StringBuilder sb = new StringBuilder();
        String sep = param==null?"|":param.toString();
        int count = 0;
        for (Feature f : list) {
            if (f.getAttribute(index)!=null) {
                if (0<count++) sb.append(sep).append(f.getAttribute(index).toString());
                else sb.append(f.getString(index));
            }
        }
        return sb.toString();
    }

    public AttributeType getReturnAttributeType(AttributeType inputType) {
        if (inputType == AttributeType.GEOMETRY) return null;
        else return inputType;
    }

}
