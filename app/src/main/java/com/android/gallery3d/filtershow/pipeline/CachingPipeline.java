/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gallery3d.filtershow.pipeline;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.android.gallery3d.filtershow.cache.BitmapCache;
import com.android.gallery3d.filtershow.cache.ImageLoader;
import com.android.gallery3d.filtershow.filters.FilterRepresentation;
import com.android.gallery3d.filtershow.filters.FiltersManager;
import com.android.gallery3d.filtershow.imageshow.GeometryMathUtils;
import com.android.gallery3d.filtershow.imageshow.PrimaryImage;

import java.util.Vector;

public class CachingPipeline implements PipelineInterface {
    private static final String LOGTAG = "CachingPipeline";
    private boolean DEBUG = false;

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

    private FiltersManager mFiltersManager = null;
    private volatile Bitmap mOriginalBitmap = null;
    private volatile Bitmap mResizedOriginalBitmap = null;

    private FilterEnvironment mEnvironment = new FilterEnvironment();
    private CacheProcessing mCachedProcessing = new CacheProcessing();

    private volatile int mWidth = 0;
    private volatile int mHeight = 0;

    private volatile float mPreviewScaleFactor = 1.0f;
    private volatile float mHighResPreviewScaleFactor = 1.0f;
    private volatile String mName = "";

    public CachingPipeline(FiltersManager filtersManager, String name) {
        mFiltersManager = filtersManager;
        mName = name;
    }

    public void stop() {
        mEnvironment.setStop(true);
    }

    public synchronized void reset() {
        synchronized (CachingPipeline.class) {
            mOriginalBitmap = null; // just a reference to the bitmap in ImageLoader
            if (mResizedOriginalBitmap != null) {
                mResizedOriginalBitmap.recycle();
                mResizedOriginalBitmap = null;
            }

            mPreviewScaleFactor = 1.0f;
            mHighResPreviewScaleFactor = 1.0f;

            mWidth = 0;
            mHeight = 0;
        }
    }

    private String getType(RenderingRequest request) {
        if (request.getType() == RenderingRequest.ICON_RENDERING) {
            return "ICON_RENDERING";
        }
        if (request.getType() == RenderingRequest.FILTERS_RENDERING) {
            return "FILTERS_RENDERING";
        }
        if (request.getType() == RenderingRequest.FULL_RENDERING) {
            return "FULL_RENDERING";
        }
        if (request.getType() == RenderingRequest.GEOMETRY_RENDERING) {
            return "GEOMETRY_RENDERING";
        }
        if (request.getType() == RenderingRequest.PARTIAL_RENDERING) {
            return "PARTIAL_RENDERING";
        }
        if (request.getType() == RenderingRequest.HIGHRES_RENDERING) {
            return "HIGHRES_RENDERING";
        }
        return "UNKNOWN TYPE!";
    }

    private void setupEnvironment(ImagePreset preset, boolean highResPreview) {
        mEnvironment.setPipeline(this);
        mEnvironment.setFiltersManager(mFiltersManager);
        mEnvironment.setBitmapCache(PrimaryImage.getImage().getBitmapCache());
        if (highResPreview) {
            mEnvironment.setScaleFactor(mHighResPreviewScaleFactor);
        } else {
            mEnvironment.setScaleFactor(mPreviewScaleFactor);
        }
        mEnvironment.setQuality(FilterEnvironment.QUALITY_PREVIEW);
        mEnvironment.setImagePreset(preset);
        mEnvironment.setStop(false);
    }

    public void setOriginal(Bitmap bitmap) {
        mOriginalBitmap = bitmap;
        Log.v(LOGTAG,"setOriginal, size " + bitmap.getWidth() + " x " + bitmap.getHeight());
        ImagePreset preset = PrimaryImage.getImage().getPreset();
        setupEnvironment(preset, false);
        updateOriginalAllocation(preset);
    }

    private synchronized boolean updateOriginalAllocation(ImagePreset preset) {
        if (preset == null) {
            return false;
        }
        Bitmap originalBitmap = mOriginalBitmap;

        if (originalBitmap == null) {
            return false;
        }

        mResizedOriginalBitmap = preset.applyGeometry(originalBitmap, mEnvironment);

        return true;
    }

