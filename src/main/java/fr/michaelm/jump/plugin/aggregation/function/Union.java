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
import fr.michaelm.jump.plugin.aggregation.AggregationFunction;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import java.util.ArrayList;
import java.util.List;


/**
 * An aggregation function to union geometries of input features.
 *
 * @author Michael Michaud
 * @version 1.0
 */

public class Union extends AggregationFunction {


    public Union() {
        super(I18N.getInstance("fr.michaelm.jump.plugin.aggregation")
            .get("function.Union"));
    }


    public Geometry aggregate(List<Feature> list,
                              String sourceAttr,
                              boolean ignore_null,
                              Object param) {
        if (list.size()==0) return new GeometryFactory().createGeometryCollection(new Geometry[0]);
        List<Geometry> geoms = new ArrayList<>();
        for (Feature f : list) geoms.add(f.getGeometry());
        return UnaryUnionOp.union(geoms);
    }


    public AttributeType getReturnAttributeType(AttributeType inputType) {
        if (inputType == AttributeType.GEOMETRY) {
            return AttributeType.GEOMETRY;
        }
        else return null;
    }

}
