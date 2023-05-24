package com.auction.usedauction;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
@Component
public class InitProcedure {

    private final DataSource dataSource;
    public void initProcedure() throws IOException {
        ResourceDatabasePopulator resourceDatabasePopulator=new ResourceDatabasePopulator();
        resourceDatabasePopulator.setSeparator("DELIMITER");
        InputStream is= new ClassPathResource("testProcedure.sql").getInputStream();
        resourceDatabasePopulator.addScript(new InputStreamResource(is));
        resourceDatabasePopulator.execute(dataSource);
    }
}
