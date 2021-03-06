package tw.com.citi.catalog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mapper {

    Class<? extends org.springframework.jdbc.core.RowMapper>  value();

}
