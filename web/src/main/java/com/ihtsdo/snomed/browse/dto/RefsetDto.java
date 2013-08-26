package com.ihtsdo.snomed.browse.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.google.common.base.Objects;

public class RefsetDto {
    
    private Long id;

    @NotNull
    @Size(min=2, max=20, message="validation.refset.publicid.size")
    @Pattern(regexp="[a-zA-Z0-9_]+", message="validation.refset.publicid.charactermix")
    private String publicId;
    
    @NotNull
    @Size(min=4, max=50, message="validation.refset.title.size")
    private String title;
    
    @NotNull
    @Size(min=4, message="validation.refset.description.size")
    private String description;
    
    public RefsetDto(){}
    
    public RefsetDto(Long id, String publicId, String title, String description){
        this.id = id;
        this.publicId = publicId;
        this.title = title;
        this.description = description;
    }
    
    @Override
    public String toString(){
        return Objects.toStringHelper(this)
                .add("id", getId())
                .add("title", getTitle())
                .add("description", getDescription())
                .add("publicId", getPublicId())
                .toString();
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    

}
