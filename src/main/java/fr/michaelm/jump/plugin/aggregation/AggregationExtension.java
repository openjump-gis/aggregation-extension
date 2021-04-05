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

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

// 1.0.0 (2021-04-05) refactoring for java8 / OpenJUMP2 / jts1.18
// 0.2.10 (2017-01-03) fix small bug in dialog initialization
// 0.2.9 (2016-12-01) fix option new_layer which did not work
// 0.2.8 (2013-11-30) add italian translation
// 0.2.7 (2013-03-20)
public class AggregationExtension extends Extension {

    public String getName() {
        return "Aggregation PlugIn (Micha\u00EBl Michaud)";
    }

    public String getVersion() {
        return "1.0.0 (2021-04-05)";
    }

    public void configure(PlugInContext context) {
        new AggregatePlugIn().initialize(context);
    }

}