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

import org.locationtech.jts.geom.Geometry;

import java.util.*;


/**
 * A function object for {@link Geometry} functions (which return a Geometry).
 * Provides metadata about the function.
 *
 * @author Martin Davis
 * @version 1.0
 */
public abstract class GeometryPredicate
{
  static GeometryPredicate[] method = {
    new IntersectsPredicate(),
    new PlainIntersectsPredicate(),
    //new ContainsPredicate(),
    new CoveredByPredicate(),
    new CoversPredicate(),
    //new CrossesPredicate(),
    //new DisjointPredicate(),
    new EqualsPredicate(),
    //new OverlapsPredicate(),
    //new TouchesPredicate(),
    new WithinPredicate(),
    new WithinDistancePredicate(),
    //new SimilarPredicate(),
  };

  static List<String> getNames()
  {
    List<String> names = new ArrayList<>();
    for (GeometryPredicate predicate : method) {
      names.add(predicate.name);
    }
    return names;
  }

  static GeometryPredicate getPredicate(String name)
  {
    for (GeometryPredicate predicate : method) {
      if (predicate.name.equals(name))
        return predicate;
    }
    return null;
  }

  private final String name;
  private final int nArguments;
  private final int nParams;
  private String description;

  public GeometryPredicate(String name, int nParams)
  {
    this(name, 2, nParams, null);
  }

  public GeometryPredicate(String name)
  {
    this(name, 2, 0, null);
  }

  public GeometryPredicate(String name, int nArgs, int nParams)
  {
    this(name, nArgs, nParams, null);
  }

  public GeometryPredicate(String name, int nArgs, int nParams,
                          String description)
  {
    this.name = name;
    this.nArguments = nArgs;
    this.nParams = nParams;
    this.description = description;
  }
  public String getName() { return name; }
  public int getGeometryArgumentCount() { return nArguments; }
  public int getParameterCount() { return nParams; }

  public abstract boolean isTrue(Geometry geom0, Geometry geom1, double[] param);

  private static class IntersectsPredicate extends GeometryPredicate {
    public IntersectsPredicate() {  super(I18NPlug.getI18N("predicate.Intersects"));  }
    public boolean isTrue(Geometry geom0, Geometry geom1, double[] param) {
      return geom0.intersects(geom1);   }
  }
  private static class PlainIntersectsPredicate extends GeometryPredicate {
    public PlainIntersectsPredicate() {  super(I18NPlug.getI18N("predicate.PlainIntersects"));  }
    public boolean isTrue(Geometry geom0, Geometry geom1, double[] param) {
      return geom0.relate(geom1, "T********");   }
  }
  /*private static class ContainsPredicate extends GeometryPredicate {
    public ContainsPredicate() {  super(GenericNames.CONTAINS);  }
    public boolean isTrue(Geometry geom0, Geometry geom1, double[] param) {
      return geom0.contains(geom1);   }
  }*/
  private static class CoversPredicate extends GeometryPredicate {
    public CoversPredicate() {  super(I18NPlug.getI18N("predicate.Covers"));  }
    public boolean isTrue(Geometry geom0, Geometry geom1, double[] param) {
      return geom0.covers(geom1);   }
  }
  private static class CoveredByPredicate extends GeometryPredicate {
    public CoveredByPredicate() {  super(I18NPlug.getI18N("predicate.CoveredBy"));  }
    public boolean isTrue(Geometry geom0, Geometry geom1, double[] param) {
      return geom0.coveredBy(geom1);   }
  }
  /*private static class CrossesPredicate extends GeometryPredicate {
    public CrossesPredicate() {  super(GenericNames.CROSSES);  }
    public boolean isTrue(Geometry geom0, Geometry geom1, double[] param) {
      return geom0.crosses(geom1);   }
  }
  public static class DisjointPredicate extends GeometryPredicate {
    public DisjointPredicate() {  super(GenericNames.DISJOINT);  }
    public boolean isTrue(Geometry geom0, Geometry geom1, double[] param) {
      return geom0.disjoint(geom1);   }
  }*/
  private static class EqualsPredicate extends GeometryPredicate {
    public EqualsPredicate() {  super(I18NPlug.getI18N("predicate.Equals"));  }
    public boolean isTrue(Geometry geom0, Geometry geom1, double[] param) {
      return geom0.equals(geom1);   }
  }
  /*private static class OverlapsPredicate extends GeometryPredicate {
    public OverlapsPredicate() {  super(GenericNames.OVERLAPS);  }
    public boolean isTrue(Geometry geom0, Geometry geom1, double[] param) {
      return geom0.overlaps(geom1);   }
  }
  private static class TouchesPredicate extends GeometryPredicate {
    public TouchesPredicate() {  super(GenericNames.TOUCHES);  }
    public boolean isTrue(Geometry geom0, Geometry geom1, double[] param) {
      return geom0.touches(geom1);   }
  }
  */
  private static class WithinPredicate extends GeometryPredicate {
    public WithinPredicate() {  super(I18NPlug.getI18N("predicate.Within"));  }
    public boolean isTrue(Geometry geom0, Geometry geom1, double[] param) {
      return geom0.within(geom1);   }
  }
  public static class WithinDistancePredicate extends GeometryPredicate {
    public WithinDistancePredicate() {  super(I18NPlug.getI18N("predicate.WithinDistance"), 1);  }
    public boolean isTrue(Geometry geom0, Geometry geom1, double[] param) {
      return geom0.isWithinDistance(geom1, param[0]);   }
  }
  /*public static class SimilarPredicate extends GeometryPredicate {
      public SimilarPredicate() {  super(GenericNames.SIMILAR, 1);  }
      public boolean isTrue(Geometry geom0, Geometry geom1, double[] param) {
        return BufferGeometryMatcher.isMatch(geom0, geom1, param[0]);
      }
    }*/
}
