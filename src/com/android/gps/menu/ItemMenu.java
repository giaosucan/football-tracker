package com.android.gps.menu;

/**
 * Menu Item, dislay text and image
 */

import android.graphics.Bitmap;

public class ItemMenu {
    Bitmap image;
    String title;

    public ItemMenu(Bitmap image, String title) {
	super();
	this.image = image;
	this.title = title;
    }

    public Bitmap getImage() {
	return image;
    }

    public void setImage(Bitmap image) {
	this.image = image;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

}
