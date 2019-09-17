package cn.sexycode.spring.study.chapter4;

import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Formatter;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringFormatAnnotationFormatterFactory implements AnnotationFormatterFactory<StringFormat> {
    /**
     * The types of fields that may be annotated with the &lt;A&gt; annotation.
     */
    @Override
    public Set<Class<?>> getFieldTypes() {
        return Set.of(List.class, String.class);
    }

    /**
     * Get the Printer to print the value of a field of {@code fieldType} annotated with
     * {@code annotation}.
     * <p>If the type T the printer accepts is not assignable to {@code fieldType}, a
     * coercion from {@code fieldType} to T will be attempted before the Printer is invoked.
     *
     * @param annotation the annotation instance
     * @param fieldType  the type of field that was annotated
     * @return the printer
     */
    @Override
    public Printer<?> getPrinter(StringFormat annotation, Class<?> fieldType) {
        return new StringFormatFormatter(annotation.pattern());
    }

    /**
     * Get the Parser to parse a submitted value for a field of {@code fieldType}
     * annotated with {@code annotation}.
     * <p>If the object the parser returns is not assignable to {@code fieldType},
     * a coercion to {@code fieldType} will be attempted before the field is set.
     *
     * @param annotation the annotation instance
     * @param fieldType  the type of field that was annotated
     * @return the parser
     */
    @Override
    public Parser<?> getParser(StringFormat annotation, Class<?> fieldType) {
        return new StringFormatFormatter(annotation.pattern());
    }

    private static class StringFormatFormatter implements Formatter<Collection<String>> {
        private Pattern pattern;
         StringFormatFormatter(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        /**
         * Parse a text String to produce a T.
         *
         * @param text   the text string
         * @param locale the current user locale
         * @return an instance of T
         * @throws ParseException           when a parse exception occurs in a java.text parsing library
         * @throws IllegalArgumentException when a parse exception occurs
         */
        @Override
        public Collection<String> parse(String text, Locale locale) throws ParseException {
            List<String> list = new ArrayList<>(){};
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                list.add(matcher.group());
            }
            return list;
        }


        /**
         * Print the object of type T for display.
         *
         * @param object the instance to print
         * @param locale the current user locale
         * @return the printed text string
         */
        @Override
        public String print(Collection<String> object, Locale locale) {
            return object.toString();
        }
    }
}
