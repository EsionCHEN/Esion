package io.renren.common.utils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * xml解析顺序是否有序
 *
 * @author ASen
 */
public enum XmlSort {
  /** 有序 */
  SORT(LinkedHashMap.class, "有序"),
  /** 无序 */
  NO_SORT(HashMap.class, "无序");

  /** 创建的map字节码对象 */
  private final Class<?> mapClass;

  /** 顺序名称 */
  private final String message;

  XmlSort(Class<?> mapClass, String message) {
    this.mapClass = mapClass;
    this.message = message;
  }

  public Class<?> getMapClass() {
    return mapClass;
  }

  public String getMessage() {
    return message;
  }

  public static void main(String[] args) {
    String patternBegin = "<MD5>";
    String patternEnd = "^</MD5>";
    String str = "<12>asdadas1asx<MD5>EA2E7503581CD7D6A6A7E19572657F00</MD5>";
    Pattern r = Pattern.compile(patternBegin);
    Pattern d = Pattern.compile(patternEnd);
    Matcher matcher = r.matcher(str);
    Matcher matcher1 = d.matcher(str);
    System.out.println(matcher.matches());
    System.out.println(matcher1);
  }
}