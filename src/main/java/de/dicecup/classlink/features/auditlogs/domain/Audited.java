package de.dicecup.classlink.features.auditlogs.domain;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audited {
    String action();
    String resource() default "";
    String detail() default "";
    //TODO: explain this
    int actorIdArgIndex() default -1;
}
