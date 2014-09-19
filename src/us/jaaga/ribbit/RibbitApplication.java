package us.jaaga.ribbit;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class RibbitApplication extends Application {

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Parse.initialize(this, "kX8uhOHvDghSimPKxZIRGMSpmnbZas1dKMMUemeo",
				"9KlA15dsnWCe38zPkC5H2B504XprscWbAQC25Ne1");
		ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();
	}

}
