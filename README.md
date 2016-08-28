NinePatchChunk
==============

This is a simple Android library which allows you to create a chunk for NinePatchDrawable at runtime. So you are able to load 9.png images, for example, from assets of your application or from other source.<br>
The solution based on <b>[this](https://android.googlesource.com/platform/packages/apps/Gallery2/+/android-4.2.2_r1.2/src/com/android/gallery3d/ui/NinePatchChunk.java)</b> implementation from Android Open Source Project.<br>
Additional information about 9-patch chunk structure you can find <b>[here](https://android.googlesource.com/platform/frameworks/base/+/master/include/androidfw/ResourceTypes.h)</b>.

--------
Usage
-----

The main class of the library is NinePatchChunk.<br>
<p1>Global constants:<br></p1>
<code>public static final int NO_COLOR = 0x00000001;</code> - The 9 patch segment is not a solid color.<br>
<code>public static final int TRANSPARENT_COLOR = 0x00000000;</code> - The 9 patch segment is completely transparent.<br>

Firstly let's go through the fields of the class object you would use.

- <code>boolean wasSerialized</code> - The indicator if this chunk was serialized or not. I didn't saw images without this variable equals true. If you will - feel free to contact me. This value is <code>true</code> by default.<br>
- <code>ArrayList\<Div\> xDivs</code> - An array with stretchable areas goes on X axes. The 0 start pixel is pixel of actual bitmap without 9-patch 1-pixel border. The <code>Div</code> class will be described bellow. By default this value is null<br>
- <code>ArrayList\<Div\> yDivs</code> - The same as xDivs but for the Y axes.
- <code>Rect padding</code> - The paddings from the ends of bitmap which you set while creating regular 9-patch image by defining right and bottom borders in Android 9-patch editor.<br>
Let's put some example. Let's say that you have 7x7 9-patch image. and you want the content will be exactly in the middle of the image.<br>
So your final image size without 9-patch specific borders would be 5x5 and if you want the contend will be placed inside 3-d pixel area you should define paddings as <code>Rect(2,2,2,2)</code>
- <code>int colors[]</code> - The colors of arrays which represents areas crossing X and Y stretchable and unstretchable areas of 9-patch bitmap. For more information look on <code>struct Res_png_9patch</code> commentaries <b>[here](https://android.googlesource.com/platform/frameworks/base/+/master/include/androidfw/ResourceTypes.h)</b>.<br>
Long story short, if you have the same color in that area - the color for thar area will be that color. If color of that area is Transparent (has alpha 0) - the color should be <code>Color.TRANSPARENT</code>. If the area is filled with different colors - the array should content <code>NO_COLOR</code> value. See static methods description to see how easily properly create this array.

The class has only one object-dependent method:

- <code>byte[] toBytes()</code> - serializes your current chunk instance to a byte array so you can use it creating NinePatchDrawable object.


Also NinePatchChunk class has some static methods. Let's go through them.<br>
- <code>NinePatchChunk parse(byte[] data)</code> - Parses any serialized chunk into the object you can use or edit.<br>
- <code>NinePatchDrawable create9PatchDrawable(Resources resources, Bitmap bitmap, String srcName)</code> - Creates NinePatchDrawable right from raw Bitmap object. So resulting drawable will have width and height 2 pixels less if it is raw, not compiled 9-patch resource.<br>
- <code>NinePatchChunk createEmptyChunk() - Jut creates empty chunk object to you have ability to change it.
- <code>int[] createColorsArray(NinePatchChunk chunk, int bitmapWidth, int bitmapHeight)</code> - Creates a proper color array sized according to your X and Y divs.
- <code>void createColorsArrayAndSet(NinePatchChunk chunk, int bitmapWidth, int bitmapHeight)</code> - The same as previous but this method also sets created array to your chunk if it is not null.
- <code>boolean isRawNinePatchBitmap(Bitmap bitmap)</code> - This method checks if the bitmap is 9-patch image.

And finally, Div object description. This object is used to define stretchable areas:<br>

- <code>int start</code> - the starting pixel of stretchable area. The count starts from 0.
- <code>int stop</code> - the ending pixel of stretchable area. This pixel is right after the latest Color.BLACK pixel of the stretchable area.

--------
Building from sources
--------

NinePatchChunk is a library which delivers as open source project.<br>
As build tool it uses Gradle build system.

In order to build the library you need this environment installed:
- Java JDK 6
- Android SDK with build tools version 18.1.1

For success build this environment variables should be defined in your system:
- JAVA_HOME     - will point to your JDK location
- ANDROID_HOME  - will point to Android SDK location
 
For the next step go to command line and then go to location where NinePatchChunk library sources are stored<br>
<code>cd <path to the library>/NinePatchChunk</code> (where <i>gradlew</i> script file stored)<br>

From this point in order to get output files you just need to run this command from command line:<br>
<code>gradlew assemble</code> to build release and debug aar files<br>
<code>gradlew assembleDebug</code> to build debug aar file only<br>
<code>gradlew assembleRelease</code> to build release aar file only<br>

That's it. So now you can find the output files inside this folder:<br>
<code>NinePatchChunk\NinePatchChunkLib\build\libs</code> 

--------

This is it. The using is simple as that. See the java doc to learn more.<br>
If you have some additional question - feel free to contact me.<br><br>
Thanks!
