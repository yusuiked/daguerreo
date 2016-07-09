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

import static com.google.common.base.CaseFormat.*;
import static org.jooq.impl.DSL.*;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SelectQuery;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.UniqueKey;
import org.jooq.UpdatableRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.yukung.daguerreo.domain.entity.Identifiable;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;

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
        return dsl
            .selectFrom(table)
            .fetch()
            .map(mapper());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> findAll(Iterable<ID> ids) {
        if (ids == null) {
            return Collections.emptyList();
        }
        Field<?>[] pk = pk();
        List<ID> keys = new ArrayList<>();
        ids.forEach(keys::add);

        List<E> result = new ArrayList<>();
        if (pk != null) {
            result = dsl
                .selectFrom(table)
                .where(in(pk, keys))
                .fetch()
                .map(mapper());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> findAll(Sort sort) {
        // TODO Throw exception if the index of the specified columns does not exist.
        SelectQuery<R> query = getQuery(sort);
        return query.fetch().map(mapper());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<E> findAll(Pageable pageable) {
        if (pageable == null) {
            return new PageImpl<>(findAll());
        }
        SelectQuery<R> query = getQuery(pageable);
        return new PageImpl<>(query.fetch().map(mapper()), pageable, count());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E findOne(ID id) {
        Field<?>[] pk = pk();
        R record = null;

        if (pk != null) {
            record = dsl
                .selectFrom(table)
                .where(equal(pk, id))
                .fetchOne();
        }
        return record == null ? null : mapper().map(record);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(ID id) {
        Field<?>[] pk = pk();

        return pk != null && dsl
            .selectCount()
            .from(table)
            .where(equal(pk, id))
            .fetchOne(0, Integer.class) > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        return dsl
            .selectCount()
            .from(table)
            .fetchOne(0, Long.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends E> S save(S entity) {
        Assert.notNull(entity);
        R record;

        if (getId(entity) == null) {
            record = dsl.newRecord(table, entity);
        } else {
            R fetched = fetchById(getId(entity));
            if (fetched != null) {
                fetched.from(entity);
                record = fetched;
            } else {
                record = dsl.newRecord(table, entity);
            }
        }
        // TODO 楽観的ロックでかち合った時に DataChangedException 拾って refresh() とリトライ
        record.store();
        return record.into(entity);
    }

    /**
     * {@inheritDoc}
     * <p>
     * NOTE: Please note that the case of large number of entity to be saved which is becomes very slowly.
     * Because in the inside are calling the save() in the loop.
     * </p>
     */
    @Override
    public <S extends E> Iterable<S> save(Iterable<S> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        // TODO if jOOQ supports auto-generated ID when the batch update, modify using it here.
        // See: https://github.com/jOOQ/jOOQ/issues/3327
        List<S> result = new ArrayList<>();
        entities.forEach(entity -> result.add(save(entity)));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(ID id) {
        Field<?>[] pk = pk();

        if (pk != null) {
            dsl
                .deleteFrom(table)
                .where(equal(pk, id))
                .execute();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(E entity) {
        delete(Collections.singletonList(entity));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Iterable<? extends E> entities) {
        Field<?>[] pk = pk();

        if (pk != null) {
            List<ID> ids = new ArrayList<>();
            entities.forEach(entity -> ids.add(getId(entity)));
            dsl
                .deleteFrom(table)
                .where(in(pk, ids))
                .execute();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteInBatch(Iterable<E> entities) {
        List<R> targets = new ArrayList<>();
        Field<?>[] pk = pk();

        for (E entity : entities) {
            R record = dsl.newRecord(table, entity);
            if (pk != null) {
                for (Field<?> field : pk) {
                    // To replace the "changed" flag with true which is same as the fetched record.
                    record.changed(field, false);
                }
                // If a entity property is NULL, but the column is NOT NULL
                // then we should let the database apply DEFAULT values
                for (int i = 0; i < record.size(); i++) {
                    if (record.getValue(i) == null) {
                        if (!record.field(i).getDataType().nullable()) {
                            record.changed(i, false);
                        }
                    }
                }
                targets.add(record);
            }
        }
        dsl.batchDelete(targets).execute();
    }

    /**
     * Deletion of all the records is not supported for ensure safety.
     */
    @Override
    public final void deleteAll() {
        throw new UnsupportedOperationException("deleteAll() is not supported.");
    }

    private ID getId(E entity) {
        Assert.notNull(entity);
        return entity.getId();
    }

    private Field<?>[] pk() {
        UniqueKey<R> key = table.getPrimaryKey();
        return key == null ? null : key.getFieldsArray();
    }

    @SuppressWarnings("unchecked")
    private Condition equal(Field<?>[] pk, ID id) {
        if (pk.length == 1) {
            return ((Field<Object>) pk[0]).equal(pk[0].getDataType().convert(id));
        } else {
            return row(pk).equal((Record) id);
        }
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    private Condition in(Field<?>[] pk, Collection<ID> ids) {
        if (pk.length == 1) {
            if (ids.size() == 1) {
                return equal(pk, ids.iterator().next());
            } else {
                return pk[0].in(pk[0].getDataType().convert(ids));
            }
        } else {
            return row(pk).in(ids.toArray(new Record[ids.size()]));
        }
    }

    private R fetchById(ID id) {
        Field<?>[] pk = pk();
        R record = null;

        if (pk != null) {
            record = dsl
                .selectFrom(table)
                .where(equal(pk, id))
                .fetchOne();
        }

        return record == null ? null : record;
    }

    private SelectQuery<R> getQuery(Sort sort) {
        SelectQuery<R> query = dsl.selectFrom(table).getQuery();
        // Do not sort if specified sort condition.
        if (sort == null) {
            return query;
        }
        for (Sort.Order order : sort) {
            // It's currently only allowed column name of lowercase.
            Field<?> field = table.field(name(LOWER_CAMEL.to(LOWER_UNDERSCORE, order.getProperty())));
            if (field == null) {
                // TODO Consider later that can't find the field which has sort condition.
                continue;
            }
            SortField<?> sortField;
            if (order.getDirection() == Sort.Direction.ASC) {
                sortField = field.asc();
            } else {
                sortField = field.desc();
            }
            query.addOrderBy(sortField);
        }
        return query;
    }

    private SelectQuery<R> getQuery(Pageable pageable) {
        SelectQuery<R> query = getQuery(pageable.getSort());
        query.addLimit(pageable.getOffset(), pageable.getPageSize());
        return query;
    }
}
