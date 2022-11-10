package com.atomscat.bootstrap.modules.weixincp.repository;

import com.atomscat.bootstrap.modules.weixincp.entity.TreeRoot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author th158
 * CrudRepository: : 它提供标准的创建，读取，更新和删除。它包含诸如 findOne()，findAll( )，save()，delete()，等。
 * PagingAndSortingRepository : 它扩展了 CrudRepository 并添加了findAll方法。它使我们能够以分页方式排序和检索数据。
 * JpaRepository : 这是一个 JPA特定存储库，它在 Spring Data Jpa 中定义。它扩展了存储库CrudRepository和PagingAndSortingRepository。它添加了特定于JPA的方法，例如 flush()，以在持久性上下文上触发刷新。
 */
@Repository
public interface TreeRootRepository extends JpaRepository<TreeRoot, Long> {
}
