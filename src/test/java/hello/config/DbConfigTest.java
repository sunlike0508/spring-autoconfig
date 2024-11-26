package hello.config;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionManager;

@Slf4j
@SpringBootTest
class DbConfigTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    TransactionManager transactionManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void checkBean() {
        log.info("datasource = {}", dataSource);
        log.info("transactionManager = {}", transactionManager);
        log.info("jdbcTemplate = {}", jdbcTemplate);

        assertNotNull(dataSource);
        assertNotNull(transactionManager);
        assertNotNull(jdbcTemplate);
    }
}
