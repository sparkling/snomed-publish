package com.ihtsdo.snomed.service.serialiser;

import java.io.IOException;
import java.io.Writer;

import com.ihtsdo.snomed.service.InvalidInputException;

public class SnomedSerialiserFactory  {

    public static SnomedSerialiserFactory instance;
    
    public enum Form{
        CANONICAL, CHILD_PARENT, RDF_SCHEMA;
    }
    
    public static SnomedSerialiser getSerialiser(Form form, Writer writer) throws IOException{
        switch (form){
            case CANONICAL:
                return new CanonicalSerialiser(writer);
            case CHILD_PARENT:
                return new ChildParentSerialiser(writer);
            case RDF_SCHEMA:
                return new RdfSchemaSerialiser(writer);
            default:
                throw new InvalidInputException("BaseSnomedSerialiser " + form + " not found");
        }
    }
}