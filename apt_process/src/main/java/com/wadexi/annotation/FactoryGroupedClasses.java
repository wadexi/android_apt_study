package com.wadexi.annotation;

//import com.squareup.javapoet.JavaFile;
//import com.squareup.javapoet.MethodSpec;
//import com.squareup.javapoet.TypeSpec;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.lang.model.element.Modifier;
import javax.xml.crypto.Data;

/**
 * 需要一个叫FactoryGroupedClasses的数据结构，用来简单的组合所有的FactoryAnnotatedClasses到一起。
 */
public class FactoryGroupedClasses {

    private static final String SUFFIX = "Factory";

    private String qualifiedClassName;

    private Map<String,FactoryAnnotatedClass> itemMap = new LinkedHashMap<String,FactoryAnnotatedClass>();

    public FactoryGroupedClasses(String qualifiedClassName){
        this.qualifiedClassName = qualifiedClassName;
    }

    public void add (FactoryAnnotatedClass toInsert) throws  IdAlreadyUsedException{
        FactoryAnnotatedClass existing = itemMap.get(toInsert.getId());
        if(existing != null){
            throw new IdAlreadyUsedException(existing);
        }
        itemMap.put(toInsert.getId(),toInsert);
    }

    public void generateCode(Elements elementUtils, Filer filer) throws IOException, ClassNotFoundException {
        /**
         * 写 java 文件跟写其他文件完全一样。我们可以使用Filer提供的一个Writer对象来操作。
         * 我们可以用字符串拼接的方法写入我们生成的代码。
         * 幸运的是，Square公司（因为提供了许多非常优秀的开源项目二非常有名）给我们提供了JavaWriter，(现在叫JavaPoet，javaWriter是前身)
         * 这是一个高级的生成Java代码的库
         */
        TypeElement superClassName = elementUtils.getTypeElement(qualifiedClassName);

        String factoryClassname = superClassName.getSimpleName() + SUFFIX;

//        JavaFileObject jfo = filer.createSourceFile(qualifiedClassName + SUFFIX);
//
//        Writer writer = jfo.openWriter();


//        addStatement() 负责分号和换行
//        beginControlFlow() + endControlFlow() 需要一起使用，提供换行符和缩进。
//        $L for Literals  Literals 直接写在输出代码中，没有转义
//        $S for Strings 当输出的代码包含字符串的时候, 可以使用 $S 表示一个 string
//        $T for Types 使用Java内置的类型会使代码比较容易理解。JavaPoet极大的支持这些类型，通过 $T 进行映射，会自动import声明。
//        $N for Names 使用 $N 可以引用另外一个通过名字生成的声明。

        System.out.println("================qualifiedClassName: " + qualifiedClassName);
        ClassName returnClass = ClassName.get(superClassName);

        //创建方法
        MethodSpec.Builder methodbuilder = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .returns(returnClass)
                .addParameter(String.class,"id")
                .beginControlFlow("if (id == null)")
                .addStatement("throw new IllegalArgumentException(\"id is null!\")")
                .endControlFlow();

        for (FactoryAnnotatedClass item: itemMap.values()){
            methodbuilder.beginControlFlow("if ($S.equals(id))",item.getId())
                    .addStatement("return new $N()",item.getTypeElement().getQualifiedName().toString())
                    .endControlFlow();
        }

        MethodSpec methodSpec = methodbuilder.addStatement("throw new IllegalArgumentException(\"Unknown id = \" + id)")
        .build();

        //创建类
        TypeSpec classFileName = TypeSpec.classBuilder(factoryClassname)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodSpec)
                .build();
        //创建文件
        PackageElement pkg = elementUtils.getPackageOf(superClassName);

        JavaFile javaFile;
        if(!pkg.isUnnamed()){
            String pkgName = pkg.getQualifiedName().toString();
            System.out.printf("包名：" + pkgName + "  类名：" + classFileName);
            javaFile = JavaFile.builder(pkgName,classFileName).build();
        }else {
            javaFile = JavaFile.builder("",classFileName).build();
            System.out.printf("包名：" + "" + "  类名：" + classFileName);
        }
        javaFile.writeTo(filer);


        // main method
//        MethodSpec main = MethodSpec.methodBuilder("main")
//                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//                .returns(void.class)
//                .addParameter(String[].class, "args")
//                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
//                .build();
//        // HelloWorld class
//        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
//                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
//                .addMethod(main)
//                .build();
//
//        try {
//            // build com.example.HelloWorld.java
//            JavaFile javaFile = JavaFile.builder("com.example", helloWorld)
//                    .addFileComment(" This codes are generated automatically. Do not modify!")
//                    .build();
//            // write to file
//            javaFile.writeTo(filer);



    }

}
