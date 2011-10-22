package org.apache.archiva.jarinfo.model.io;

import java.util.Calendar;

import org.apache.archiva.jarinfo.utils.Timestamp;
import org.apache.commons.beanutils.Converter;

/**
 * TimestampConverter
 *
 * @version $Id$
 */
public class TimestampConverter implements Converter {

    @SuppressWarnings("unchecked")
    public Object convert(Class type, Object value) {
        if( value == null ) {
            return null;
        }
        
        if( value instanceof Calendar) {
            return Timestamp.convert((Calendar)value);
        }
        
        return Timestamp.convert(String.valueOf(value));
    }

}
