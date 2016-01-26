/**
 * Copyright (C) 2013, 2014 Johannes Taelman
 *
 * This file is part of Axoloti.
 *
 * Axoloti is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Axoloti is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Axoloti. If not, see <http://www.gnu.org/licenses/>.
 */
package axoloti.outlets;

import axoloti.atom.AtomDefinition;
import axoloti.atom.AtomInstance;
import axoloti.datatypes.DataType;
import axoloti.datatypes.SignalMetaData;
import axoloti.object.AxoObjectInstance;
import axoloti.utils.CharEscape;
import java.security.MessageDigest;
import org.simpleframework.xml.Attribute;

/**
 *
 * @author Johannes Taelman
 */
public class Outlet implements AtomDefinition {

    @Attribute
    String name;
    @Attribute(required = false)
    public String description;
    @Deprecated
    @Attribute(required = false)
    Boolean SumBuffer;

    public DataType getDatatype() {
        return null;
    }

    public Outlet() {
    }

    public Outlet(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String GetCName() {
        return "outlet_" + CharEscape.CharEscape(name);
    }

    SignalMetaData GetSignalMetaData() {
        return SignalMetaData.none;
    }

    public void updateSHA(MessageDigest md) {
        md.update(name.getBytes());
        md.update((byte) getDatatype().hashCode());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AtomInstance CreateInstance(AxoObjectInstance o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
