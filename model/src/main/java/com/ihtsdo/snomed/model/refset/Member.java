package com.ihtsdo.snomed.model.refset;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import com.google.common.base.Objects;
import com.ihtsdo.snomed.model.Concept;

@Entity
public class Member {
	
    @Id 
    @GeneratedValue(strategy=GenerationType.IDENTITY) 
    private Long id;
    
    private String serialisedId;
	
	@Temporal(TemporalType.TIMESTAMP) 
	Date effective;
	
    @Column(columnDefinition = "BIT", length = 1) 
    private boolean active; 
    
    @OneToOne 
	private Concept module;
	
    @OneToOne 
	private Concept component;
	
    @NotNull
    private Date creationTime;
    
    @NotNull
    private Date modificationTime;
    
	public Member() {
		effective = new Date();
		active = true;
	}
	
	public static Set<Member> createFromConcepts(Collection<Concept> concepts){
		Set<Member> created = new HashSet<>();
		for (Concept c : concepts){
			created.add(Member.getBuilder(null, c).build());
		}
		return created;
	}
	
   @Override
    public boolean equals(Object o){
        if (o instanceof Member){
        	Member r = (Member) o;
            if (r.getId() == getId()){
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString(){
        return Objects.toStringHelper(this)
                .add("id", getId())
                .add("effective", getEffective())
                .add("isActive", isActive())
                .add("component", getComponent())
                .add("module", getModule())
                .toString();
    }
    
    @Override
    public int hashCode(){
   	 return java.util.Objects.hash(
   			 getId(),
   			 getSerialisedId(),
   			 getEffective(),
   			 isActive(),
   			 getComponent(),
   			 getModule());
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
	
	public String getSerialisedId() {
		return serialisedId;
	}

	public void setSerialisedId(String serialisedId) {
		this.serialisedId = serialisedId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getEffective() {
		return effective;
	}

	public void setEffective(Date effective) {
		this.effective = effective;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Concept getModule() {
		return module;
	}

	public void setModule(Concept module) {
		this.module = module;
	}

	public Concept getComponent() {
		return component;
	}

	public void setComponent(Concept component) {
		this.component = component;
	}
	
    public static Builder getBuilder(Concept module, Concept component) {
        return new Builder(module, component);
    }
    
    public static class Builder {
        private Member built;

        Builder(Concept module, Concept component) {
            built = new Member();
            built.setComponent(component);
            built.setModule(module);
        }
        
        public Builder id(Long id){
            built.setId(id);
            return this;
        }
        
        public Builder component(Concept component){
            built.setComponent(component);
            return this;
        }
        
        public Builder module(Concept module){
            built.setModule(module);
            return this;
        }
        public Builder serialisedId(String serialisedId){
            built.setSerialisedId(serialisedId);
            return this;
        }        
        public Member build() {
            return built;
        }
    }
}