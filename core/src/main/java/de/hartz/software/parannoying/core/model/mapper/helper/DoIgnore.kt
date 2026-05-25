package de.hartz.software.parannoying.core.model.mapper.helper
// https://mapstruct.org/faq/#How-to-avoid-MapStruct-selecting-a-method
@org.mapstruct.Qualifier
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DoIgnore