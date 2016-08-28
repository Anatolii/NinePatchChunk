package ua.anatolii.graphics.ninepatch.test.matchers;

import android.support.annotation.NonNull;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.testng.internal.Nullable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Anatolii on 2015-10-25.
 */
public class ByteArrayMatcher extends BaseMatcher<byte[]> {

	@Nullable
	private final byte[] expectedArray;
	@NonNull
	private final int[] excludePositionsFromCheck;

	public static Matcher<? super byte[]> sameArray(@Nullable byte[] createdChunk, @NonNull int... excludePositionsFromCheck) {
		return new ByteArrayMatcher(createdChunk, excludePositionsFromCheck);
	}

	public ByteArrayMatcher(byte[] expectedArray, @NonNull int... excludePositionsFromCheck) {
		this.expectedArray = expectedArray;
		Arrays.sort(excludePositionsFromCheck);
		this.excludePositionsFromCheck = excludePositionsFromCheck;
	}

	@Override
	public boolean matches(Object item) {
		if (item == null && expectedArray == null)
			return true;
		if (item != null && expectedArray == null)
			return false;
		if (item == null && expectedArray != null)
			return false;
		if (!item.getClass().isArray())
			return false;
		if (item.getClass().isInstance(expectedArray.getClass()))
			return false;
		byte[] array = (byte[]) item;
		if(array.length != expectedArray.length)
			return false;
		return getDifferences(array).isEmpty();
	}

	@Override
	public void describeTo(Description description) {
		description.appendValue(expectedArray);
	}

	@Override
	public void describeMismatch(Object item, Description description) {
		super.describeMismatch(item, description);
		if (item != null && expectedArray != null && item.getClass().isArray() && !item.getClass().isInstance(expectedArray.getClass())) {

			byte[] array = (byte[]) item;
			if (expectedArray.length != array.length) {
				description.appendText("\nArrays length does not match.");
				description.appendText(" Expected: ").appendValue(expectedArray.length);
				description.appendText(" But was: ").appendValue(array.length);
			} else {
				List<Difference> differences = getDifferences(array);
				description.appendText("\nGiven array is not the same as expected.");
				description.appendText("\nPositions excluded from comparison: " + Arrays.toString(excludePositionsFromCheck));
				description.appendText("\nDifferences are: (" + differences.size() + ")");
				description.appendText(getDifferencesString(differences));
			}

		}
	}

	@NonNull
	private List<Difference> getDifferences(byte[] array) {
		List<Difference> differences = new LinkedList<>();
		for (int i = 0; i < expectedArray.length; i++) {
			if (expectedArray[i] != array[i]) {
				boolean shouldExcludePosition = isPostitionShouldBeExcluded(i);
				if (!shouldExcludePosition)
					differences.add(new Difference(i, expectedArray[i], array[i]));
			}
		}
		return differences;
	}

	private boolean isPostitionShouldBeExcluded(int i) {
		for (int excludePosition : excludePositionsFromCheck) {
			if (excludePosition == i) {
				return true;
			}
		}
		return false;
	}

	private String getDifferencesString(List<Difference> differences) {
		StringBuilder builder = new StringBuilder();
		for (Difference difference : differences) {
			builder.append("\nPosition:\t").append(difference.position).append("\t");
			builder.append("Expected:\t").append(difference.expected).append(" \t");
			builder.append("Was:\t").append(difference.was).append("\t");
		}
		return builder.toString();
	}

	private static class Difference {
		public final int position;
		public final byte expected;
		public final byte was;

		private Difference(int position, byte expected, byte was) {
			this.position = position;
			this.expected = expected;
			this.was = was;
		}
	}
}
