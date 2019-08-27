package com.infotech.batch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.infotech.batch.listener.JobCompletionNotificationListener;
import com.infotech.batch.model.Person;
import com.infotech.batch.processor.PersonItemProcessor;

import java.util.Arrays;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    @Bean
    public FlatFileItemReader<Person> reader() {
        FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
        reader.setResource(new ClassPathResource("persons.csv"));
        reader.setLineMapper(new DefaultLineMapper<Person>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[]{"firstName", "lastName", "email", "age"});
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }});
        }});
        return reader;
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    //        @Bean
//    public JdbcBatchItemWriter<Person> writer() {
//        JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
//        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
//        writer.setSql("INSERT INTO person (first_name, last_name) VALUES (:firstName, :lastName);");
//        writer.setDataSource(dataSource);
//        return writer;
//    }
    @Bean
    @StepScope
    public JdbcBatchItemWriter<Person> jdbcUpdateTable1Writer(DataSource dataSource) {
        JdbcBatchItemWriter<Person> jdbcBatchItemWriter = new JdbcBatchItemWriter<>();
        jdbcBatchItemWriter.setAssertUpdates(true);
        jdbcBatchItemWriter.setDataSource(dataSource);
        jdbcBatchItemWriter.setSql("INSERT INTO email (email) VALUES (:email)");
        jdbcBatchItemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
        return jdbcBatchItemWriter;
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Person> jdbcInsertTable2Writer(DataSource dataSource) {
        JdbcBatchItemWriter<Person> jdbcBatchItemWriter = new JdbcBatchItemWriter<>();
        jdbcBatchItemWriter.setDataSource(dataSource);
        jdbcBatchItemWriter.setSql("INSERT INTO age (age) VALUES (:age)");
        jdbcBatchItemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
        return jdbcBatchItemWriter;
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Person> jdbcUpdateTable3Writer(DataSource dataSource) {
        JdbcBatchItemWriter<Person> jdbcBatchItemWriter = new JdbcBatchItemWriter<>();
        jdbcBatchItemWriter.setAssertUpdates(true);
        jdbcBatchItemWriter.setDataSource(dataSource);
        jdbcBatchItemWriter.setSql("INSERT INTO person (first_name, last_name) VALUES (:firstName, :lastName)");
        jdbcBatchItemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
        return jdbcBatchItemWriter;
    }

    @Bean
    @StepScope
    public CompositeItemWriter<Person> compositeItemWriter(
            @Qualifier("jdbcUpdateTable1Writer") JdbcBatchItemWriter<Person> jdbcUpdateTable1Writer,
            @Qualifier("jdbcInsertTable2Writer") JdbcBatchItemWriter<Person> jdbcInsertTable2Writer,
            @Qualifier("jdbcUpdateTable3Writer") JdbcBatchItemWriter<Person> jdbcUpdateTable3Writer) {
        CompositeItemWriter<Person> writer = new CompositeItemWriter<>();
        writer.setDelegates(Arrays.asList(jdbcUpdateTable1Writer, jdbcInsertTable2Writer, jdbcUpdateTable3Writer));
        return writer;
    }


    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener) {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1())
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<Person, Person>chunk(100000)
                .reader(reader())
                .processor(processor())
                .writer(compositeItemWriter(jdbcUpdateTable1Writer(dataSource), jdbcInsertTable2Writer(dataSource), jdbcUpdateTable3Writer(dataSource)))
                .build();
    }
}