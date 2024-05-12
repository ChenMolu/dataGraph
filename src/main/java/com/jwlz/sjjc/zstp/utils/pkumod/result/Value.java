package com.jwlz.sjjc.zstp.utils.pkumod.result;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**
 * @author wj
 */
public class Value implements Serializable {

    private String type;

    private String datatype;

    private String value;

    public Value() {
    }

    public Value(String type, String datatype, String value) {
        this.type = type;
        this.datatype = datatype;
        this.value = value;
    }


    public String asString() {
        return value;
    }

    public long asLong() {
        if ("http://www.w3.org/2001/XMLSchema#long".equalsIgnoreCase(datatype)) {
            try {
                if (value != null && "".equals(value)) {
                    return Long.parseLong(value);
                } else {
                    throw new NumberFormatException("value is blank");
                }
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Cannot coerce String to long without losing precision");
            }
        } else {
            throw new RuntimeException("Defined data type is " + datatype);
        }
    }

    public int asInt() {
        if ("http://www.w3.org/2001/XMLSchema#int".equalsIgnoreCase(datatype) ||
                "http://www.w3.org/2001/XMLSchema#integer".equalsIgnoreCase(datatype)) {
            try {
                if (value != null && "".equals(value)) {
                    return Integer.parseInt(value);
                } else {
                    throw new NumberFormatException("value is blank");
                }
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Cannot coerce String to int without losing precision");
            }
        } else {
            throw new RuntimeException("Defined data type is " + datatype);
        }
    }

    public long asDateTimeLong() {
        if ("http://www.w3.org/2001/XMLSchema#dateTime".equalsIgnoreCase(datatype)) {
            try {
                Instant instant = Instant.parse(this.value);
                return instant.toEpochMilli();
            } catch (DateTimeParseException e) {
                throw new RuntimeException("Cannot coerce String to timestamp long without losing precision");
            }
        } else {
            throw new RuntimeException("Defined data type is " + datatype);
        }
    }

    public long asDateLong() {
        if ("http://www.w3.org/2001/XMLSchema#date".equalsIgnoreCase(datatype)) {
            try {
                return LocalDate.parse(this.value).atStartOfDay(ZoneId.of("GMT")).toInstant().toEpochMilli();
            } catch (DateTimeParseException e) {
                throw new RuntimeException("Cannot coerce String to date long without losing precision");
            }
        } else {
            throw new RuntimeException("Defined data type is " + datatype);
        }
    }

    public boolean asBoolean() {
        if ("http://www.w3.org/2001/XMLSchema#boolean".equalsIgnoreCase(datatype)) {
            return Boolean.parseBoolean(this.value);
        } else {
            throw new RuntimeException("Defined data type is " + datatype);
        }
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
