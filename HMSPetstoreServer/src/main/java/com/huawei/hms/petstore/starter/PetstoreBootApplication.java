/**
 * Copyright 2020 Huawei Technologies Co., Ltd. All rights reserved. eSDK is licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.huawei.hms.petstore.starter;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.huawei.hms.petstore.configuration.DataSourceConfig;

@SpringBootApplication(scanBasePackages = {"com.huawei.hms.petstore"})
@ServletComponentScan
@MapperScan({"com.huawei.hms.petstore.dao"})
public class PetstoreBootApplication extends SpringBootServletInitializer {

    private static final Logger LOG = LogManager.getLogger(PetstoreBootApplication.class);

    @Autowired
    private DataSourceConfig config;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(PetstoreBootApplication.class, args);

    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PetstoreBootApplication.class);
    }

    /**
     * initial DataSource
     * 
     * @return javax.sql.DataSource DataSource instance
     */
    @Bean
    public DataSource dataSource() {
        LOG.info("Begin init datasource");
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(config.getDriver());
        ds.setUrl(config.getUrl());
        ds.setUsername(config.getUser());
        // TODO demo not encrypt database password
        ds.setPassword(config.getPassword());
        ds.setMaxTotal(200);
        ds.setMaxIdle(60);
        ds.setMaxWaitMillis(500);
        ds.setDefaultAutoCommit(false);
        ds.setValidationQuery("SELECT COUNT(*) FROM DUAL");
        ds.setTestOnBorrow(true);
        ds.setTestWhileIdle(true);
        ds.setTimeBetweenEvictionRunsMillis(3600000);
        ds.setMinEvictableIdleTimeMillis(18000000);
        LOG.info("End init datasource");
        return ds;
    }

    /**
     * initial sql session factory
     * 
     * @return SqlSessionFactory SqlSessionFactory
     * @throws Exception exception
     */
    @Bean
    public SqlSessionFactory sqlSessionFactoryBean() throws Exception {
        SqlSessionFactoryBean sessionBean = new SqlSessionFactoryBean();
        sessionBean.setDataSource(dataSource());
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionBean.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
        sessionBean.setVfs(SpringBootVFS.class);
        sessionBean.setTypeAliasesPackage("com.huawei.hms.petstore.dao.domain");
        return sessionBean.getObject();
    }

    /**
     * initial PlatformTransactionManager
     * @return PlatformTransactionManager PlatformTransactionManager
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }
}
