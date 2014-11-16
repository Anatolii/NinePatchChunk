package ua.anatolii.graphics.ninepatch.test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;
import android.util.Pair;
import ua.anatolii.graphics.ninepatch.ImageLoadingResult;
import ua.anatolii.graphics.ninepatch.NinePatchChunk;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by Anatolii on 10/27/13.
 */
public class NinePatchChunkTest extends AndroidTestCase {


    public void testImageLoadingFromAssets() {
        Bitmap bitmap = getBitmap(R.drawable.test_image_1, "test_image_1");

        InputStream is = getInputStreamFromAssets("test_image_1.9.png");

        ImageLoadingResult loadingResult = NinePatchChunk.createChunkFromRawBitmap(getContext(), is);
        NinePatchChunk chunk = loadingResult.chunk;
        Bitmap testBitmap = loadingResult.bitmap;
        assertNotNull("Loaded test bitmap can't be considered as raw NinePatch bitmap", chunk);
        assertEquals("Test end system bitmaps have different density.", bitmap.getDensity(), testBitmap.getDensity());
        assertEquals("Test and system bitmaps have different sizes",
                new SizePair(bitmap.getWidth(), bitmap.getHeight()),
                new SizePair(testBitmap.getWidth(), testBitmap.getHeight()));
        assertTheSameBitmap(bitmap, testBitmap);

        byte[] expectedChunk = bitmap.getNinePatchChunk();
        byte[] createdChunk = chunk.toBytes();
        if (Arrays.equals(createdChunk, expectedChunk))
            return;
        assertEquals("Created chunk length differs from expected", expectedChunk.length, createdChunk.length);
        HashMap<Integer, Pair<Byte, Byte>> chunkDifferences = new HashMap<Integer, Pair<Byte, Byte>>(expectedChunk.length);
        for (int i = 0; i < expectedChunk.length; i++) {
            if (expectedChunk[i] != createdChunk[i])
                chunkDifferences.put(i, new Pair<Byte, Byte>(expectedChunk[i], createdChunk[i]));
        }
        assertTrue("Loaded chunk is not the same as created by system.\nDifferences are: " + getDifferencesString(chunkDifferences), chunkDifferences.size() == 0);
    }

    private void assertTheSameBitmap(Bitmap bitmap, Bitmap testBitmap) {
        ArrayList<Map.Entry<Pair<Integer, Integer>, Pair<Integer, Integer>>> differences = new ArrayList<Map.Entry<Pair<Integer, Integer>, Pair<Integer, Integer>>>();
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                int expected = bitmap.getPixel(x, y);
                int was = testBitmap.getPixel(x, y);
                if (expected != was)
                    differences.add(
                            new AbstractMap.SimpleEntry<Pair<Integer, Integer>, Pair<Integer, Integer>>(new Pair<Integer, Integer>(x, y), new Pair<Integer, Integer>(expected, was)));
            }
        }
        if (differences.size() > 0) {
            StringBuilder builder = new StringBuilder("Images are not fully identical. Differences are:");
            for (Map.Entry<Pair<Integer, Integer>, Pair<Integer, Integer>> entry : differences) {
                builder.append("\nPixel: ").append(entry.getKey().first).append("\t").append(entry.getKey().second);
                builder.append("\tExpected: ").append(entry.getValue().first);
                builder.append("\tWas: ").append(entry.getValue().second);
            }
            fail(builder.toString());
        }
    }

    public void testParseValidNinePatchChunk() {
        Bitmap bitmap = getBitmap(R.drawable.test_image_1, "test_image_1");
        assertNotNull("Loaded bitmap is not nine patch image", bitmap.getNinePatchChunk());
        NinePatchChunk.parse(bitmap.getNinePatchChunk());
    }

    private String getDifferencesString(Map<Integer, Pair<Byte, Byte>> chunkDifferences) {
        StringBuilder builder = new StringBuilder();
        Set<Integer> keys = chunkDifferences.keySet();
        Integer[] keysArray = keys.toArray(new Integer[keys.size()]);
        Arrays.sort(keysArray);
        for (Integer key : keysArray) {
            Pair<Byte, Byte> pair = chunkDifferences.get(key);
            builder.append("\nPosition: ").append(key).append("\t");
            builder.append("Expected: ").append(pair.first).append(" \t");
            builder.append("Was: ").append(pair.second).append("\t");
        }
        return builder.toString();
    }

    private Bitmap getBitmap(int resourceId, String resourceName) {
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), resourceId);
        if (bitmap == null) {
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
