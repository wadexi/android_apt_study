package com.wadexi.annotation;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import admin.example.com.annotation.Factory;

@AutoService(Processor.class)
public class FactoryProcessor extends AbstractProcessor {

    private Types typeUtils;//一个用来处理TypeMirror的工具类
    private Elements elementUtils; //一个用来处理Element的工具类
    private Filer filer;//正如这个类的名字所示，你可以使用这个类来创建文件
    /**
     * Messager为注解处理器提供了一种报告错误消息，警告信息和其他消息的方式 它不是注解处理器开发者的日志工具
     * Messager是用来给那些使用了你的注解处理器的第三方开发者显示信息的
     */
    private Messager messager;

    private Map<String,FactoryGroupedClasses> factoryClasses = new LinkedHashMap<String,FactoryGroupedClasses>();

    public FactoryProcessor() {
        super();
    }


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        System.out.println("扫描注解器");
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    /**
     * 如果你在process()中抛出了一个异常，那 jvm 就会崩溃
     * 注解处理器的使用者将会得到一个从 javac 给出的非常难懂的异常错误信息。
     * 因为它包含了注解处理器的堆栈信息。因此注解处理器提供了Messager类。
     * 它能打印漂亮的错误信息，而且你可以链接到引起这个错误的元素上。
     * 在现代的IDE中，第三方开发者可以点击错误信息，IDE会跳转到产生错误的代码行中，以便快速定位错误。
     * @param annotations
     * @param roundEnv
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("===========process=====================");
        /**
         * roundEnv.getElementsAnnotatedWith
         * 返回一个被@Factory注解的元素列表,你可能注意到我避免说“返回一个被@Factory注解的类列表”。因为它的确是返回了一个Element列表。记住：Element可以是类，方法，变量等。
         */
        for (Element annotatedElement: roundEnv.getElementsAnnotatedWith(Factory.class)){//
            /**
             *  检查这个元素是否是一个类
             *  为什么需要这样做呢？因为我们要确保只有class类型的元素被我们的处理器处理
             *
             *  类是一种TypeElement元素。那我们为什么不使用if (! (annotatedElement instanceof TypeElement))来检查呢？
             *  这是错误的判断，因为接口也是一种TypeElement类型。
             *  所以在注解处理器中，你应该避免使用instanceof，
             *  应该用ElementKind或者配合TypeMirror使用TypeKind。
             */

            if(annotatedElement.getKind() != ElementKind.CLASS){
                error(annotatedElement,"Only classes can be annotated with @%s",Factory.class.getSimpleName());
                /**
                 * 为了能够获取Messager显示的信息，非常重要的是注解处理器必须不崩溃地完成运行。
                 * 这就是我们在调用error()后执行return true的原因。
                 * 如果我们在这里没有返回的话，process()就会继续运行，
                 * 因为messager.printMessage( Diagnostic.Kind.ERROR)并不会终止进程。
                 * 如果我们没有在打印完错误信息后返回的话，
                 * 我们就可能会运行到一个空指针异常等等。
                 * 就像前面所说的，如果我们继续运行process()，一旦有处理的异常在process()中被抛出，
                 * javac 就会打印注解处理器的空指针异常堆栈信息，而不是Messager显示的信息。
                 */
                return true;//exit processing
            }
            /**
             * 接下来我们要检查被注解的类至少有一个公有构造函数，不是抽象类，继承了特定的类，以及是一个public类：
             */
            TypeElement typeElement = (TypeElement) annotatedElement;
            try {
                FactoryAnnotatedClass annotatedClass = new FactoryAnnotatedClass(typeElement);//throws IllegalArgumentException
                if(!isValidClass(annotatedClass)){
                    return true;// Error message printed, exit processing
                }
                // Everything is fine, so try to add
                FactoryGroupedClasses factoryClass = factoryClasses.get(annotatedClass.getQualifiedFactoryGroupName());
                if(factoryClass == null){
                    String qualifiedGroupName = annotatedClass.getQualifiedFactoryGroupName();
                    factoryClass = new FactoryGroupedClasses(qualifiedGroupName);
                    factoryClasses.put(qualifiedGroupName,factoryClass);
                }
                factoryClass.add(annotatedClass);

            }catch (IllegalArgumentException e){
                // @Factory.id() is empty
                error(typeElement,e.getMessage());
                return true;
            } catch (IdAlreadyUsedException e) {
                e.printStackTrace();
                FactoryAnnotatedClass existing = e.getAnnotatedClass();
                //already existing
                error(annotatedElement,"Conflict : the class %s is annotated with @%s with id = %s bu %s already uses the same id",
                        typeElement.getQualifiedName().toString(),
                        Factory.class.getSimpleName(),
                        existing.getTypeElement().getQualifiedName().toString());
                return true;
            }

        }

