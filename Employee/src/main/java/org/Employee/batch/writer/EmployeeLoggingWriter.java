package org.Employee.batch.writer;

import org.Employee.entity.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class EmployeeLoggingWriter
        implements ItemWriter<Employee> {

    private static final Logger log =
            LoggerFactory.getLogger(EmployeeLoggingWriter.class);

    @Override
    public void write(Chunk<? extends Employee> chunk) {

        log.info(
                "TEST WRITER received {} employees",
                chunk.size()
        );
    }
}
