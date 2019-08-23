package cn.sexycode.spring.study.chapter4;

import java.beans.PropertyEditorSupport;
import java.util.Optional;

/**
 * 转换String -> ClassRoom;
 */
public class ClassRoomPropertyEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        String[] strings = Optional.ofNullable(text).orElseGet(String::new).split(",");
        ClassRoom classRoom = new ClassRoom();
        classRoom.setName(strings[0]);
        classRoom.setSize(Integer.parseInt(strings[1]));
        setValue(classRoom);
    }
}
