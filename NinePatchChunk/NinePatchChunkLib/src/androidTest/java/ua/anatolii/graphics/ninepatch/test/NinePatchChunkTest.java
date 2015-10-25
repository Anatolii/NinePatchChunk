package ua.anatolii.graphics.ninepatch.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.util.Pair;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;

import de.lemona.android.testng.AndroidTestNGSupport;
import ua.anatolii.graphics.ninepatch.ImageLoadingResult;
import ua.anatolii.graphics.ninepatch.NinePatchChunk;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.testng.Assert.fail;
import static ua.anatolii.graphics.ninepatch.test.matchers.BitmapMatcher.sameBitmap;
import static ua.anatolii.graphics.ninepatch.test.matchers.ByteArrayMatcher.sameArray;

/**
 * Created by Anatolii on 10/27/13.
 */
public class NinePatchChunkTest {

	@DataProvider(name = "drawablesToTest", parallel = false)
	public Object[][] createDrawablesToTest() {
		return new Object[][]{
				{R.drawable.image_container, "image_container.9.png"},
				{R.drawable.lib_bg, "lib_bg.9.png"},
				{R.drawable.logo_overlay, "logo_overlay.9.png"},
				{R.drawable.menu_dropdown_panel_styled, "menu_dropdown_panel_styled.9.png"},
				{R.drawable.progress_primary_styled, "progress_primary_styled.9.png"},
				{R.drawable.spinner_ab_default_styled, "spinner_ab_default_styled.9.png"},
				{R.drawable.tab_selected_pressed_styled, "tab_selected_pressed_styled.9.png"},
				{R.drawable.tab_unselected_focused_styled, "tab_unselected_focused_styled.9.png"},
				{R.drawable.test_image_1, "test_image_1.9.png"},
				{R.drawable.tile, "tile.9.png"},
				{R.drawable.widget_bg_normal, "widget_bg_normal.9.png"},
				{R.drawable.widget_bg_pressed, "widget_bg_pressed.9.png"},
				{R.drawable.widget_bg_selected, "widget_bg_selected.9.png"},
		};
	}


	@Test(dataProvider = "drawablesToTest")
	public void testImageLoadingFromAssets(@DrawableRes Integer drawableResId, String drawableName) {
		Context context = AndroidTestNGSupport.getContext();
		assertThat("Context is null", context, notNullValue());

		Bitmap bitmap = getBitmap(drawableResId, drawableName, context);
		InputStream is = getInputStreamFromAssets(drawableName, context);

		ImageLoadingResult loadingResult = NinePatchChunk.createChunkFromRawBitmap(context, is);
		NinePatchChunk chunk = loadingResult.chunk;
		Bitmap testBitmap = loadingResult.bitmap;
		assertThat("Loaded test bitmap can't be considered as raw NinePatch bitmap", chunk, notNullValue());
		assertThat("Test end system bitmaps have different density.", bitmap.getDensity(), equalTo(testBitmap.getDensity()));
		assertThat("Test and system bitmaps have different sizes",
				new SizePair(bitmap.getWidth(), bitmap.getHeight()),
				equalTo(new SizePair(testBitmap.getWidth(), testBitmap.getHeight())));
		assertThat("Images are not fully identical", testBitmap, sameBitmap(bitmap));
		byte[] expectedChunk = bitmap.getNinePatchChunk();
		byte[] createdChunk = chunk.toBytes();
		assertThat("Created chunk different from expected", createdChunk, sameArray(expectedChunk, 4, 8, 28));//See NinePatchChunk implementation why this bytes are skipped.
	}

	@Test(dataProvider = "drawablesToTest")
	public void parseValidNinePatchChunk(@DrawableRes Integer drawableResId, String drawableName) {
		Context context = AndroidTestNGSupport.getContext();
		assertThat("Context is null", context, notNullValue());
		Bitmap bitmap = getBitmap(drawableResId, drawableName, context);
		assertThat("Loaded bitmap is not nine patch image", bitmap.getNinePatchChunk(), notNullValue());
		assertThat("Parsed chunk is null", NinePatchChunk.parse(bitmap.getNinePatchChunk()), notNullValue());
	}

	private Bitmap getBitmap(int resourceId, String resourceName, Context context) {
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
		if (bitmap == null) {
			fail("Can't load example image from system resources. Image '" + resourceName + "' does not exists");
		}
		return bitmap;
	}

	private InputStream getInputStreamFromAssets(String filePath, Context context) {
		InputStream is = null;
		try {
			is = context.getResources().getAssets().open(filePath);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		if (is == null)
			fail("InputStream was not open from Assets for file: " + filePath);
		return is;
	}

	private static final class SizePair extends Pair<Integer, Integer> {

		public SizePair(Integer width, Integer height) {
			super(width, height);
		}

		@Override
		public String toString() {
			return new StringBuilder().append("Width: ").append(first)
					.append(" Height: ").append(second).toString();
		}
	}
}
