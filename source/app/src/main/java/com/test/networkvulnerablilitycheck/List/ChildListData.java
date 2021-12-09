package com.test.networkvulnerablilitycheck.List;

import android.graphics.drawable.Drawable;

public class ChildListData {
    // ImageView에 상응
    public Drawable mChildItem;
    public String mChildText;

    public ChildListData(Drawable drawable, String string){
        mChildItem = drawable;
        mChildText = string;
    }

    public String getText() {
      return mChildText;
    }

}
