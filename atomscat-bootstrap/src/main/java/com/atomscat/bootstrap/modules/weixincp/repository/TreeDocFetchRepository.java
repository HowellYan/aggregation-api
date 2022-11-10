package com.atomscat.bootstrap.modules.weixincp.repository;

import com.atomscat.bootstrap.modules.weixincp.entity.TreeDocFetch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author th158
 */
@Repository
public interface TreeDocFetchRepository extends JpaRepository<TreeDocFetch, Long> {
}
