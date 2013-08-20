package ru.igarin.base.restlib.provider.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/*
 * 
 * Supported types:
 * String
 * int
 * long
 * double
 */

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ProviderDataBaseTable {
	int version() default 1;
}
