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

import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.jooq.Table;
import org.jooq.UpdatableRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.yukung.daguerreo.domain.entity.Identifiable;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Repository base implementation for jOOQ.
 * <p>
 * All of the {@link JooqRepository} will be implemented by inheriting this class.
 * </p>
 *
 * @author yukung
 */
public abstract class BasicJooqRepository<R extends UpdatableRecord<R>, T extends Table<R>, E extends Identifiable<ID>,
    ID extends Serializable> implements JooqRepository<E, ID> {

    private T table;
    private Class<E> entityClass;
    private RecordMapper<R, E> mapper;

    @SuppressWarnings("unchecked")
    @PostConstruct
    private void init() {
        Type type = this.getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) type;
        Class<?> tableClass = (Class<?>) pt.getActualTypeArguments()[1];
        try {
            table = (T) tableClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();    // TODO later
        }
        entityClass = (Class<E>) pt.getActualTypeArguments()[2];
        mapper = dsl.configuration().recordMapperProvider().provide(table.recordType(), entityClass);
    }

    @Autowired
    protected DSLContext dsl;

    /**
     * Returns the target {@link Table}.
     *
     * @return the table
     */
    protected Table<R> table() {
        return table;
    }

    /**
     * Returns the class object of target entity class.
     *
     * @return the class object of entity class
     */
    protected Class<E> entityClass() {
        return entityClass;
    }

    /**
     * Returns the {@link RecordMapper}.
     * <p>
     * Subclasses may override this method to provide custom implementations.
     * </p>
     *
     * @return the record mapper
     */
    protected RecordMapper<R, E> mapper() {
        return mapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> findAll() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> findAll(Iterable<ID> ids) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> findAll(Sort sort) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<E> findAll(Pageable pageable) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E findOne(ID id) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(ID id) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends E> S save(S entity) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends E> Iterable<S> save(Iterable<S> entities) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(ID id) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(E entity) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Iterable<? extends E> entities) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteInBatch(Iterable<E> entities) {

    }

    /**
     * Deletion of all the records is not supported for ensure safety.
     */
    @Override
    public final void deleteAll() {
        throw new UnsupportedOperationException("deleteAll() is not supported.");
    }
}
