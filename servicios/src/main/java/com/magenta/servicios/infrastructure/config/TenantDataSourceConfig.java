package com.magenta.servicios.infrastructure.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Envuelve el bean {@code dataSource} auto-configurado por Spring Boot con
 * {@link TenantAwareDataSourceProxy} sin modificar la configuración de HikariCP.
 *
 * <p>Se usa {@link BeanPostProcessor} en lugar de declarar un {@code @Bean DataSource}
 * propio para no interferir con la auto-configuración de Flyway, JPA y Actuator
 * que también dependen del bean {@code dataSource}.
 */
@Component
public class TenantDataSourceConfig implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource dataSource && "dataSource".equals(beanName)) {
            return new TenantAwareDataSourceProxy(dataSource);
        }
        return bean;
    }
}
