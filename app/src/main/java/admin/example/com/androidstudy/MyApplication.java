package admin.example.com.androidstudy;

import android.support.multidex.MultiDexApplication;

/**
 * Created by ${wadexi} on 2018/2/2.
 *
 * extends MultiDexApplication -->
 * --> test resolve
 * com.android.builder.dexing.DexArchiveBuilderException: com.android.builder.dexing.DexArchiveBuilderException: Failed to process D:\softwarelocation\android\projects\AndroidStudy\app\build\intermediates\transforms\desugar\debug\22.jar
 * at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
 * at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
 * at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
 * at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
 */

public class MyApplication extends MultiDexApplication {

    public MyApplication() {
        super();
    }
}
