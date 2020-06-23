package ch.qiminfo.demo.controller;

import ch.qiminfo.demo.bean.AuthorBean;
import ch.qiminfo.demo.das.AuthorDAS;
import ch.qiminfo.demo.exception.AuthorNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/author")
public class AuthorRestController {

    @Autowired
    private AuthorDAS authorDAS;

    @GetMapping("/{uuid}")
    public AuthorBean get(@PathVariable(value = "uuid") String uuid) {
        Optional<AuthorBean> authorByUuid = this.authorDAS.getByUuid(uuid);
        if (!authorByUuid.isPresent()) {
            throw new AuthorNotFoundException(uuid);
        }
        return authorByUuid.get();
    }

    @GetMapping
    public List<AuthorBean> getAll() {
        return this.authorDAS.getAll();
    }
}