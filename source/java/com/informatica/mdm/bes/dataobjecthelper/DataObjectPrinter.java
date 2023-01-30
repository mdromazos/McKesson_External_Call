package com.informatica.mdm.bes.dataobjecthelper;

import java.util.List;

import org.apache.log4j.Logger;

import com.informatica.mdm.bes.helper.StringHelper;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;

public class DataObjectPrinter {
	protected static Logger logger = Logger.getLogger(StringHelper.class.getClass());

	
    public void printDataObject(DataObject dataObject, int indent) {

        logger.debug(">>> Entering printDataObject");
        Type objType = dataObject.getType();
        List propList = objType.getProperties();
        for (int p = 0, size = propList.size(); p < size; p++) {
            if (dataObject.isSet(p)) {
                Property property = (Property) propList.get(p);
                // For many-valued properties, process a list of values
                if (property.isMany()) {
                    // For many-valued properties, process a list of values
                    List values = dataObject.getList(p);
                    for (int v = 0, count = values.size(); v < count; v++) {
                        printValue(values.get(v), property, indent);
                    }
                } else {
                    // For single-valued properties, print out the value
                    printValue(dataObject.get(p), property, indent);
                }

            }
        }
    }

    public void printValue(Object value, Property property, int indent) {

        // Get the name of the property
        String propertyName = property.getName();

        // Construct a string for the proper indentation
        String margin = "";
        for (int i = 0; i < indent; i++)
            margin += "\t";
        if (value != null && property.isContainment()) {
            // For containment properties, display the value
            // with printDataObject
            Type type = property.getType();
            String typeName = type.getName();
            logger.debug(margin + propertyName + " (" + typeName + "):");
            System.out.println(margin + propertyName + " (" + typeName + "):");
            printDataObject((DataObject) value, indent + 1);
        } else {
            // For non-containment properties, just print the value
        	System.out.println(margin + propertyName + ": " + value);
            logger.debug(margin + propertyName + ": " + value);
        }

    }
}
