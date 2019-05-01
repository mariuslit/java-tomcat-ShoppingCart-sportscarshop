package lt.bta.java2.api.filters;


import javax.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Anotacijos interfeisas (aprasas), kur ir kaip gali buti naudojama
 */
@NameBinding
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface AccessRoles {
    Role[] value() default {};
}
