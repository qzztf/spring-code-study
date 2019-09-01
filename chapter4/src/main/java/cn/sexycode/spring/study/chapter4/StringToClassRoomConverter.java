package cn.sexycode.spring.study.chapter4;

import org.springframework.core.convert.converter.Converter;

import java.util.Optional;

public class StringToClassRoomConverter implements Converter<String, ClassRoom> {
    @Override
    public ClassRoom convert(String source) {
        String[] strings = Optional.ofNullable(source).orElseGet(String::new).split(",");
        ClassRoom classRoom = new ClassRoom();
        classRoom.setName(strings[0]);
        classRoom.setSize(Integer.parseInt(strings[1]));
        return classRoom;
    }
}
