package com.pawhub.module.auth.repository;

import com.pawhub.module.auth.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {}
