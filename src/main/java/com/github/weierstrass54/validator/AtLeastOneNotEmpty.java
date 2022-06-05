package com.github.weierstrass54.validator;

import org.springframework.beans.BeanUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.beans.PropertyDescriptor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import java.util.stream.Stream;

@Constraint(validatedBy = AtLeastOneNotEmpty.AtLeastOneNotEmptyValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOneNotEmpty {
    String message() default "По крайней мере одно поле должно быть непустым.";
    String[] fields() default {};
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class AtLeastOneNotEmptyValidator implements ConstraintValidator<AtLeastOneNotEmpty, Object> {
        private List<String> fields;

        @Override
        public void initialize(AtLeastOneNotEmpty constraintAnnotation) {
            fields = Arrays.asList(constraintAnnotation.fields());
        }

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            boolean isInvalid = getPropertyDescriptors(value).map(pd -> {
                try {
                    Object obj = pd.getReadMethod().invoke(pd);
                    if (obj instanceof CharSequence cs) {
                        return cs.isEmpty();
                    }
                    if (obj instanceof Collection<?> c) {
                        return c.isEmpty();
                    }
                    if (obj instanceof Map<?,?> m) {
                        return m.isEmpty();
                    }
                    if (obj instanceof Object[] objs) {
                        return objs.length == 0;
                    }
                    return false;
                }
                catch (Exception e) {
                    return false;
                }
            }).reduce(true, (a, b) -> a && b);
            return !isInvalid;
        }

        private Stream<PropertyDescriptor> getPropertyDescriptors(Object value) {
            return Optional.ofNullable(value).map(v -> {
                PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(v.getClass());
                return !fields.isEmpty() ? Arrays.stream(pds).filter(pd -> fields.contains(pd.getName())) : Arrays.stream(pds);
            }).orElse(Stream.empty());
        }
    }
}

/*
        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            boolean isInvalid = Try.sequence(
                getPropertyDescriptors(value).map(pd ->
                    Try.of(() -> pd.getReadMethod().invoke(pd))
                        .map(Option::of)
                        .filter(Option::isDefined)
                        .map(Option::get)
                        .map(obj -> Match(obj).of(
                            Case($(instanceOf(CharSequence.class)), CharSequence::isEmpty),
                            Case($(instanceOf(Collection.class)), Collection::isEmpty),
                            Case($(instanceOf(Map.class)), Map::isEmpty),
                            Case($(instanceOf(Object[].class)), objs -> objs.length == 0),
                            Case($(), __ -> false)
                        ))
                    )
                .collect(Collectors.toList())
            ).getOrElse(Seq(true)).fold(true, (a, b) -> a && b);
            return !isInvalid;
        }

        private Array<PropertyDescriptor> getPropertyDescriptors(Object value) {
            return Option.of(value).map(v -> {
                PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(v.getClass());
                return !fields.isEmpty() ? Array.of(pds).filter(pd -> fields.contains(pd.getName())) : Array.of(pds);
            }).getOrElse(Array.empty());
        }
    }
}
 */