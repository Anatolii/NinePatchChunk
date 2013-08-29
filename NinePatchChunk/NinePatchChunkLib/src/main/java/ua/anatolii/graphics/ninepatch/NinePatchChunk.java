package ua.anatolii.graphics.ninepatch;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by Anatolii on 8/27/13.
 */
public class NinePatchChunk implements Externalizable{

	// The 9 patch segment is not a solid color.
	public static final int NO_COLOR = 0x00000001;
	// The 9 patch segment is completely transparent.
	public static final int TRANSPARENT_COLOR = 0x00000000;

	public boolean wasSerialized = true;
	public ArrayList<Div> xDivs;
	public ArrayList<Div> yDivs;
	public Rect padding = new Rect();
	public int colors[];

	public static NinePatchChunk parse(byte[] data) throws DivLengthException, ChunkNotSerializedException, BufferUnderflowException {
		ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());

		NinePatchChunk chunk = new NinePatchChunk();
		chunk.wasSerialized = byteBuffer.get() != 0;
		if (!chunk.wasSerialized)
			throw new ChunkNotSerializedException();//don't know how to handle

		byte divXCount = byteBuffer.get();
		checkDivCount(divXCount);
		byte divYCount = byteBuffer.get();
		checkDivCount(divYCount);

		chunk.colors = new int[byteBuffer.get()];

		// skip 8 bytes
		byteBuffer.getInt();
		byteBuffer.getInt();

		chunk.padding.left = byteBuffer.getInt();
		chunk.padding.right = byteBuffer.getInt();
		chunk.padding.top = byteBuffer.getInt();
		chunk.padding.bottom = byteBuffer.getInt();

		// skip 4 bytes
		byteBuffer.getInt();

		int xDivs = divXCount >> 1;
		chunk.xDivs = new ArrayList<Div>(xDivs);
		readDivs(xDivs, byteBuffer, chunk.xDivs);

		int yDivs = divYCount >> 1;
		chunk.yDivs = new ArrayList<Div>(yDivs);
		readDivs(yDivs, byteBuffer, chunk.yDivs);

		for (int i = 0; i < chunk.colors.length; i++)
			chunk.colors[i] = byteBuffer.getInt();

