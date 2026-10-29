package com.example.apros.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 「参照のみ」の管理者アカウントでも実行してよいハンドラーに付ける。
 * このアノテーションが付いていない /admin/** のハンドラーは、
 * 編集権限を持つ管理者アカウントだけが実行できる（付け忘れても安全側に倒れる）。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AdminViewAllowed {
}
