/**
 * 
 */
package org.drools.base.resolvers;

import org.drools.WorkingMemory;
import org.drools.spi.Tuple;

public class GlobalVariable
    implements
    ValueHandler {

    private static final long serialVersionUID = 320L;

    public final String             globalName;
    public final Class              clazz;
    
    
    private Object cachedValue = ValueHandler.EMPTY;

    public GlobalVariable(final String name, final Class clazz) {
        this.globalName = name;
        this.clazz = clazz;
    }

    public Object getValue(final Tuple tuple,
                           final WorkingMemory wm) {
        if ( this.cachedValue == ValueHandler.EMPTY ) {
            this.cachedValue = wm.getGlobal( this.globalName );
        }
        return this.cachedValue;
    }
    
    public Class getExtractToClass() {
        return this.clazz;
    }    
    
    public void reset() {
        this.cachedValue = ValueHandler.EMPTY;
    }    
    
    public String toString() {
        return "[GlobalVariable name=" + this.globalName + "]";
    }

    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + this.globalName.hashCode();
        return result;
    }

    public boolean equals(Object object) {
        if ( this == object ) {
            return true;
        }
        if ( object == null || getClass() != object.getClass() ) {
            return false;
        }
        
        final GlobalVariable other = (GlobalVariable) object;
        return this.globalName.equals( other.globalName );
    }

}