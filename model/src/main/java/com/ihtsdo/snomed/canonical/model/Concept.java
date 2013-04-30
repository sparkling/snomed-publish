
package com.ihtsdo.snomed.canonical.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.primitives.Longs;

@XmlRootElement
@Entity
public class Concept {
    private static final String ATTRIBUTE = "attribute";
    private static final Logger LOG = LoggerFactory.getLogger( Concept.class );
    public static final long IS_KIND_OF_RELATIONSHIP_TYPE_ID = 116680003;
    
    @XmlTransient @Transient private Set<Concept> cache;
    @XmlTransient @Transient private Map<Integer, Group> subjectOfStatementGrouping = new HashMap<Integer, Group>();

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private long id;
    @XmlTransient private static Concept kindOfPredicate;    

    private long serialisedId;
    private int status;
    private String fullySpecifiedName;
    private String ctv3id;
    private String snomedId;
    private String type;
    
    @Column(columnDefinition = "BIT", length = 1) private boolean primitive;
    @XmlTransient @OneToOne private Ontology ontology;

    @XmlTransient @OneToMany(mappedBy="subject") 
    private Set<Statement> subjectOfStatements = new HashSet<Statement>();

    @XmlTransient @OneToMany(mappedBy="object")
    private Set<Statement> objectOStatements = new HashSet<Statement>();    
    
    @XmlTransient @OneToMany(mappedBy="predicate")
    private Set<Statement> predicateOfStatements = new HashSet<Statement>();

    @XmlTransient
    @ManyToMany
    @JoinTable(name = "KIND_OF", 
        joinColumns = @JoinColumn(name="child_id"),
        inverseJoinColumns = @JoinColumn(name="parent_id"),
        uniqueConstraints=@UniqueConstraint(columnNames={"parent_id", "child_id"}))
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Set<Concept> kindOfs = new HashSet<Concept>();

    @XmlTransient @ManyToMany(mappedBy="kindOfs")
    private Set<Concept> parentOf = new HashSet<Concept>();
    
    public Concept(){}
    
    public Concept(long serialisedId){
        this.serialisedId = serialisedId;
        if (serialisedId == IS_KIND_OF_RELATIONSHIP_TYPE_ID){
            kindOfPredicate = this;
        }
    }
    
    public Set<Concept> getAllKindOfPrimitiveConcepts(boolean useCache){
        if (useCache && (cache != null)){
            if (LOG.isDebugEnabled()){
                StringBuffer debugStringBuffer = new StringBuffer("Cache hit for concept " + this.getSerialisedId() + " with values {");
                for (Concept c : cache){
                    debugStringBuffer.append(c.getSerialisedId()+ ", ");
                }
                if (!cache.isEmpty()){
                    debugStringBuffer.delete(debugStringBuffer.length() - 2, debugStringBuffer.length());
                }
                debugStringBuffer.append("}");
                LOG.debug(debugStringBuffer.toString());
            }
            return cache;
        }
        LOG.debug("Populating cache for concept " + getSerialisedId());
        cache = new HashSet<Concept>();
        
        for (Concept kindOf : kindOfs){
            if (kindOf.isPrimitive()){
                cache.add(kindOf);
            }
            cache.addAll(kindOf.getAllKindOfPrimitiveConcepts(useCache));
        }   
        return cache;
    }

    public boolean isKindOfPredicate(){
        return serialisedId == IS_KIND_OF_RELATIONSHIP_TYPE_ID;
    }
    
    public static void setKindOfPredicate(Concept kindOfPredicateToSet){
        kindOfPredicate = kindOfPredicateToSet;
    }
    
    public static Concept getKindOfPredicate() throws IllegalStateException{
        if (kindOfPredicate == null){
            throw new IllegalStateException("The isA predicate does not exist in this ontology at this point");
        }
        return kindOfPredicate;
    }
    
    public boolean isPredicate(){
        return ((type != null) && (!type.isEmpty()) && type.equals(ATTRIBUTE));
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", getId())
                .add("internalId", getSerialisedId())
                .add("ontology", getOntology() == null ? null : getOntology().getId())
                .add("status", getStatus())
                .add("fullySpecifiedName", getFullySpecifiedName())
                .add("ctv3id", getCtv3id())
                .add("snomedId", getSnomedId())
                .add("primitive", isPrimitive())
                .toString();
    }

    @Override
    public int hashCode(){
        return Longs.hashCode(getSerialisedId());
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof Concept){
            Concept c = (Concept) o;
            if (c.getSerialisedId() == this.getSerialisedId()){
                return true;
            }
        }
        return false;
    }
    
    public void addSubjectOfStatement(Statement statement){
        subjectOfStatements.add(statement);
        populateGroup(statement);
    }
    
    public Group getGroup(Statement statement){
        Group group = subjectOfStatementGrouping.get(statement.getGroup());
        if (group == null){
            group = populateGroup(statement);    
        }
        return group;
    }

    private Group populateGroup(Statement statement) {
        Group group = subjectOfStatementGrouping.get(statement.getGroup());
        if (group == null) {
            group = new Group(statement); 
            subjectOfStatementGrouping.put(statement.getGroup(), group);
        }else{
            group.addStatement(statement);
        }
        return group;
    }
    
    public void setSerialisedId(long serialisedId) {
        this.serialisedId = serialisedId;
        if (serialisedId == IS_KIND_OF_RELATIONSHIP_TYPE_ID){
            kindOfPredicate = this;
        }
    }    

    /*
     * Generated Getters and Setters
     */

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getFullySpecifiedName() {
        return fullySpecifiedName;
    }
    public void setFullySpecifiedName(String fullySpecifiedName) {
        this.fullySpecifiedName = fullySpecifiedName;
    }
    public String getCtv3id() {
        return ctv3id;
    }
    public void setCtv3id(String ctv3id) {
        this.ctv3id = ctv3id;
    }
    public String getSnomedId() {
        return snomedId;
    }
    public void setSnomedId(String snomedId) {
        this.snomedId = snomedId;
    }
    public boolean isPrimitive() {
        return primitive;
    }
    public void setPrimitive(boolean primitive) {
        this.primitive = primitive;
    }
    public Set<Concept> getKindOfs() {
        return kindOfs;
    }
    public void setKindOfs(Set<Concept> kindOfs) {
        this.kindOfs = kindOfs;
    }
    public Set<Concept> getParentOf() {
        return parentOf;
    }
    public void setParentOf(Set<Concept> parentOf) {
        this.parentOf = parentOf;
    }
    public void addKindOf(Concept concept){
        this.kindOfs.add(concept);
    }
    public void addParentOf(Concept concept){
        this.parentOf.add(concept);
    }
    public Set<Statement> getSubjectOfRelationshipStatements(){
        return subjectOfStatements;
    }
    public void addPredicateOfStatement(Statement statement){
        predicateOfStatements.add(statement);
    }
    public Set<Statement> getPredicateOfRelationshipStatements(){
        return predicateOfStatements;
    }
    public void addObjectOfStatement(Statement statement){
        objectOStatements.add(statement);
    }
    public Set<Statement> getObjectOfRelationshipStatements(){
        return objectOStatements;
    }
    public Ontology getOntology() {
        return ontology;
    }
    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public long getSerialisedId() {
        return serialisedId;
    }
}
