package com.patentlens.repository;

import com.patentlens.entity.BlockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<BlockEntity, Long> {

    List<BlockEntity> findByOrderByBlockIndexAsc();

    Optional<BlockEntity> findTopByOrderByBlockIndexDesc();

    Optional<BlockEntity> findByDocumentHash(String documentHash);
}
