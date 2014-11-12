package com.teamtreehouse.ribbit;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class RibbitApplication extends Application {
	
	@Override
	public void onCreate() { 
		super.onCreate();
	    Parse.initialize(this, "dbrOyEHgX2GR3EPTIblrNNAd4ITXarc82shcXdlu", "6iyrGf1I8pXKRGlxMuL4NQQ4doZLrR5btJ2ZlvII");
	}
}
