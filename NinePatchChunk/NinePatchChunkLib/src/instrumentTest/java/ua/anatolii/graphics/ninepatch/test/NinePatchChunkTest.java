package ua.anatolii.graphics.ninepatch.test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import ua.anatolii.graphics.ninepatch.NinePatchChunk;

/**
 * Created by Anatolii on 10/27/13.
 */
public class NinePatchChunkTest extends AndroidTestCase {

	public void testImageLoadingFromAssets(){
		Bitmap bitmap = getBitmap(R.drawable.test_image_1, "test_image_1");
		InputStream is = getInputStreamFromAssets("test_image_1.9.png");

		Bitmap testBitmap = BitmapFactory.decodeStream(is);
		NinePatchChunk chunk = NinePatchChunk.createChunkFromRawBitmap(testBitmap);
		assertTrue("Loaded chunk is not the same as created by system.", Arrays.equals(bitmap.getNinePatchChunk(), chunk.toBytes()));
	}

	public void testParseValidNinePatchChunk(){
		Bitmap bitmap = getBitmap(R.drawable.test_image_1, "test_image_1");
		assertNotNull("Loaded bitmap is not nine patch image",bitmap.getNinePatchChunk());
		NinePatchChunk.parse(bitmap.getNinePatchChunk());
	}

	private Bitmap getBitmap(int resourceId, String resourceName) {
		Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), resourceId);
		if(bitmap == null) {
			fail("Can't load example image from system resources. Image '" + resourceName + "' does not exists");
		}
		return bitmap;
	}

	private InputStream getInputStreamFromAssets(String filePath) {
		InputStream is = null;
		try {
			is = getContext().getResources().getAssets().open(filePath);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		if(is == null)
			fail("InputStream was not open from Assets for file: "+ filePath);
		return is;
	}
}
