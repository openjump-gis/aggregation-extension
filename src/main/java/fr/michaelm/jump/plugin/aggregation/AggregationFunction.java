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

import java.util.*;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;

import fr.michaelm.jump.plugin.aggregation.function.*;

/**
 * A feature attribute aggregator.
 *
 * [TODO] Add parameters attribute and accessor,
 * create subclass outside this abstract class
 * create a map in this object to register new AggregationFunction
 *
 * @author Michael Michaud
 * @version 1.0
 */
public abstract class AggregationFunction {
    
    static AggregationFunction[] methods = {
        new Count(),
		    new Union(),
        new Sum(),
        new Average(),
        new Min(),
        new Max(),
        new StandardDeviation(),
		    new Median(),
		    new Minority(),
		    new Majority(),
        new ConcatenateAll(),
        new ConcatenateDistinct()
    };

    static List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (AggregationFunction method : methods) {
          names.add(method.name);
        }
        return names;
    }
    
    static List<String> getNames(AttributeType type) {
        List<String> names = new ArrayList<>();
        for (AggregationFunction method : methods) {
            if (method.getReturnAttributeType(type) != null) {
                names.add(method.name);
            }
        }
        return names;
    }
    
    static AggregationFunction getFunction(String name) {
        for (AggregationFunction method : methods) {
          if (method.name.equals(name))
            return method;
        }
        return null;
    }
    
    private final String name;
    private final String description;
    private Object parameter;
    
    public AggregationFunction(String name) {
        this(name, null);
    }
    
    public AggregationFunction(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public AggregationFunction(String name, String description, Object param) {
        this.name = name;
        this.description = description;
        this.parameter = param;
    }
    
    /**
     * Return the name of this aggregation function.
     */
    public String getName() { return name; }
    
    /**
     * Return a description of this aggregation function.
     */
    public String getDescription() { return description; }
    
    /**
     * Return this function parameter
     */
    public Object getParameter() { return parameter; }
    
    /**
     * Return true if this function needs a parameter
     */
     public boolean hasParameter() {return parameter!=null;}
    
    /**
     * Return this AggregationFunction return AttributeType for
     * input values having inputType AttributeType.
     * The return type is generally the same as the input type,
     * but it may be different (ex. return type for count() function is
     * AttributeType.INTEGER whatever the input type is),
     * and the method return null if this function is not available for
     * the inputType (ex. mean() is not available for AttributeType.GEOMETRY
     * inputType.
     * @param inputType the AttributeType of values to aggregate
     * @return AttributeType of aggregated values.
     */
    public abstract AttributeType getReturnAttributeType(AttributeType inputType);
    
    public abstract Object aggregate(List<Feature> list, String sourceAttr,
		                             boolean ignore_null, Object param);
    
}
