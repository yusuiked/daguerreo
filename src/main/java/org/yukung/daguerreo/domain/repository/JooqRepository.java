/*
 * Copyright 2016 Yusuke Ikeda
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yukung.daguerreo.domain.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.io.Serializable;
import java.util.List;

/**
 * jOOQ specific {@link org.springframework.data.repository.Repository} interface.
 *
 * @author yukung
 */
@NoRepositoryBean
public interface JooqRepository<E, ID extends Serializable> extends PagingAndSortingRepository<E, ID> {

    /**
     * {@inheritDoc}
     */
    @Override
    List<E> findAll();

    /**
     * {@inheritDoc}
     */
    @Override
    List<E> findAll(Iterable<ID> ids);

    /**
     * {@inheritDoc}
     */
    @Override
    List<E> findAll(Sort sort);

    /**
     * {@inheritDoc}
     */
    @Override
    E findOne(ID id);

    /**
     * {@inheritDoc}
     */
    @Override
    boolean exists(ID id);

    /**
     * {@inheritDoc}
     */
    @Override
    long count();

    /**
     * {@inheritDoc}
     */
    @Override
    <S extends E> S save(S entity);

    /**
     * {@inheritDoc}
     */
    @Override
    <S extends E> Iterable<S> save(Iterable<S> entities);

    /**
     * {@inheritDoc}
     */
    @Override
    void delete(ID id);

    /**
     * {@inheritDoc}
     */
    @Override
    void delete(E entity);

    /**
     * {@inheritDoc}
     */
    @Override
    void delete(Iterable<? extends E> entities);

    /**
     * Deletes the given entities in a batch which means it will create a single {@link org.jooq.DeleteQuery}.
     *
     * @param entities targets of deletion.
     */
    void deleteInBatch(Iterable<E> entities);
}
