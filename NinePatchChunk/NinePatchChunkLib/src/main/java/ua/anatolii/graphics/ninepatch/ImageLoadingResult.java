package ua.anatolii.graphics.ninepatch;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;

/**
 * Created by Anatolii on 11/2/13.
 */
public class ImageLoadingResult {

    public final Bitmap bitmap;

    public final NinePatchChunk chunk;

    public ImageLoadingResult(Bitmap bitmap, NinePatchChunk chunk) {
        this.bitmap = bitmap;
        this.chunk = chunk;
    }

    public NinePatchDrawable getNinePatchDrawable(Resources resources, String strName){
        if(bitmap == null)
            return null;
        if(chunk == null)
            return new NinePatchDrawable(resources, bitmap, null, new Rect(), strName);
        return new NinePatchDrawable(resources, bitmap, chunk.toBytes(), chunk.padding, strName);
    }
}
