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
import fr.michaelm.jump.plugin.aggregation.AggregationFunction;

import java.util.*;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;


/**
 * An aggregation function to count all features passed in.
 *
 * @author Michael Michaud
 * @version 1.0
 */

public class Count extends AggregationFunction {

    public Count() {
        super(I18N.getInstance("fr.michaelm.jump.plugin.aggregation")
            .get("function.Count"));
    }

    public Object aggregate(List<Feature> list, String sourceAttr,
                            boolean ignore_null, Object param) {
        if (ignore_null) {
            int nnc = 0;
            for (Feature f : list) {
                if (f.getAttribute(sourceAttr) != null) nnc++;
            }
            return nnc;
        } else return list.size();
    }

    public AttributeType getReturnAttributeType(AttributeType inputType) {
        return AttributeType.INTEGER;
    }

}
