package admin.example.com.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Factory {
    /**
     * 我们注解那些食物类
     * 使用type()表示这个类属于哪个工厂
     * 使用id()表示这个类的具体类型
     * */
    Class<?> type();

    String id();
}
