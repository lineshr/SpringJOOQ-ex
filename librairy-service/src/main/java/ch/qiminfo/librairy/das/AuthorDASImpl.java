package ch.qiminfo.librairy.das;

import ch.qiminfo.librairy.bean.AuthorBean;
import ch.qiminfo.librairy.db.tables.records.AuthorRecord;
import ch.qiminfo.librairy.mapper.AuthorMapper;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ch.qiminfo.librairy.db.tables.Author.AUTHOR;
import static java.util.Objects.requireNonNull;

@Repository
public class AuthorDASImpl implements AuthorDAS {

    private final DSLContext dsl;

    private final AuthorMapper authorMapper;

    @Autowired
    public AuthorDASImpl(DSLContext dsl, AuthorMapper authorMapper) {
        this.dsl = requireNonNull(dsl);
        this.authorMapper = requireNonNull(authorMapper);
    }

    @Override
    public List<AuthorBean> getAll() {
        Result<AuthorRecord> records = this.dsl.selectFrom(AUTHOR).fetch();
        return records.stream()
                .map(this.authorMapper::map)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AuthorBean> getByUuid(String uuid) {
        AuthorRecord record = this.dsl.selectFrom(AUTHOR).where(AUTHOR.UUID.eq(uuid)).fetchOne();
        return record != null ? Optional.of(this.authorMapper.map(record)) : Optional.empty();
    }
}