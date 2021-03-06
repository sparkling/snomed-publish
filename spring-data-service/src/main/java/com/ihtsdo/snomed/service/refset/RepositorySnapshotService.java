package com.ihtsdo.snomed.service.refset;

import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ihtsdo.snomed.dto.refset.PlanDto;
import com.ihtsdo.snomed.dto.refset.SnapshotDto;
import com.ihtsdo.snomed.exception.ConceptIdNotFoundException;
import com.ihtsdo.snomed.exception.NonUniquePublicIdException;
import com.ihtsdo.snomed.exception.RefsetConceptNotFoundException;
import com.ihtsdo.snomed.exception.RefsetNotFoundException;
import com.ihtsdo.snomed.exception.SnapshotNotFoundException;
import com.ihtsdo.snomed.exception.validation.ValidationException;
import com.ihtsdo.snomed.model.refset.Refset;
import com.ihtsdo.snomed.model.refset.Rule;
import com.ihtsdo.snomed.model.refset.Snapshot;
import com.ihtsdo.snomed.model.refset.Status;
import com.ihtsdo.snomed.repository.refset.RefsetRepository;
import com.ihtsdo.snomed.repository.refset.SnapshotRepository;
import com.ihtsdo.snomed.service.ConceptService;
import com.ihtsdo.snomed.service.refset.RefsetService.SortOrder;

//http://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-three-custom-queries-with-query-methods/

@Service
@Transactional(value = "transactionManager")
public class RepositorySnapshotService implements SnapshotService {
    private static final Logger LOG = LoggerFactory.getLogger(RepositorySnapshotService.class);

    protected static final int NUMBER_OF_SNAPSHOTS_PER_PAGE = 5;

    @Inject
    protected SnapshotRepository snapshotRepository;
    
    @Inject
    RefsetRepository refsetRepository;    
    
    @Inject
    protected PlanService planService;
    
    @Inject
    protected RefsetService refsetService;    
    
    @Inject
    protected ConceptService conceptService;   
    
    @PostConstruct
    public void init(){}
    
    @Override
    @Transactional(readOnly=true)
    public Snapshot findByPublicId(String refsetPublicId, String snapshotPublicId) throws SnapshotNotFoundException{
        LOG.debug("Getting snapshot with publicId={} for refset {}", snapshotPublicId, refsetPublicId);
        Snapshot snapshot = snapshotRepository.findOneBySnapshotPublicIdAndRefsetPublicIdAndStatus(
                refsetPublicId, snapshotPublicId, Status.ACTIVE);
        if (snapshot == null){
            throw new SnapshotNotFoundException(refsetPublicId, snapshotPublicId);
        }
        return snapshot;
    } 

// SNAPSHOT MEMBERS ARE IMMUTABLE
//    @Override
//    @Transactional(rollbackFor = {SnapshotNotFoundException.class,
//            ConceptIdNotFoundException.class, RefsetNotFoundException.class})
//    public Snapshot addMembers(Set<MemberDto> members, String snapshotPublicId, String refsetPublicId)
//            throws SnapshotNotFoundException, ConceptIdNotFoundException, RefsetNotFoundException {
//        LOG.debug("Adding {} new members to refset {}", members.size(), snapshotPublicId);
//        
//        Refset refset = refsetService.findByPublicId(refsetPublicId);
//        Snapshot snapshot = findByPublicId(refsetPublicId, snapshotPublicId);
//        snapshot.addMembers(RepositoryRefsetService.fillMembers(members, refset.getModuleConcept(), conceptService));
//        return snapshot;
//    }    

    @Override
    @Transactional(rollbackFor = {SnapshotNotFoundException.class, NonUniquePublicIdException.class})
    public Snapshot update(String refsetPublicId, String snapshotPublicId, SnapshotDto updated) 
            throws NonUniquePublicIdException, SnapshotNotFoundException{
        LOG.debug("Updating snapshot {} for refset {} with: {} ", snapshotPublicId, refsetPublicId, updated);
        
        Snapshot snapshot = findByPublicId(refsetPublicId, snapshotPublicId);
        
        snapshot.update(
                updated.getTitle(),
                updated.getDescription());

        try {
            return snapshotRepository.save(snapshot);
        } catch (DataIntegrityViolationException e) {
            throw new NonUniquePublicIdException("Public id [" + updated.getPublicId() + "] already exist for snapshot for refset [" + refsetPublicId + "]");            
        }
    }   
    
