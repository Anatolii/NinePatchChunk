package ua.anatolii.graphics.ninepatch.test.matchers;

import android.graphics.Bitmap;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.testng.internal.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Anatolii on 2015-10-25.
 */
public class BitmapMatcher extends BaseMatcher<Bitmap> {
	@Nullable
	private final Bitmap expectedBitmap;

	public BitmapMatcher(@Nullable Bitmap expectedBitmap) {
		this.expectedBitmap = expectedBitmap;
	}

	public static Matcher<? super Bitmap> sameBitmap(Bitmap bitmap) {
		return new BitmapMatcher(bitmap);
	}

	@Override
	public boolean matches(Object item) {
		if (item == null && expectedBitmap == null)
			return true;
		if (item == null && expectedBitmap != null)
			return false;
		if(expectedBitmap == null && item !=null)
			return false;
		if (!(item instanceof Bitmap))
			return false;
		Bitmap bitmap = (Bitmap) item;
		List<PixelDifference> differences = findDifferences(bitmap, expectedBitmap);
		return differences.size() == 0;
	}

	@Override
	public void describeTo(Description description) {
		description.appendValue(expectedBitmap);
	}

	@Override
	public void describeMismatch(Object item, Description description) {
		super.describeMismatch(item, description);
		if (!(item instanceof Bitmap)) {
			description.appendText("\nExpected Bitmap instance but ");
			super.describeMismatch(item, description);
		} else {
			Bitmap bitmap = (Bitmap) item;
			List<PixelDifference> differences = findDifferences(bitmap, expectedBitmap);
			description.appendText("\nImages are not fully identical. Differences are:");
			for (PixelDifference difference : differences) {
				description.appendText("\nPixel:\tx: ").appendValue(difference.x).appendText("\ty: ").appendValue(difference.y);
				description.appendText("\tExpected: ").appendValue(difference.expected);
				description.appendText("\tWas: ").appendValue(difference.was);
			}
		}
	}

	private static List<PixelDifference> findDifferences(Bitmap bitmap, Bitmap expectedBitmap) {
		List<PixelDifference> differences = new LinkedList<>();
		for (int x = 0; x < bitmap.getWidth(); x++) {
			for (int y = 0; y < bitmap.getHeight(); y++) {
				int expected = bitmap.getPixel(x, y);
				int was = expectedBitmap.getPixel(x, y);
				if (expected != was)
					differences.add(new PixelDifference(x, y, expected, was));
			}
		}
		return differences;
	}

	private static class PixelDifference {
		private final int x;
		private final int y;
		private final int expected;
		private final int was;

		public PixelDifference(int x, int y, int expected, int was) {
			this.x = x;
			this.y = y;
			this.expected = expected;
			this.was = was;
		}
	}
}
