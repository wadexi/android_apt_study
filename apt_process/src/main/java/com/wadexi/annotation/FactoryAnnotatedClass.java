package com.wadexi.annotation;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

import admin.example.com.annotation.Factory;

public class FactoryAnnotatedClass {

    private TypeElement annotatedClassElement;//被注解的类元素
    private String qualifiedSuperClassName;//注解属性type  指向的class的全名
    private String simpleTypeName;//注解属性type  指向的class的简称
    private String id;//注解属性id 的值

    public FactoryAnnotatedClass(TypeElement classElement) throws IllegalArgumentException{
        this.annotatedClassElement = classElement;
        Factory annotation = classElement.getAnnotation(Factory.class);
        id = annotation.id();
        if("".equals(id)){
            throw new IllegalArgumentException(String.format("id() in @%s for class %s is null or empty! that is not allow",Factory.class.getSimpleName(),classElement.getQualifiedName().toString()));
        }

        try{
            /**
             * 这个类已经被编译过了：这种情况是第三方 .jar 包含已编译的被@Factory注解 .class 文件。
             * 这种情况下，我们可以像try 代码块中所示那样直接获取Class。
             * (译注：因为@Factory的@Retention为RetentionPolicy.CLASS，所有被编译过的代码也会保留@Factory的注解信息)
             */
            Class<?> clazz = annotation.type();
            qualifiedSuperClassName = clazz.getCanonicalName();
            simpleTypeName = clazz.getSimpleName();
        }catch (MirroredTypeException mte){
            /**
             * 这个类还没有被编译：这种情况是我们尝试编译被@Fractory注解的源代码。
             * 这种情况下，直接获取Class会抛出MirroredTypeException异常。
             * 幸运的是，MirroredTypeException包含一个TypeMirror，它表示我们未被编译类。
             * 因为我们知道它一定是一个Class类型（我们前面有检查过），
             * 所以我们可以将它转换为DeclaredType， 然后获取TypeElement来读取合法名称。
             */
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            qualifiedSuperClassName = classTypeElement.getQualifiedName().toString();
            simpleTypeName = classTypeElement.getSimpleName().toString();
        }
    }

    public  String getId(){
        return id;
    }

    public String getQualifiedFactoryGroupName() {
        return qualifiedSuperClassName;
    }

    public String getSimpleFactoryGroupName() {
        return simpleTypeName;
    }

    public TypeElement getTypeElement() {
        return annotatedClassElement;
    }


}
