package com.osfans.trime.ime.symbol;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class SimpleKeyDao {

  @NonNull
  public static List<SimpleKeyBean> SimpleKeyboard(@NonNull String string) {
    String[] strings = string.split("\n+");
    List<SimpleKeyBean> list = new ArrayList<>();
    for (String str : strings) {
      if (str.length() < 1) continue;
      SimpleKeyBean keyBean = new SimpleKeyBean(str);
      list.add(keyBean);
    }
    return list;
  }

  @NonNull
  public static List<SimpleKeyBean> Single(@NonNull String string) {
    List<SimpleKeyBean> list = new ArrayList<>();

    char h = 0;

    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      if (c >= '\uD800' && c <= '\udbff') h = c;
      else if (c >= '\udc00' && c <= '\udfff')
        list.add(new SimpleKeyBean(String.valueOf(new char[] {h, c})));
      else list.add(new SimpleKeyBean(Character.toString(c)));
    }
    return list;
  }
}
