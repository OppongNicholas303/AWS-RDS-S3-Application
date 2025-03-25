package com.example.All.in.one.repository;

import com.example.All.in.one.model.ImageItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ImageRepository extends JpaRepository<ImageItem, Long> {
    // This adds transaction support and a proper JPQL query
    @Transactional
    @Modifying
    @Query("DELETE FROM ImageItem i WHERE i.key = ?1")
    void deleteByKey(String key);

    // Alternatively, you could add a find method to get the entity first
    ImageItem findByKey(String key);
}
