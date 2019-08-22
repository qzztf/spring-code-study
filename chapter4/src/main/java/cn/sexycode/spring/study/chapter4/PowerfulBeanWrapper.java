package cn.sexycode.spring.study.chapter4;

import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PowerfulBeanWrapper extends BeanWrapperImpl {
    public PowerfulBeanWrapper(Object o) {
        super(o);
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] propertyDescriptors = super.getPropertyDescriptors();
        List<PropertyDescriptor> propertyDescriptorList = new ArrayList<>(Arrays.asList(propertyDescriptors));
        Arrays.stream(propertyDescriptors).forEach(propertyDescriptor -> {
            Object value = getPropertyValue(propertyDescriptor.getName());
            if (value != null && !(value instanceof Class) && !value.getClass().isPrimitive()) {
                propertyDescriptorList.addAll(Arrays.asList(new PowerfulBeanWrapper(value).getPropertyDescriptors()));
            }
        });
        return propertyDescriptorList.toArray(new PropertyDescriptor[0]);
    }
}
