package ch.qiminfo.demo.das;

import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static ch.qiminfo.das.db.public_.tables.Author.AUTHOR;
import static ch.qiminfo.das.db.public_.tables.AuthorBook.AUTHOR_BOOK;
import static ch.qiminfo.das.db.public_.tables.Book.BOOK;
import static org.junit.Assert.assertEquals;

@JooqTest
@RunWith(SpringRunner.class)
public class JooqIntegrationTest {

    private final static String AUTHOR_BERT_BATES_UUID = "ea14c2ba-b0af-11ea-b3de-0242ac130004";
    private final static String AUTHOR_BRYAN_BASHAM_UUID = "ff302fc2-b0af-11ea-b3de-0242ac130004";

    private final static String BOOK_HEAD_FIRST_JAVA_UUID = "08decfa6-b0b0-11ea-b3de-0242ac130004";
    private final static String BOOK_OCA_OCP_UUID = "24a453aa-b0b0-11ea-b3de-0242ac130004";

    @Autowired
    private DSLContext dsl;

    @Test
    public void givenValUUIDData_whenInserting_thenSucceed() {
        String authorUuid = UUID.randomUUID().toString();
        dsl.insertInto(AUTHOR)
                .set(AUTHOR.UUID, authorUuid)
                .set(AUTHOR.FIRST_NAME, "Herbert")
                .set(AUTHOR.LAST_NAME, "Schildt")
                .execute();

        String bookUuid = UUID.randomUUID().toString();
        dsl.insertInto(BOOK)
                .set(BOOK.UUID, bookUuid)
                .set(BOOK.TITLE, "A Beginner's GuUUIDe")
                .execute();

        dsl.insertInto(AUTHOR_BOOK)
                .set(AUTHOR_BOOK.AUTHOR_UUID, authorUuid)
                .set(AUTHOR_BOOK.BOOK_UUID, bookUuid)
                .execute();

        final Result<Record3<String, String, Integer>> result = dsl.select(AUTHOR.UUID, AUTHOR.LAST_NAME, DSL.count())
                .from(AUTHOR).join(AUTHOR_BOOK).on(AUTHOR.UUID.equal(AUTHOR_BOOK.AUTHOR_UUID))
                .join(BOOK).on(AUTHOR_BOOK.BOOK_UUID.equal(BOOK.UUID))
                .groupBy(AUTHOR.LAST_NAME)
                .orderBy(AUTHOR.LAST_NAME.desc())
                .fetch();

        assertEquals(3, result.size());
        assertEquals("Sierra", result.getValue(0, AUTHOR.LAST_NAME));
        assertEquals(Integer.valueOf(2), result.getValue(0, DSL.count()));
        assertEquals("Bates", result.getValue(2, AUTHOR.LAST_NAME));
        assertEquals(Integer.valueOf(1), result.getValue(2, DSL.count()));
    }

    @Test(expected = DataAccessException.class)
    public void givenInvalUUIDData_whenInserting_thenFail() {

        String unknownBookUuid = UUID.randomUUID().toString();

        dsl.insertInto(AUTHOR_BOOK)
                .set(AUTHOR_BOOK.AUTHOR_UUID, AUTHOR_BERT_BATES_UUID)
                .set(AUTHOR_BOOK.BOOK_UUID, unknownBookUuid)
                .execute();
    }

    @Test
    public void givenValUUIDData_whenUpdating_thenSucceed() {
        dsl.update(AUTHOR)
                .set(AUTHOR.LAST_NAME, "Baeldung")
                .where(AUTHOR.UUID.equal(AUTHOR_BRYAN_BASHAM_UUID))
                .execute();

        dsl.update(BOOK)
                .set(BOOK.TITLE, "Building your REST API with Spring")
                .where(BOOK.UUID.equal(BOOK_OCA_OCP_UUID))
                .execute();

        dsl.insertInto(AUTHOR_BOOK)
                .set(AUTHOR_BOOK.AUTHOR_UUID, AUTHOR_BRYAN_BASHAM_UUID)
                .set(AUTHOR_BOOK.BOOK_UUID, BOOK_OCA_OCP_UUID)
                .execute();

        final Result<Record3<String, String, String>> result = dsl.select(AUTHOR.UUID, AUTHOR.LAST_NAME, BOOK.TITLE)
                .from(AUTHOR).join(AUTHOR_BOOK).on(AUTHOR.UUID.equal(AUTHOR_BOOK.AUTHOR_UUID))
                .join(BOOK).on(AUTHOR_BOOK.BOOK_UUID.equal(BOOK.UUID))
                .where(AUTHOR.UUID.equal(AUTHOR_BRYAN_BASHAM_UUID))
                .fetch();

        assertEquals(1, result.size());
        assertEquals(AUTHOR_BRYAN_BASHAM_UUID, result.getValue(0, AUTHOR.UUID));
        assertEquals("Baeldung", result.getValue(0, AUTHOR.LAST_NAME));
        assertEquals("Building your REST API with Spring", result.getValue(0, BOOK.TITLE));
    }

    @Test
    public void givenValUUIDData_whenDeleting_thenSucceed() {
        dsl.delete(AUTHOR)
                .where(AUTHOR.UUID.eq(AUTHOR_BRYAN_BASHAM_UUID))
                .execute();

        final Result<Record3<String, String, String>> result = dsl.select(AUTHOR.UUID, AUTHOR.FIRST_NAME, AUTHOR.LAST_NAME)
                .from(AUTHOR).fetch();

        assertEquals(2, result.size());
    }

    @Test(expected = DataAccessException.class)
    public void givenInvalUUIDData_whenDeleting_thenFail() {
        dsl.delete(BOOK)
                .where(BOOK.UUID.eq(BOOK_HEAD_FIRST_JAVA_UUID))
                .execute();
    }
}