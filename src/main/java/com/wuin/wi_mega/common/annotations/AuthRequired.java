package com.wuin.wi_mega.common.annotations;


import com.wuin.wi_mega.common.enums.UserTypeEnum;

import java.lang.annotation.*;

/**
 * @author test
 * @version 1.0
 * @created 2023/3/3.
 */
@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthRequired {
    boolean required() default true;

    UserTypeEnum[] userTypes() default {};
}
