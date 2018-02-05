package com.wadexi.annotation;

public class IdAlreadyUsedException extends Exception{
    private FactoryAnnotatedClass annotatedClass;


    public IdAlreadyUsedException() {
    }

    public IdAlreadyUsedException(FactoryAnnotatedClass annotatedClass) {
        this.annotatedClass = annotatedClass;
    }

    public FactoryAnnotatedClass getAnnotatedClass() {
        return annotatedClass;
    }
}
