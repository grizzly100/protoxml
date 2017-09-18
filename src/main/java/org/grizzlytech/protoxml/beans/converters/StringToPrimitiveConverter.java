package org.grizzlytech.protoxml.beans.converters;


import org.grizzlytech.protoxml.beans.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See "JAXB Mapping of XML Schema Built-in Data Types"
 */
public class StringToPrimitiveConverter implements Converter {

    private static final Logger LOG = LoggerFactory.getLogger(StringToPrimitiveConverter.class);

    public Object convert(Object value, Class toClass) {
        Object result = null;
        String fromClassName = value.getClass().getCanonicalName();
        String input = (String) value;

        switch (toClass.getCanonicalName()) {
            // xs:int (and xs:unsignedShort)
            case "int":
                result = Integer.parseInt(input);
                break;

            // xs:long (and xs:unsignedInt)
            case "long":
                result = Long.parseLong(input);
                break;

            // xs:short (xs:unsignedByte)
            case "short":
                result = Short.parseShort(input);
                break;

            // xs:double
            case "double":
                result = Double.parseDouble(input);
                break;

            // xs:float
            case "float":
                result = Float.parseFloat(input);
                break;

            // xs:byte
            case "byte":
                result = Byte.parseByte(input);
                break;

            // xs:boolean
            case "boolean":
                result = StringToBooleanConverter.parseBoolean(input);
                break;

            default:
                LOG.error("Could not convert {} of class {} into {}", value, fromClassName,
                        toClass.getCanonicalName());
        }

        return result;
    }


    @Override
    public Class getFromClass() {
        return Object.class;
    }

    @Override
    public Class getToClass() {
        return Object.class;
    }

    public String key() {
        return "StringToPrimitive";
    }

}
