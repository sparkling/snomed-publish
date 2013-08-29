package com.ihtsdo.snomed.web.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ihtsdo.snomed.dto.RefsetDto;
import com.ihtsdo.snomed.exception.NonUniquePublicIdException;
import com.ihtsdo.snomed.exception.RefsetNotFoundException;
import com.ihtsdo.snomed.model.Refset;
import com.ihtsdo.snomed.service.RefsetService;
import com.ihtsdo.snomed.web.repository.RefsetRepository;

//http://www.petrikainulainen.net/programming/spring-framework/spring-data-jpa-tutorial-three-custom-queries-with-query-methods/
//@Transactional (value = "transactionManager", readOnly = true)
/**
 * @author Henrik Pettersen / Sparkling Ideas (henrik@sparklingideas.co.uk)
 */
@Service
//@Scope(proxyMode=ScopedProxyMode.TARGET_CLASS)
public class RepositoryRefsetService implements RefsetService {
    private static final Logger LOG = LoggerFactory.getLogger(RepositoryRefsetService.class);

    protected static final int NUMBER_OF_REFSETS_PER_PAGE = 5;

    @Inject
    RefsetRepository refsetRepository;
    
    @PostConstruct
    public void init(){}
    
    /* (non-Javadoc)
     * @see com.ihtsdo.snomed.service.RefsetService#findAll(int)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Refset> findAll(int pageIndex){
        LOG.debug("Retrieving all refsets");
//        System.out.println("\n\n\n\n\n\nFUCK!!!!!!!\n\n\n\n\n\n\n");
//        Pageable pageSpecification = new PageRequest(pageIndex, NUMBER_OF_REFSETS_PER_PAGE, sortByAscendingTitle());
//        Page requestedPage = refsetRepository.findAll(constructPageSpecification(pageIndex));
//        return requestedPage.getContent();
        return null;
    }
    
    /* (non-Javadoc)
     * @see com.ihtsdo.snomed.service.RefsetService#findAll()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Refset> findAll(){
        LOG.debug("Retrieving all refsets");
        return refsetRepository.findAll(sortByAscendingTitle());
    }    
    
    /* (non-Javadoc)
     * @see com.ihtsdo.snomed.service.RefsetService#findById(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public Refset findById(Long id) {
        LOG.debug("Finding refset by id: " + id);
        return refsetRepository.findOne(id);
    }
    
    /* (non-Javadoc)
     * @see com.ihtsdo.snomed.service.RefsetService#findByPublicId(java.lang.String)
     */
    @Override
    @Transactional
    public Refset findByPublicId(String publicId){
        LOG.debug("Getting refset with publicId=" + publicId);
        return refsetRepository.findByPublicId(publicId);
    }    
    
    /* (non-Javadoc)
     * @see com.ihtsdo.snomed.service.RefsetService#update(com.ihtsdo.snomed.web.dto.RefsetDto)
     */
    @Override
    @Transactional(rollbackFor = RefsetNotFoundException.class)
    public Refset update(RefsetDto updated) throws RefsetNotFoundException, NonUniquePublicIdException {
        LOG.debug("Updating refset with information: " + updated);
        Refset refset = refsetRepository.findOne(updated.getId());
        if (refset == null) {
            LOG.debug("No refset found with id: " + updated.getId());
            throw new RefsetNotFoundException();
        }
        refset.update(updated.getPublicId(), updated.getTitle(), updated.getDescription());
        try {
            return refsetRepository.save(refset);
        } catch (DataIntegrityViolationException e) {
            throw new NonUniquePublicIdException(e.getMessage(), e);
        }
    }    

    
    /* (non-Javadoc)
     * @see com.ihtsdo.snomed.service.RefsetService#create(com.ihtsdo.snomed.web.dto.RefsetDto)
     */
    @Override
    @Transactional
    public Refset create(RefsetDto created) throws NonUniquePublicIdException{
        LOG.debug("Creating new refset [{}]", created.toString());
        Refset refset = Refset.getBuilder(created.getPublicId(), created.getTitle(), created.getDescription()).build();
        try {
            return refsetRepository.save(refset);
        } catch (DataIntegrityViolationException e) {
            throw new NonUniquePublicIdException(e.getMessage(), e);
        }
    }
    
    /* (non-Javadoc)
     * @see com.ihtsdo.snomed.service.RefsetService#delete(java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = RefsetNotFoundException.class)
    public Refset delete(Long refsetId) throws RefsetNotFoundException {
        LOG.debug("Deleting refset with id: " + refsetId);
        Refset deleted = refsetRepository.findOne(refsetId);
        if (deleted == null) {
            LOG.debug("No refset found with id: " + refsetId);
            throw new RefsetNotFoundException();
        }
        refsetRepository.delete(deleted);
        return deleted;
    }  
    
    /* (non-Javadoc)
     * @see com.ihtsdo.snomed.service.RefsetService#delete(java.lang.String)
     */
    @Override
    @Transactional(rollbackFor = RefsetNotFoundException.class)
    public Refset delete(String publicId) throws RefsetNotFoundException {
        LOG.debug("Deleting refset with public id: " + publicId);
        Refset deleted = refsetRepository.findByPublicId(publicId);
        if (deleted == null) {
            LOG.debug("No refset found with id: " + publicId);
            throw new RefsetNotFoundException();
        }
        refsetRepository.delete(deleted);
        return deleted;
    }     

    private Sort sortByAscendingTitle() {
        return new Sort(Sort.Direction.ASC, "title");
    }
    
//    private Pageable constructPageSpecification(int pageIndex) {
//        Pageable pageSpecification = new PageRequest(pageIndex, NUMBER_OF_REFSETS_PER_PAGE, sortByAscendingTitle());
//        return pageSpecification;
//    }    

    /*
     * @Query("SELECT p FROM Person p WHERE LOWER(p.lastName) = LOWER(:lastName)")
    public List<Person> find(@Param("lastName") String lastName);
     */
    
}