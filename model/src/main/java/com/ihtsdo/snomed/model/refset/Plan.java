package com.ihtsdo.snomed.model.refset;

import java.beans.Transient;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.ihtsdo.snomed.exception.ConceptsCacheNotBuiltException;
import com.ihtsdo.snomed.model.Concept;

@Entity
@org.hibernate.annotations.Table(appliesTo = "Plan",
        indexes={})
@Table(name = "Plan"//, 
//uniqueConstraints = @UniqueConstraint(columnNames = {"publicId"})
)
public class Plan {
    
    private static final Logger LOG = LoggerFactory.getLogger(Plan.class);

    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(
        joinColumns = @JoinColumn(name="plan_id"),
        inverseJoinColumns = @JoinColumn(name="concept_id"),
        uniqueConstraints=@UniqueConstraint(columnNames={"plan_id", "concept_id"}))
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Set<Concept> concepts;
    
    @OneToOne(targetEntity=BaseRule.class, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private Rule terminal;
    
    @NotNull
    private Date creationTime;
    
    @NotNull
    private Date modificationTime;
    
    @Version
    private long version = 0;
     
    public void refreshConceptsCache(){
        if (getTerminal() != null){
            concepts = terminal.generateConcepts();
        }else{
            concepts = new HashSet<>();
        }
    } 
    
    @Transient
    public final Set<Concept> getConcepts() throws ConceptsCacheNotBuiltException{
        if (concepts == null){
            throw new ConceptsCacheNotBuiltException("Build the cache before calling getConcepts");
        }
        return concepts;
    }
    
    @Transient
    public final Set<Concept> refreshAndGetConcepts(){
        refreshConceptsCache();
        Set<Concept> concepts = null;
        try {
            concepts = getConcepts();
        }
        catch (ConceptsCacheNotBuiltException e) {
            //will never happen
        }
        
        return concepts;
    }    
    
    @Transient
    public List<Rule> getRules(){
        if (terminal != null){
            CollectRulesVisitor visitor = new CollectRulesVisitor();
            terminal.accept(visitor);
            LOG.debug("Found {} Rules", visitor.getRules().size());
            return visitor.getRules();
        }
        return new ArrayList<>();
    }
    
    private static class CollectRulesVisitor implements Visitor{
        private List<Rule> rules = new ArrayList<>();

        @Override
        public void visit(Rule rule) {
            rules.add(rule);
        }
        
        public List<Rule> getRules(){
            return rules;
        }
    }
    
    @Override
    public int hashCode(){
        return Objects.hashCode(
                getTerminal());
                
    }
    
    @Override
    public boolean equals(Object o){
        if (o instanceof Plan){
            Plan r = (Plan) o;
            if (Objects.equal(r.getTerminal(), this.getTerminal()) && 
                    Objects.equal(r.refreshAndGetConcepts(), this.refreshAndGetConcepts())){
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString(){        
        return Objects.toStringHelper(this)
                .add("id", getId())
                .add("terminal", getTerminal())
                .add("concepts", refreshAndGetConcepts().size())
                .toString();
    }    

    public Rule getTerminal() {
        return terminal;
    }

    public void setTerminal(Rule terminal) {
        this.terminal = terminal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }


    @PreUpdate
    public void preUpdate() {
        modificationTime = new Date(Calendar.getInstance().getTime().getTime());
    }
    
    @PrePersist
    public void prePersist() {
        Date now = new Date(Calendar.getInstance().getTime().getTime());
        creationTime = now;
        modificationTime = now;
    }

    public static Builder getBuilder(Rule terminal) {
        return new Builder(terminal);
    }
    
    public static class Builder {
        private Plan built;

        Builder(Rule terminal) {
            built = new Plan();
            built.terminal = terminal;
        }
        
        public Builder id(Long id){
            built.id = id;
            return this;
        }

        public Plan build() {
            return built;
        }
    }
    
}