    public void renderHighres(RenderingRequest request) {
        synchronized (CachingPipeline.class) {
            ImagePreset preset = request.getImagePreset();
            setupEnvironment(preset, false);
            Bitmap bitmap = PrimaryImage.getImage().getOriginalBitmapHighres();
            if (bitmap == null) {
                return;
            }
            bitmap = mEnvironment.getBitmapCopy(bitmap, BitmapCache.HIGHRES);
            bitmap = preset.applyGeometry(bitmap, mEnvironment);

            mEnvironment.setQuality(FilterEnvironment.QUALITY_PREVIEW);
            Bitmap bmp = preset.apply(bitmap, mEnvironment);
            if (!mEnvironment.needsStop()) {
                request.setBitmap(bmp);
            } else {
                mEnvironment.cache(bmp);
            }
            mFiltersManager.freeFilterResources(preset);
        }
    }

    public void renderGeometry(RenderingRequest request) {
        synchronized (CachingPipeline.class) {
            ImagePreset preset = request.getImagePreset();
            setupEnvironment(preset, false);
            Bitmap bitmap = PrimaryImage.getImage().getOriginalBitmapHighres();
            if (bitmap == null) {
                return;
            }
            bitmap = mEnvironment.getBitmapCopy(bitmap, BitmapCache.GEOMETRY);
            bitmap = preset.applyGeometry(bitmap, mEnvironment);
            if (!mEnvironment.needsStop()) {
                request.setBitmap(bitmap);
            } else {
                mEnvironment.cache(bitmap);
            }
            mFiltersManager.freeFilterResources(preset);
        }
    }

    public void renderFilters(RenderingRequest request) {
        synchronized (CachingPipeline.class) {
            ImagePreset preset = request.getImagePreset();
            setupEnvironment(preset, false);
            Bitmap bitmap = PrimaryImage.getImage().getOriginalBitmapHighres();
            if (bitmap == null) {
                return;
            }
            bitmap = mEnvironment.getBitmapCopy(bitmap, BitmapCache.FILTERS);
            bitmap = preset.apply(bitmap, mEnvironment);
            if (!mEnvironment.needsStop()) {
                request.setBitmap(bitmap);
            } else {
                mEnvironment.cache(bitmap);
            }
            mFiltersManager.freeFilterResources(preset);
        }
    }

