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

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import org.assertj.db.api.Assertions;
import org.assertj.db.type.Table;
import org.jooq.Field;
import org.jooq.impl.DefaultRecordMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yukung.daguerreo.Application;
import org.yukung.daguerreo.domain.entity.BookApi;
import org.yukung.daguerreo.infrastructure.tables.records.BookApiRecord;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.ninja_squad.dbsetup.Operations.*;
import static org.assertj.core.api.Assertions.*;
import static org.yukung.daguerreo.infrastructure.Tables.*;

/**
 * Unit tests for {@link BasicJooqRepository}.
 *
 * @author yukung
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
public class BasicJooqRepositoryTest {

    @Autowired
    private DummyRepository repository;

    // Setup for DB Testing
    @Autowired
    private DataSource ds;
    private static final String[] COLUMNS_BOOK_API =
        Arrays.stream(BOOK_API.fields())
            .map(Field::getName)
            .toArray(String[]::new);
    private static final Operation DELETE_ALL_BOOK_API = deleteAllFrom(BOOK_API.getName());
    private static final Operation INSERT_BOOK_API =
        insertInto(BOOK_API.getName()).columns(COLUMNS_BOOK_API)
            .values(1, "Amazon Product Advertising API", "https://ecs.amazonaws.jp/onca/xml")
            .values(2, "Google Books API", "https://www.googleapis.com/books/v1/volumes")
            .values(3, "楽天ブックス書籍検索API", "https://app.rakuten.co.jp/services/api/BooksBook/Search/20130522")
            .build();
    private static DbSetupTracker dbSetupTracker = new DbSetupTracker();

    @Before
    public void setUp() throws Exception {
        DbSetup dbSetup = new DbSetup(new DataSourceDestination(ds), sequenceOf(DELETE_ALL_BOOK_API, INSERT_BOOK_API));
        dbSetupTracker.launchIfNecessary(dbSetup);
    }

    @Test
    public void table() throws Exception {
        assertThat(repository.table()).isNotNull();
        assertThat(repository.table().getName()).isEqualToIgnoringCase("book_api");
    }

    @Test
    public void entityClass() throws Exception {
        assertThat(repository.entityClass()).isNotNull().isEqualTo(BookApi.class);
    }

    @Test
    public void mapper() throws Exception {
        assertThat(repository.mapper()).isNotNull().isExactlyInstanceOf(DefaultRecordMapper.class);
    }

    @Test
    public void findAll() throws Exception {
        // given
        dbSetupTracker.skipNextLaunch();

        // when
        List<BookApi> bookApis = repository.findAll();

        // then
        assertThat(bookApis)
            .isNotNull()
            .isNotEmpty()
            .hasSize(3)
            .extracting("id", "name", "url")
            .containsOnly(
                tuple(1, "Amazon Product Advertising API", "https://ecs.amazonaws.jp/onca/xml"),
                tuple(2, "Google Books API", "https://www.googleapis.com/books/v1/volumes"),
                tuple(3, "楽天ブックス書籍検索API", "https://app.rakuten.co.jp/services/api/BooksBook/Search/20130522")
            );
    }

    @Test
    public void findAllByIds() throws Exception {
        // given
        dbSetupTracker.skipNextLaunch();

        // when
        List<BookApi> bookApis = repository.findAll(Arrays.asList(1, 3));

        // then
        assertThat(bookApis)
            .isNotNull()
            .isNotEmpty()
            .hasSize(2)
            .extracting("id", "name", "url")
            .containsOnly(
                tuple(1, "Amazon Product Advertising API", "https://ecs.amazonaws.jp/onca/xml"),
                tuple(3, "楽天ブックス書籍検索API", "https://app.rakuten.co.jp/services/api/BooksBook/Search/20130522")
            );
    }

    @Test
    public void findAllByIdsWhichAreNothing() throws Exception {
        // given
        dbSetupTracker.skipNextLaunch();
        List<Integer> nothing = null;

        // when
        List<BookApi> empty = repository.findAll(nothing);

        // then
        assertThat(empty)
            .isNotNull()
            .isEmpty();
    }

    @Test
    public void findAllByIdsWhichIsEmpty() throws Exception {
        // given
        dbSetupTracker.skipNextLaunch();
        List<Integer> emptyList = Collections.emptyList();

        // when
        List<BookApi> empty = repository.findAll(emptyList);

        // then
        assertThat(empty)
            .isNotNull()
            .isEmpty();
    }

    @Test
    public void findAllByAscendingSort() throws Exception {
        // given
        dbSetupTracker.skipNextLaunch();

        // when
        List<BookApi> bookApis = repository.findAll(new Sort(new Order(Sort.Direction.ASC, "id")));

        // then
        assertThat(bookApis)
            .isNotNull()
            .isNotEmpty()
            .hasSize(3)
            .extracting("id", "name", "url")
            .containsExactly(
                tuple(1, "Amazon Product Advertising API", "https://ecs.amazonaws.jp/onca/xml"),
                tuple(2, "Google Books API", "https://www.googleapis.com/books/v1/volumes"),
                tuple(3, "楽天ブックス書籍検索API", "https://app.rakuten.co.jp/services/api/BooksBook/Search/20130522")
            );
    }

    @Test
    public void findAllByDescendingSort() throws Exception {
        // given
        dbSetupTracker.skipNextLaunch();

        // when
        List<BookApi> bookApis = repository.findAll(new Sort(new Order(Sort.Direction.DESC, "id")));

        // then
        assertThat(bookApis)
            .isNotNull()
            .isNotEmpty()
            .hasSize(3)
            .extracting("id", "name", "url")
            .containsExactly(
                tuple(3, "楽天ブックス書籍検索API", "https://app.rakuten.co.jp/services/api/BooksBook/Search/20130522"),
                tuple(2, "Google Books API", "https://www.googleapis.com/books/v1/volumes"),
                tuple(1, "Amazon Product Advertising API", "https://ecs.amazonaws.jp/onca/xml")
            );
    }

    @Test
    public void findAllByPageable() throws Exception {
        // given
        dbSetupTracker.skipNextLaunch();

        // when
        PageRequest page1 = new PageRequest(0, 2);
        PageRequest page2 = new PageRequest(1, 2);
        Page<BookApi> bookApis1 = repository.findAll(page1);
        Page<BookApi> bookApis2 = repository.findAll(page2);

        // then
        assertThat(bookApis1)
            .isNotNull()
            .isNotEmpty()
            .hasSize(2)
            .extracting("id", "name", "url")
            .containsExactly(
                tuple(1, "Amazon Product Advertising API", "https://ecs.amazonaws.jp/onca/xml"),
                tuple(2, "Google Books API", "https://www.googleapis.com/books/v1/volumes")
            );
        assertThat(bookApis1.getNumber()).isEqualTo(0);
        assertThat(bookApis1.getNumberOfElements()).isEqualTo(2);
        assertThat(bookApis1.getSize()).isEqualTo(2);
        assertThat(bookApis1.getTotalPages()).isEqualTo(2);
        assertThat(bookApis1.getTotalElements()).isEqualTo(3);
        assertThat(bookApis1.isFirst()).isTrue();
        assertThat(bookApis1.isLast()).isFalse();
        assertThat(bookApis1.hasNext()).isTrue();
        assertThat(bookApis1.hasPrevious()).isFalse();
        assertThat(bookApis2)
            .isNotNull()
            .isNotEmpty()
            .hasSize(1)
            .extracting("id", "name", "url")
            .containsExactly(
                tuple(3, "楽天ブックス書籍検索API", "https://app.rakuten.co.jp/services/api/BooksBook/Search/20130522")
            );
        assertThat(bookApis2.getNumber()).isEqualTo(1);
        assertThat(bookApis2.getNumberOfElements()).isEqualTo(1);
        assertThat(bookApis2.getSize()).isEqualTo(2);
        assertThat(bookApis2.getTotalPages()).isEqualTo(2);
        assertThat(bookApis2.getTotalElements()).isEqualTo(3);
        assertThat(bookApis2.isFirst()).isFalse();
        assertThat(bookApis2.isLast()).isTrue();
        assertThat(bookApis2.hasNext()).isFalse();
        assertThat(bookApis2.hasPrevious()).isTrue();
    }

    @Test
    public void findAllByPageableAndDescendingSort() throws Exception {
        // given
        dbSetupTracker.skipNextLaunch();

        // when
        PageRequest page1 = new PageRequest(0, 2, Sort.Direction.DESC, "id");
        PageRequest page2 = new PageRequest(1, 2, Sort.Direction.DESC, "id");
        Page<BookApi> bookApis1 = repository.findAll(page1);
        Page<BookApi> bookApis2 = repository.findAll(page2);

        // then
        assertThat(bookApis1)
            .isNotNull()
            .isNotEmpty()
            .hasSize(2)
            .extracting("id", "name", "url")
            .containsExactly(
                tuple(3, "楽天ブックス書籍検索API", "https://app.rakuten.co.jp/services/api/BooksBook/Search/20130522"),
                tuple(2, "Google Books API", "https://www.googleapis.com/books/v1/volumes")
            );
        assertThat(bookApis1.getNumber()).isEqualTo(0);
        assertThat(bookApis1.getNumberOfElements()).isEqualTo(2);
        assertThat(bookApis1.getSize()).isEqualTo(2);
        assertThat(bookApis1.getTotalPages()).isEqualTo(2);
        assertThat(bookApis1.getTotalElements()).isEqualTo(3);
        assertThat(bookApis1.isFirst()).isTrue();
        assertThat(bookApis1.isLast()).isFalse();
        assertThat(bookApis1.hasNext()).isTrue();
        assertThat(bookApis1.hasPrevious()).isFalse();
        assertThat(bookApis2)
            .isNotNull()
            .isNotEmpty()
            .hasSize(1)
            .extracting("id", "name", "url")
            .containsExactly(
                tuple(1, "Amazon Product Advertising API", "https://ecs.amazonaws.jp/onca/xml")
            );
        assertThat(bookApis2.getNumber()).isEqualTo(1);
        assertThat(bookApis2.getNumberOfElements()).isEqualTo(1);
        assertThat(bookApis2.getSize()).isEqualTo(2);
        assertThat(bookApis2.getTotalPages()).isEqualTo(2);
        assertThat(bookApis2.getTotalElements()).isEqualTo(3);
        assertThat(bookApis2.isFirst()).isFalse();
        assertThat(bookApis2.isLast()).isTrue();
        assertThat(bookApis2.hasNext()).isFalse();
        assertThat(bookApis2.hasPrevious()).isTrue();
    }

    @Test
    public void findAllByPageableNothing() throws Exception {
        // given
        dbSetupTracker.skipNextLaunch();
        Pageable nothing = null;

        // when
        Page<BookApi> bookApis = repository.findAll(nothing);

        // then
        assertThat(bookApis)
            .isNotNull()
            .isNotEmpty()
            .hasSize(3)
            .extracting("id", "name", "url")
            .containsExactly(
                tuple(1, "Amazon Product Advertising API", "https://ecs.amazonaws.jp/onca/xml"),
                tuple(2, "Google Books API", "https://www.googleapis.com/books/v1/volumes"),
                tuple(3, "楽天ブックス書籍検索API", "https://app.rakuten.co.jp/services/api/BooksBook/Search/20130522")
            );
        assertThat(bookApis.getNumber()).isEqualTo(0);
        assertThat(bookApis.getNumberOfElements()).isEqualTo(3);
        assertThat(bookApis.getSize()).isEqualTo(0);
        assertThat(bookApis.getTotalPages()).isEqualTo(1);
        assertThat(bookApis.getTotalElements()).isEqualTo(3);
        assertThat(bookApis.isFirst()).isTrue();
        assertThat(bookApis.isLast()).isTrue();
        assertThat(bookApis.hasNext()).isFalse();
        assertThat(bookApis.hasPrevious()).isFalse();
    }

    @Test
    public void findOne() throws Exception {
        // given
        dbSetupTracker.skipNextLaunch();

        // when
        BookApi bookApi = repository.findOne(2);

        // then
        assertThat(bookApi).isNotNull();
        assertThat(bookApi.getId()).isEqualTo(2);
        assertThat(bookApi.getName()).isEqualTo("Google Books API");
        assertThat(bookApi.getUrl()).isEqualTo("https://www.googleapis.com/books/v1/volumes");
    }

    @Test
    public void findOneNotExists() throws Exception {
        // given
        dbSetupTracker.skipNextLaunch();

        // when
        BookApi nothing = repository.findOne(4);

        // then
        assertThat(nothing).isNull();
    }

    @Test
    public void exists() throws Exception {
        // given
        dbSetupTracker.skipNextLaunch();

        // when
        boolean exists = repository.exists(1);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    public void notExists() throws Exception {
        // given
        dbSetupTracker.skipNextLaunch();

        // when
        boolean exists = repository.exists(4);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    public void count() throws Exception {
        // given
        dbSetupTracker.skipNextLaunch();

        // when
        long count = repository.count();

        // then
        assertThat(count).isEqualTo(3);
    }

    @Test
    public void insertByAutoGeneratedId() throws Exception {
        // given
        BookApi bookApi = new BookApi(null, "国立国会図書館サーチAPI", "http://iss.ndl.go.jp/api/sru");

        // when
        BookApi inserted = repository.save(bookApi);

        // then
        assertThat(inserted).isNotNull();
        assertThat(inserted.getName()).isEqualTo(bookApi.getName());
        assertThat(inserted.getUrl()).isEqualTo(bookApi.getUrl());
        assertThat(inserted.getId()).isNotNull().isPositive();
        Assertions.assertThat(new Table(ds, BOOK_API.getName()))
            .hasNumberOfRows(4)
            .row().hasValues(1, "Amazon Product Advertising API", "https://ecs.amazonaws.jp/onca/xml")
            .row().hasValues(2, "Google Books API", "https://www.googleapis.com/books/v1/volumes")
            .row().hasValues(3, "楽天ブックス書籍検索API", "https://app.rakuten.co.jp/services/api/BooksBook/Search/20130522")
            .row().hasValues(inserted.getId(), inserted.getName(), inserted.getUrl());
    }

    @Test
    public void insertBySpecifiedId() throws Exception {
        // given
        BookApi bookApi = new BookApi(4, "国立国会図書館サーチAPI", "http://iss.ndl.go.jp/api/sru");

        // when
        BookApi inserted = repository.save(bookApi);

        // then
        assertThat(inserted).isNotNull();
        assertThat(inserted.getName()).isEqualTo(bookApi.getName());
        assertThat(inserted.getUrl()).isEqualTo(bookApi.getUrl());
        assertThat(inserted.getId()).isNotNull().isEqualTo(4);
        Assertions.assertThat(new Table(ds, BOOK_API.getName()))
            .hasNumberOfRows(4)
            .row().hasValues(1, "Amazon Product Advertising API", "https://ecs.amazonaws.jp/onca/xml")
            .row().hasValues(2, "Google Books API", "https://www.googleapis.com/books/v1/volumes")
            .row().hasValues(3, "楽天ブックス書籍検索API", "https://app.rakuten.co.jp/services/api/BooksBook/Search/20130522")
            .row().hasValues(inserted.getId(), inserted.getName(), inserted.getUrl());
    }

    @Test
    public void insertNull() throws Exception {
        // given
        BookApi bookApi = null;

        // when
        Throwable thrown = catchThrowable(() -> repository.save(bookApi));

        // then
        assertThat(thrown)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("null");
        Assertions.assertThat(new Table(ds, BOOK_API.getName()))
            .hasNumberOfRows(3)
            .row().hasValues(1, "Amazon Product Advertising API", "https://ecs.amazonaws.jp/onca/xml")
            .row().hasValues(2, "Google Books API", "https://www.googleapis.com/books/v1/volumes")
            .row().hasValues(3, "楽天ブックス書籍検索API", "https://app.rakuten.co.jp/services/api/BooksBook/Search/20130522");
    }

    @Test
    public void update() throws Exception {
        // given
        BookApi bookApi = new BookApi(2, "ダミーAPI", "http://example.com/api/v2/book");

        // when
        BookApi updated = repository.save(bookApi);

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo(bookApi.getName());
        assertThat(updated.getUrl()).isEqualTo(bookApi.getUrl());
        assertThat(updated.getId()).isNotNull().isEqualTo(2);
        Assertions.assertThat(new Table(ds, BOOK_API.getName()))
            .hasNumberOfRows(3)
            .row().hasValues(1, "Amazon Product Advertising API", "https://ecs.amazonaws.jp/onca/xml")
            .row().hasValues(updated.getId(), updated.getName(), updated.getUrl())
            .row().hasValues(3, "楽天ブックス書籍検索API", "https://app.rakuten.co.jp/services/api/BooksBook/Search/20130522");
    }

    @Test
    public void multiSave() throws Exception {
        // given
        List<BookApi> bookApis = Arrays.asList(
            new BookApi(3, "ダミーAPI", "http://example.com/api/v2/book"), // update
            new BookApi(null, "新規書籍API", "http://newbooks.com/api/v1/book") // insert
        );

        // when
        Iterable<BookApi> saved = repository.save(bookApis);

        // then
        assertThat(saved)
            .isNotNull()
            .isNotEmpty()
            .hasSize(2)
            .filteredOn(bookApi -> bookApi.getId() != null)
            .containsOnlyOnce(new BookApi(3, "ダミーAPI", "http://example.com/api/v2/book"))
            .extracting("name", "url")
            .containsOnlyOnce(
                tuple("新規書籍API", "http://newbooks.com/api/v1/book")
            );
        Iterator<BookApi> iterator = saved.iterator();
        BookApi bookApi1 = iterator.next();
        BookApi bookApi2 = iterator.next();
        Assertions.assertThat(new Table(ds, BOOK_API.getName()))
            .hasNumberOfRows(4)
            .row().hasValues(1, "Amazon Product Advertising API", "https://ecs.amazonaws.jp/onca/xml")
            .row().hasValues(2, "Google Books API", "https://www.googleapis.com/books/v1/volumes")
            .row().hasValues(bookApi1.getId(), bookApi1.getName(), bookApi1.getUrl())
            .row().hasValues(bookApi2.getId(), bookApi2.getName(), bookApi2.getUrl());
    }

    @Test
    public void multiSaveWithEmptyList() throws Exception {
        // given
        List<BookApi> nothing = Collections.emptyList();

        // when
        Iterable<BookApi> empty = repository.save(nothing);

        // then
        assertThat(empty)
            .isNotNull()
            .isEmpty();
    }

    @Test
    public void multiSaveWithNull() throws Exception {
        // given
        List<BookApi> nothing = null;

        // when
        Iterable<BookApi> empty = repository.save(nothing);

        // then
        assertThat(empty)
            .isNotNull()
            .isEmpty();
    }

    @Test
    public void deleteById() throws Exception {
        // given
        Integer id = 1;

        // when
        repository.delete(id);

        // then
        Assertions.assertThat(new Table(ds, BOOK_API.getName()))
            .hasNumberOfRows(2)
            .column(BOOK_API.ID.getName()).hasValues(2, 3);
    }

    @Test
    public void deleteByNotExistsId() throws Exception {
        // given
        Integer id = 4;

        // when
        repository.delete(id);

        // then
        Assertions.assertThat(new Table(ds, BOOK_API.getName()))
            .hasNumberOfRows(3)
            .column(BOOK_API.ID.getName()).hasValues(1, 2, 3);
    }

    @Test
    public void deleteByEntity() throws Exception {
        // given
        BookApi bookApi = new BookApi(3, "楽天ブックス書籍検索API", "https://app.rakuten.co.jp/services/api/BooksBook/Search/20130522");

        // when
        repository.delete(bookApi);

        // then
        Assertions.assertThat(new Table(ds, BOOK_API.getName()))
            .hasNumberOfRows(2)
            .column(BOOK_API.ID.getName()).hasValues(1, 2);
    }

    @Test
    public void multiDelete() throws Exception {
        // given
        List<BookApi> bookApis = Arrays.asList(
            new BookApi(1, "Amazon Product Advertising API", "https://ecs.amazonaws.jp/onca/xml"),
            new BookApi(3, "楽天ブックス書籍検索API", "https://app.rakuten.co.jp/services/api/BooksBook/Search/20130522")
        );

        // when
        repository.delete(bookApis);

        // then
        Assertions.assertThat(new Table(ds, BOOK_API.getName()))
            .hasNumberOfRows(1)
            .row().hasValues(2, "Google Books API", "https://www.googleapis.com/books/v1/volumes");
    }

    @Test
    public void deleteInBatch() throws Exception {
        // given
        List<BookApi> bookApis = Arrays.asList(
            new BookApi(1, "Amazon Product Advertising API", "https://ecs.amazonaws.jp/onca/xml"),
            new BookApi(3, "楽天ブックス書籍検索API", "https://app.rakuten.co.jp/services/api/BooksBook/Search/20130522")
        );

        // when
        repository.deleteInBatch(bookApis);

        // then
        Assertions.assertThat(new Table(ds, BOOK_API.getName()))
            .hasNumberOfRows(1)
            .row().hasValues(2, "Google Books API", "https://www.googleapis.com/books/v1/volumes");
    }

    @Test
    public void deleteAll() throws Exception {
        // when
        Throwable thrown = catchThrowable(() -> repository.deleteAll());

        // then
        assertThat(thrown)
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("deleteAll() is not supported.");
    }

    @Repository
    public static class DummyRepository
        extends BasicJooqRepository<BookApiRecord, org.yukung.daguerreo.infrastructure.tables.BookApi, BookApi, Integer> {
    }
}