		return chunk;
	}

	public static NinePatchDrawable create9PatchDrawable(Resources resources, Bitmap bitmap, String srcName) {
		if (bitmap == null) return null;
		Bitmap outBitmap;
		NinePatchChunk chunk;
		if (bitmap.getWidth() < 3 || bitmap.getHeight() < 3) {
			outBitmap = bitmap;
			chunk = NinePatchChunk.createEmptyChunk();
		} else {
			outBitmap = Bitmap.createBitmap(bitmap, 1, 1, bitmap.getWidth() - 2, bitmap.getHeight() - 2);
			try {
				chunk = createChunk(bitmap);
			} catch (WrongPaddingException e) {
				e.printStackTrace();
				chunk = NinePatchChunk.createEmptyChunk();
			} catch (DivLengthException e) {
				e.printStackTrace();
				chunk = NinePatchChunk.createEmptyChunk();
			}
		}
		return new NinePatchDrawable(resources, outBitmap, chunk.toBytes(), chunk.padding, srcName);
	}

	public static NinePatchChunk createEmptyChunk() {
		NinePatchChunk out = new NinePatchChunk();
		out.colors = new int[0];
		out.padding = new Rect();
		out.yDivs = new ArrayList<Div>();
		out.xDivs = new ArrayList<Div>();
		return out;
	}

	public byte[] toBytes() {
		int capacity = 4 + (7 * 4) + xDivs.size() * 2 * 4 + yDivs.size() * 2 * 4 + colors.length * 4;
		ByteBuffer byteBuffer = ByteBuffer.allocate(capacity).order(ByteOrder.nativeOrder());
		byteBuffer.put(Integer.valueOf(1).byteValue());
		byteBuffer.put(Integer.valueOf(xDivs.size() * 2).byteValue());
		byteBuffer.put(Integer.valueOf(yDivs.size() * 2).byteValue());
		byteBuffer.put(Integer.valueOf(colors.length).byteValue());
		//Skip
		byteBuffer.putInt(0);
		byteBuffer.putInt(0);

		if (padding == null)
			padding = new Rect();
		byteBuffer.putInt(padding.left);
		byteBuffer.putInt(padding.right);
		byteBuffer.putInt(padding.top);
		byteBuffer.putInt(padding.bottom);

		//Skip
		byteBuffer.putInt(0);

		for (Div div : xDivs) {
			byteBuffer.putInt(div.start);
			byteBuffer.putInt(div.stop);
		}
		for (Div div : yDivs) {
			byteBuffer.putInt(div.start);
			byteBuffer.putInt(div.stop);
		}
		for (int color : colors)
			byteBuffer.putInt(color);


		return byteBuffer.array();

	}

	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		int length = input.readInt();
		byte[] bytes = new byte[length];
		input.read(bytes);
		try {
			NinePatchChunk patch = parse(bytes);
			this.wasSerialized = patch.wasSerialized;
			this.xDivs = patch.xDivs;
			this.yDivs = patch.yDivs;
			this.padding = patch.padding;
			this.colors = patch.colors;
		} catch (DivLengthException e) {
			//ignore
		} catch (ChunkNotSerializedException e) {
			//ignore
		}
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		byte[] bytes = toBytes();
		output.writeInt(bytes.length);
		output.write(bytes);
	}

	private static void readDivs(int divs, ByteBuffer byteBuffer, ArrayList<Div> divArrayList) {
		for (int i = 0; i < divs; i++) {
			Div div = new Div();
			div.start = byteBuffer.getInt();
			div.stop = byteBuffer.getInt();
			divArrayList.add(div);
		}
	}

	private static void checkDivCount(byte divCount) throws DivLengthException {
		if (divCount == 0 || (divCount & 1) == 0) {
			throw new DivLengthException("Div count should be aliquot 2 and more then 0, but was: " + divCount);
		}
	}

	private static NinePatchChunk createChunk(Bitmap bitmap) throws WrongPaddingException, DivLengthException {

		NinePatchChunk out = new NinePatchChunk();
		setupStretchableRegions(bitmap, out);
		setupPadding(bitmap, out);

		setupColors(bitmap, out);
		return out;
	}

	private static void setupColors(Bitmap bitmap, NinePatchChunk out) {
		ArrayList<Div> xRegions = getRegions(out.xDivs, bitmap.getWidth() - 2);
		ArrayList<Div> yRegions = getRegions(out.yDivs, bitmap.getHeight() - 2);
		out.colors = new int[xRegions.size() * yRegions.size()];

		int colorIndex = 0;
		for (Div yDiv : yRegions) {
			for (Div xDiv : xRegions) {
				int startX = xDiv.start + 1;
				int startY = yDiv.start + 1;
				if (hasSameColor(bitmap, startX, xDiv.stop + 1, startY, yDiv.stop + 1)) {
					out.colors[colorIndex] = bitmap.getPixel(startX, startY);
				} else {
					out.colors[colorIndex] = NO_COLOR;
				}
				colorIndex++;
			}
		}
	}

	private static boolean hasSameColor(Bitmap bitmap, int startX, int stopX, int startY, int stopY) {
		int color = bitmap.getPixel(startX, startY);
		for (int x = startX; x <= stopX; x++) {
			for (int y = startY; y <= stopY; y++) {
				if (color != bitmap.getPixel(x, y))
					return false;
			}
		}
		return true;
	}

	private static void setupPadding(Bitmap bitmap, NinePatchChunk out) throws WrongPaddingException {
		int maxXPixels = bitmap.getWidth() - 2;
		int maxYPixels = bitmap.getHeight() - 2;
		ArrayList<Div> xPaddings = getXDivs(bitmap, bitmap.getWidth() - 1);
		if (xPaddings.size() > 1)
			throw new WrongPaddingException("Raw padding is wrong. Should be only one horizontal padding region");
		ArrayList<Div> yPaddings = getYDivs(bitmap, bitmap.getHeight() - 1);
		if (yPaddings.size() > 1)
			throw new WrongPaddingException("Column padding is wrong. Should be only one vertical padding region");
		if (xPaddings.size() == 0) xPaddings.add(out.xDivs.get(0));
		if (yPaddings.size() == 0) yPaddings.add(out.yDivs.get(0));
		out.padding = new Rect();
		out.padding.left = xPaddings.get(0).start;
		out.padding.right = maxXPixels - xPaddings.get(0).stop;
		out.padding.top = yPaddings.get(0).start;
		out.padding.bottom = maxYPixels - yPaddings.get(0).stop;
	}

	private static void setupStretchableRegions(Bitmap bitmap, NinePatchChunk out) throws DivLengthException {
		out.xDivs = getXDivs(bitmap, 0);
		if (out.xDivs.size() == 0)
			throw new DivLengthException("must be at least one horizontal stretchable region");
		out.yDivs = getYDivs(bitmap, 0);
		if (out.yDivs.size() == 0)
			throw new DivLengthException("must be at least one vertical stretchable region");
	}

	private static ArrayList<Div> getRegions(ArrayList<Div> divs, int max) {
		ArrayList<Div> out = new ArrayList<Div>();
		if (divs.size() == 0) return out;
		for (int i = 0; i < divs.size(); i++) {
			Div div = divs.get(i);
			if (i == 0 && div.start != 0) {
				out.add(new Div(0, div.start - 1));
			}
			if (i > 0) {
				out.add(new Div(divs.get(i - 1).stop, div.start - 1));
			}
			out.add(new Div(div.start, div.stop - 1));
			if (i == divs.size() - 1 && div.stop < max) {
				out.add(new Div(div.stop, max - 1));
			}
		}
		return out;
	}

	private static ArrayList<Div> getYDivs(Bitmap bitmap, int column) {
		ArrayList<Div> yDivs = new ArrayList<Div>();
		Div tmpDiv = null;
		for (int i = 1; i < bitmap.getHeight(); i++) {
			tmpDiv = processChunk(bitmap.getPixel(column, i), tmpDiv, i - 1, yDivs);
		}
		return yDivs;
	}

	private static ArrayList<Div> getXDivs(Bitmap bitmap, int raw) {
		ArrayList<Div> xDivs = new ArrayList<Div>();
		Div tmpDiv = null;
		for (int i = 1; i < bitmap.getWidth(); i++) {
			tmpDiv = processChunk(bitmap.getPixel(i, raw), tmpDiv, i - 1, xDivs);
		}
		return xDivs;
	}

	private static Div processChunk(int pixel, Div tmpDiv, int position, ArrayList<Div> divs) {
		if (pixel == Color.BLACK) {
			if (tmpDiv == null) {
				tmpDiv = new Div();
				tmpDiv.start = position;
			}
		}
		if (pixel == Color.TRANSPARENT) {
			if (tmpDiv != null) {
				tmpDiv.stop = position;
				divs.add(tmpDiv);
				tmpDiv = null;
			}
		}
		return tmpDiv;
	}
}