    public synchronized void render(RenderingRequest request) {
        // TODO: cleanup/remove GEOMETRY / FILTERS paths
        synchronized (CachingPipeline.class) {
            if ((request.getType() != RenderingRequest.PARTIAL_RENDERING
                  && request.getType() != RenderingRequest.ICON_RENDERING
                    && request.getBitmap() == null)
                    || request.getImagePreset() == null) {
                return;
            }

            if (DEBUG) {
                Log.v(LOGTAG, "render image of type " + getType(request));
            }

            Bitmap bitmap = request.getBitmap();
            ImagePreset preset = request.getImagePreset();
            setupEnvironment(preset, true);
            mFiltersManager.freeFilterResources(preset);

            if (request.getType() == RenderingRequest.PARTIAL_RENDERING) {
                PrimaryImage primary = PrimaryImage.getImage();
                bitmap = ImageLoader.getScaleOneImageForPreset(primary.getActivity(),
                        mEnvironment.getBimapCache(),
                        primary.getUri(), request.getBounds(),
                        request.getDestination());
                if (bitmap == null) {
                    Log.w(LOGTAG, "could not get bitmap for: " + getType(request));
                    return;
                }
            }

            if (request.getType() == RenderingRequest.FULL_RENDERING
                    || request.getType() == RenderingRequest.GEOMETRY_RENDERING
                    || request.getType() == RenderingRequest.FILTERS_RENDERING) {
                updateOriginalAllocation(preset);
            }

            if (DEBUG && bitmap != null) {
                Log.v(LOGTAG, "after update, req bitmap (" + bitmap.getWidth() + "x"
                        + bitmap.getHeight() + " ? resizeOriginal ("
                        + mResizedOriginalBitmap.getWidth() + "x"
                        + mResizedOriginalBitmap.getHeight());
            }

            if (request.getType() == RenderingRequest.FULL_RENDERING
                    || request.getType() == RenderingRequest.GEOMETRY_RENDERING) {
                bitmap = mResizedOriginalBitmap;
            } else if (request.getType() == RenderingRequest.FILTERS_RENDERING) {
                bitmap = mOriginalBitmap;
            }

            if (request.getType() == RenderingRequest.FULL_RENDERING
                    || request.getType() == RenderingRequest.FILTERS_RENDERING
                    || request.getType() == RenderingRequest.ICON_RENDERING
                    || request.getType() == RenderingRequest.PARTIAL_RENDERING
                    || request.getType() == RenderingRequest.STYLE_ICON_RENDERING) {

                if (request.getType() == RenderingRequest.ICON_RENDERING) {
                    mEnvironment.setQuality(FilterEnvironment.QUALITY_ICON);
                } else {
                    mEnvironment.setQuality(FilterEnvironment.QUALITY_PREVIEW);
                }

                if (request.getType() == RenderingRequest.ICON_RENDERING) {
                    Rect iconBounds = request.getIconBounds();
                    Bitmap source = PrimaryImage.getImage().getThumbnailBitmap();
                    if (iconBounds.width() > source.getWidth() * 2) {
                        source = PrimaryImage.getImage().getLargeThumbnailBitmap();
                    }
                    if (iconBounds != null) {
                        bitmap = mEnvironment.getBitmap(iconBounds.width(),
                                iconBounds.height(), BitmapCache.ICON);
                        Canvas canvas = new Canvas(bitmap);
                        Matrix m = new Matrix();
                        float minSize = Math.min(source.getWidth(), source.getHeight());
                        float maxSize = Math.max(iconBounds.width(), iconBounds.height());
                        float scale = maxSize / minSize;
                        m.setScale(scale, scale);
                        float dx = (iconBounds.width() - (source.getWidth() * scale))/2.0f;
                        float dy = (iconBounds.height() - (source.getHeight() * scale))/2.0f;
                        m.postTranslate(dx, dy);
                        canvas.drawBitmap(source, m, new Paint(Paint.FILTER_BITMAP_FLAG));
                    } else {
                        bitmap = mEnvironment.getBitmapCopy(source, BitmapCache.ICON);
                    }
                }
                Bitmap bmp = preset.apply(bitmap, mEnvironment);
                if (!mEnvironment.needsStop()) {
                    request.setBitmap(bmp);
                }
                mFiltersManager.freeFilterResources(preset);
            }
        }
    }

    public synchronized Bitmap renderFinalImage(Bitmap bitmap, ImagePreset preset) {
        synchronized (CachingPipeline.class) {
            setupEnvironment(preset, false);
            mEnvironment.setQuality(FilterEnvironment.QUALITY_FINAL);
            mEnvironment.setScaleFactor(1.0f);
            mFiltersManager.freeFilterResources(preset);
            bitmap = preset.applyGeometry(bitmap, mEnvironment);
            bitmap = preset.apply(bitmap, mEnvironment);
            return bitmap;
        }
    }

    public Bitmap renderGeometryIcon(Bitmap bitmap, ImagePreset preset) {
        return GeometryMathUtils.applyGeometryRepresentations(preset.getGeometryFilters(), bitmap);
    }

    public void compute(SharedBuffer buffer, ImagePreset preset, int type) {
        setupEnvironment(preset, false);
        Vector<FilterRepresentation> filters = preset.getFilters();
        Bitmap result = mCachedProcessing.process(mOriginalBitmap, filters, mEnvironment);
        buffer.setProducer(result);
        mEnvironment.cache(result);
    }

    public boolean needsRepaint() {
        SharedBuffer buffer = PrimaryImage.getImage().getPreviewBuffer();
        return buffer.checkRepaintNeeded();
    }

    public void setPreviewScaleFactor(float previewScaleFactor) {
        mPreviewScaleFactor = previewScaleFactor;
    }

    public void setHighResPreviewScaleFactor(float highResPreviewScaleFactor) {
        mHighResPreviewScaleFactor = highResPreviewScaleFactor;
    }

    public synchronized boolean isInitialized() {
        return mOriginalBitmap != null;
    }

    public String getName() {
        return mName;
    }
}
