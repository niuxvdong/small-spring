package cn.itnxd.springframework;

import cn.itnxd.springframework.converter.StringToBooleanConverter;
import cn.itnxd.springframework.converter.StringToIntegerConverter;
import cn.itnxd.springframework.core.convert.converter.Converter;
import cn.itnxd.springframework.core.convert.support.GenericConversionService;
import cn.itnxd.springframework.core.convert.support.StringToNumberConverterFactory;
import org.junit.Test;

/**
 * @Author niuxudong
 * @Date 2023/4/9 18:29
 * @Version 1.0
 * @Description
 */
public class ApiTest {

    @Test
    public void testStringToIntegerConverter() throws Exception {
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Integer num = converter.convert("8888");

        System.out.println(num);
    }

    @Test
    public void testStringToNumberConverterFactory() throws Exception {
        StringToNumberConverterFactory converterFactory = new StringToNumberConverterFactory();

        Converter<String, Integer> stringToIntegerConverter = converterFactory.getConverter(Integer.class);
        Integer intNum = stringToIntegerConverter.convert("8888");

        System.out.println(intNum);

        Converter<String, Long> stringToLongConverter = converterFactory.getConverter(Long.class);
        Long longNum = stringToLongConverter.convert("8888");

        System.out.println(longNum);
    }

    @Test
    public void testGenericConverter() throws Exception {
        StringToBooleanConverter converter = new StringToBooleanConverter();

        Boolean flag = (Boolean) converter.convert("true", String.class, Boolean.class);

        System.out.println(flag);
    }

    @Test
    public void testGenericConversionService() throws Exception {
        GenericConversionService conversionService = new GenericConversionService();
        // 向注册中心添加 converter
        conversionService.addConverter(new StringToIntegerConverter());

        Integer intNum = conversionService.convert("8888", Integer.class);
        Boolean canConvert = conversionService.canConvert(String.class, Integer.class);

        System.out.println(intNum + " " + canConvert);

        // 向注册中心添加 ConverterFactory
        conversionService.addConverterFactory(new StringToNumberConverterFactory());
        Boolean canConvert1 = conversionService.canConvert(String.class, Long.class);
        Long longNum = conversionService.convert("8888", Long.class);

        System.out.println(longNum + " " + canConvert1);

        // 向注册中心添加 GenericConverter
        conversionService.addConverter(new StringToBooleanConverter());
        Boolean canConvert2 = conversionService.canConvert(String.class, Boolean.class);
        Boolean flag = conversionService.convert("true", Boolean.class);

        System.out.println(flag + " " + canConvert2);
    }
}