        //我们已经收集了所有被@Factory注解的类的信息，这些信息以FactoryAnnotatedClass的形式保存在FactoryGroupedClass中。

        try{
            for (FactoryGroupedClasses factoryClass: factoryClasses.values()){
                factoryClass.generateCode(elementUtils,filer);
            }
            // Clear to fix the problem
            factoryClasses.clear();

        } catch (ClassNotFoundException | IOException e) {
            error(null,"生成代码异常：" + e.getMessage());
        }
        return false;
    }

    /**
     * 1.只有类能够被@Factory注解，因为接口和虚类是不能通过new操作符实例化的。
     * 2.被@Factory注解的类必须提供一个默认的无参构造函数。否则，我们不能实例化一个对象。
     * 3.被@Factory注解的类必须直接继承或者间接继承type指定的类型。（或者实现它，如果type指定的是一个接口）
     * 4.被@Factory注解的类中，具有相同的type类型的话，这些类就会被组织起来生成一个工厂类。工厂类以Factory作为后缀，例如：type=Meal.class将会生成MealFactory类。
     * 5.id的值只能是字符串，且在它的type组中必须是唯一的
     * @param item
     * @return
     */
    private boolean isValidClass(FactoryAnnotatedClass item){

        TypeElement classElement = item.getTypeElement();
        if(!classElement.getModifiers().contains(Modifier.PUBLIC)){
            error(classElement,"the class %s is not public",classElement.getQualifiedName().toString());
            return false;
        }

        // Check if it's an abstract class
        if(classElement.getModifiers().contains(Modifier.ABSTRACT)){
            error(classElement,"the class %s is abstract,you can not annotate abstract classes with @%s",classElement.getQualifiedName().toString(),Factory.class.getSimpleName());
            return false;
        }

        // Check inheritance: Class must be childclass as specified in
        // @Factory.type();
        //todo 这里的获取的是父类吗
        // 不是，是注解属性type 指向的class
        TypeElement superClassElement = elementUtils.getTypeElement(item.getQualifiedFactoryGroupName());
        if(superClassElement.getKind() == ElementKind.INTERFACE){

            if(!classElement.getInterfaces().contains(superClassElement.asType())){
                error(classElement,"the class %s annotated with @%s must implement the interface %s",
                        classElement.getQualifiedName().toString(),
                        Factory.class.getSimpleName(),
                        item.getQualifiedFactoryGroupName());
                return false;
            }

        }else {
            TypeElement currentClass = classElement;
            while (true){
                TypeMirror superClassType = currentClass.getSuperclass();
                // Basis class (java.lang.Object) reached, so exit
                if(superClassType.getKind() == TypeKind.NONE){
                    error(classElement,"the class %s annotated with %s must inherit from %s",
                            classElement.getQualifiedName().toString(),
                            Factory.class.getSimpleName(),
                            item.getSimpleFactoryGroupName());
                    return false;
                }
                if(superClassType.toString().equals(item.getQualifiedFactoryGroupName())){
                    // Required super class found
                    break;
                }

                // Moving up in inheritance tree
                currentClass = (TypeElement) typeUtils
                        .asElement(superClassType);
            }
        }

        // Check if an empty public constructor is given
        /**
         * getEnclosedElements : Returns the fields, methods, constructors, and member types
         *        that are directly declared in this class or interface.
         */
        for (Element enclosed : classElement.getEnclosedElements()){

            if(enclosed.getKind() == ElementKind.CONSTRUCTOR){
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if(constructorElement.getParameters().size() == 0
                        && constructorElement.getModifiers().contains(Modifier.PUBLIC)){
                    // Found an empty constructor
                    return true;
                }
            }
        }
        //no empty constructor found
        error(classElement,"the class %s must provide an public empty default constructor",classElement.getQualifiedName().toString());

        return false;
    }


    @Override
    public Set<String> getSupportedOptions() {
        System.out.println("===========getSupportedOptions=====================");
        return super.getSupportedOptions();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        System.out.println("===========getSupportedAnnotationTypes=====================");
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(Factory.class.getCanonicalName());//getCanonicalName: 返回Java语言规范定义的基础类的规范名称
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        System.out.println("===========getSupportedSourceVersion=====================");
        return SourceVersion.latestSupported();
    }



    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        System.out.println("===========getCompletions=====================");
        return super.getCompletions(element, annotation, member, userText);
    }

    @Override
    protected synchronized boolean isInitialized() {
        System.out.println("===========isInitialized=====================");
        return super.isInitialized();
    }


    /**
     * 打印错误信息
     * @param element
     * @param msg
     * @param args
     */
    public void error(Element element,String msg,Object... args){
        messager.printMessage(Diagnostic.Kind.ERROR,String.format(msg,args),element);
    }
}
