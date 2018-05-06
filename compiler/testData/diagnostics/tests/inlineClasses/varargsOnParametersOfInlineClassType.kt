// !LANGUAGE: +InlineClasses
// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_VARIABLE, -UNUSED_ANONYMOUS_PARAMETER

inline class Foo(val x: Int)

fun f1(<!VARARG_ON_INLINE_CLASS_TYPE!>vararg a: Foo<!>) {}
fun f2(<!VARARG_ON_INLINE_CLASS_TYPE!>vararg a: Foo?<!>) {}

class A {
    fun f3(a0: Int, <!VARARG_ON_INLINE_CLASS_TYPE!>vararg a1: Foo<!>) {
        fun f4(<!VARARG_ON_INLINE_CLASS_TYPE!>vararg a: Foo<!>) {}

        val g = fun (<!USELESS_VARARG_ON_PARAMETER, VARARG_ON_INLINE_CLASS_TYPE!>vararg v: Foo<!>) {}
    }
}

class B(<!VARARG_ON_INLINE_CLASS_TYPE!>vararg val s: Foo<!>) {
    constructor(a: Int, <!VARARG_ON_INLINE_CLASS_TYPE!>vararg s: Foo<!>) : this(*s)
}

annotation class Ann(<!VARARG_ON_INLINE_CLASS_TYPE!>vararg val f: <!INVALID_TYPE_OF_ANNOTATION_MEMBER!>Foo<!><!>)