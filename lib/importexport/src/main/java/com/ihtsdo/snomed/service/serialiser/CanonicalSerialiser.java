package com.ihtsdo.snomed.service.serialiser;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ihtsdo.snomed.model.Concept;
import com.ihtsdo.snomed.model.Description;
import com.ihtsdo.snomed.model.OntologyVersion;
import com.ihtsdo.snomed.model.Statement;

public class CanonicalSerialiser extends BaseSnomedSerialiser{
    private static final Logger LOG = LoggerFactory.getLogger( BaseSnomedSerialiser.class );
    
    CanonicalSerialiser(Writer writer) throws IOException{
        super(writer);
    }
    
    @Override
    public SnomedSerialiser footer(){
        return this;
    }    
    
    @Override
    public SnomedSerialiser header() throws IOException{
        writer.write("CONCEPTID1" + DELIMITER + "RELATIONSHIPTYPE" + DELIMITER +
                "CONCEPTID2" + DELIMITER + "RELATIONSHIPGROUP\r\n");
        return this;
    }
    
    @Override
    public void write (OntologyVersion o, Collection<Statement> statements) throws IOException{
        Iterator<Statement> rIt = statements.iterator();
        int counter = 1;
        while (rIt.hasNext()){
            write(rIt.next());
            counter++;
        }
        LOG.info("Wrote " + counter + " lines");
    }

    @Override
    public void write(Statement r) throws IOException{
        writer.write(Long.toString(r.getSubject().getSerialisedId())
                + DELIMITER + Long.toString(r.getPredicate().getSerialisedId())
                + DELIMITER + Long.toString(r.getObject().getSerialisedId())
                + DELIMITER + Integer.toString(r.getGroupId()) + "\r\n");
    }


	@Override
	public void write(Concept c) throws IOException, ParseException {
		throw new UnsupportedOperationException("Write for concept not implemented for canonical serialiser");
		
	}

	@Override
	public void write(Description d) throws IOException, ParseException {
		throw new UnsupportedOperationException("Write for description not implemented for canonical serialiser");
	}    
    
    
    
//    protected void printRelationship(Writer w, long serialisedId, long subjectId, long preicateId, long objectId) throws IOException{
//        w.write(Long.toString(r.getSubject().getSerialisedId())
//                + DELIMITER + Long.toString(r.getPredicate().getSerialisedId())
//                + DELIMITER + Long.toString(r.getObject().getSerialisedId())
//                + DELIMITER + Integer.toString(r.getGroupId()));
//}   
//  public void writeFast(Writer w, final int ontologyId, EntityManager em) throws IOException{
//  printHeading(w);
//
//  HibernateEntityManager hem = em.unwrap(HibernateEntityManager.class);
//  Session session = ((Session) hem.getDelegate()).getSessionFactory().openSession();
//  session.doWork(new Work() {
//      public void execute(Connection connection) throws SQLException {
//          
//          java.sql.Statement statement = connection.createStatement();
//          ResultSet rs = statement.executeQuery(
//             "select" +
//                  " s.subject_id, s.predicate_id, s.object_id" +
//              " from" +
//                  " statement s" +
//              " left outer join" +
//                  " concept c1" +
//                      " on s.subject_id=c1.id" +
//              " left outer join" +
//                  " concept c2" +
//                      " on s.predicate_id=c2.id" +
//              " left outer join" +
//                  " concept c3" +
//                      " on s.object_id=c3.id" +
//              " where" +
//                  " s.ontologyVersion_id=2" +
//              " order by" +
//                  " s.id");
//          while(rs.next()){
//              long subjectId = rs.getLong(1);   
//          }
//          PreparedStatement statement = connection.prepareStatement(
//                  "INSERT INTO ONTOLOGY (NAME) values (?)", java.sql.Statement.RETURN_GENERATED_KEYS);
//          statement.setString(1, name);
//          int affectedRows = statement.executeUpdate();
//          if (affectedRows == 0) {
//              throw new SQLException("Creating ontology failed, no rows affected.");
//          }
//          ResultSet generatedKeys = statement.getGeneratedKeys();
//          if (generatedKeys.next()) {
//              ontology.setId(generatedKeys.getLong(1));
//          } else {
//              throw new SQLException("Creating ontology failed, no generated key obtained.");
//          }
//      }
//  });
//}    

}