    @Override
    @Transactional(rollbackFor = {
            RefsetNotFoundException.class, 
            NonUniquePublicIdException.class})
    public Snapshot createFromRefsetMembers(String refsetPublicId, SnapshotDto snapshotDto) 
            throws RefsetNotFoundException, NonUniquePublicIdException {
        
        Refset refset = refsetService.findByPublicId(refsetPublicId);
        
        Snapshot snapshot = snapshotRepository.
                findOneBySnapshotPublicIdAndRefsetPublicIdAndAnyStatus(refsetPublicId, snapshotDto.getPublicId());

        if (snapshot != null){
            throw new NonUniquePublicIdException("Snapshot with public id {} allready exists");
        }
        
        snapshot = Snapshot.getBuilder(
                //snapshotDto.getPublicId(),
                UUID.randomUUID().toString(),
                snapshotDto.getTitle(), 
                snapshotDto.getDescription(), 
                refset,
                refset.getMembers(),
                refset.getPlan().getTerminal() != null ? refset.getPlan().getTerminal().clone() : null).build();
        
        //LOG.debug("Creating snapshot " + snapshot.toString());        
        
        refset.addSnapshot(snapshot);
        refset.setPendingChanges(false);
        try {
            snapshotRepository.save(snapshot);
            refsetRepository.save(refset);
            setSnapshotPublicIdAfterSave(refset, snapshot);
        } catch (DataIntegrityViolationException e) {
            throw new NonUniquePublicIdException(e.getMessage(), e);
        }
        return snapshot;
    }    

    @Override
    @Transactional(rollbackFor={RefsetConceptNotFoundException.class,
            NonUniquePublicIdException.class, ValidationException.class})
    public Snapshot createFromDeclaredMembers(String refsetPublicId, SnapshotDto created) throws NonUniquePublicIdException, ValidationException, ConceptIdNotFoundException, RefsetNotFoundException{
        LOG.debug("Creating new snapshot [{}] for refset {}", created.toString(), refsetPublicId);
        
        Refset refset = refsetService.findByPublicId(refsetPublicId);
        
        PlanDto planDto = new PlanDto();
        planDto.setRefsetRules(created.getRefsetRules());
        planDto.setTerminal(created.getTerminal());
        
        Rule rule = planService.createRules(planDto);
        
        Snapshot snapshot = Snapshot.getBuilder(
                //created.getPublicId(),
                UUID.randomUUID().toString(),
                created.getTitle(), 
                created.getDescription(),
                refset,
                RepositoryRefsetService.fillMembers(created.getMemberDtos(), refset.getModuleConcept(), conceptService),
                rule
                ).build();
        
        //LOG.debug("Creating snapshot " + snapshot.toString());
        
        try {
            snapshotRepository.save(snapshot);
            refsetRepository.save(refset);
            setSnapshotPublicIdAfterSave(refset, snapshot);
        } catch (DataIntegrityViolationException e) {
            throw new NonUniquePublicIdException(e.getMessage(), e);
        }
        return snapshot;
    }
    
    @Transactional
    private void setSnapshotPublicIdAfterSave(Refset refset, Snapshot snapshot){
        //LOG.debug("Generating Snapshot ID from refset {} and Snapshot {}", refset, snapshot);
        snapshot.setPublicId(refset.getId() + "-" + snapshot.getId().toString());
    }
    
    @Override
    @Transactional
    public Snapshot delete(String refsetPublicId, String snapshotPublicId) throws SnapshotNotFoundException {
        LOG.debug("Inactivating (deleting) snapshot with public id {} for refset {} " + snapshotPublicId, refsetPublicId);
        
        Snapshot inactivated = snapshotRepository.findOneBySnapshotPublicIdAndRefsetPublicIdAndStatus(
                refsetPublicId, snapshotPublicId, Status.ACTIVE);
        if (inactivated == null) {
            throw new SnapshotNotFoundException(refsetPublicId, snapshotPublicId);
        }
        inactivated.setStatus(Status.INACTIVE);
        return inactivated;
    }    

    @Override
    @Transactional
    public Snapshot resurect(String refsetPublicId, String snapshotPublicId) throws SnapshotNotFoundException {
        LOG.debug("Resurecting snapshot with public id {} for refset {}", snapshotPublicId, refsetPublicId);
        Snapshot resurected = snapshotRepository.findOneBySnapshotPublicIdAndRefsetPublicIdAndStatus(
                refsetPublicId, snapshotPublicId, Status.INACTIVE);
        if (resurected == null) {
            throw new SnapshotNotFoundException(refsetPublicId, snapshotPublicId);
        }
        resurected.setStatus(Status.ACTIVE);
        return resurected;
    }

    @Override
    @Transactional(readOnly=true)
    public com.ihtsdo.snomed.service.Page<Snapshot> findAllSnapshots(String refsetPublicId, String sortBy, SortOrder sortOrder, String searchTerm, int page, int pageSize) {
        LOG.debug("Getting all snapshots for refset with publicId={} sorted by {} {}, page {} of {}, with search term '{}'", refsetPublicId, sortBy, sortOrder, page, pageSize, searchTerm);
        Page<Snapshot> pageResult =  snapshotRepository.findAllByRefsetPublicIdAndStatus(refsetPublicId, Status.ACTIVE, searchTerm, 
                new PageRequest(page, pageSize, new Sort(RepositoryRefsetService.sortDirection(sortOrder), sortBy)));
        return new com.ihtsdo.snomed.service.Page<Snapshot>(pageResult.getContent(), pageResult.getTotalElements());
    }
}
