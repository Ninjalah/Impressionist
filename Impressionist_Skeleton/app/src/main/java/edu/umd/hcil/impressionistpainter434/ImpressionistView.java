package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.VelocityTracker;
import android.widget.ImageView;

import java.text.MessageFormat;
import java.util.Random;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    private ImageView _imageView;
    private Bitmap _imageViewBitmap;

    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();

    private int _alpha = 150;
    private int _defaultRadius = 50;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = true;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private float _minBrushRadius = 5;
    private VelocityTracker velocityTracker = null;
    private int _brushRadius;
    private boolean _invert = false;

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        _brushRadius = _defaultRadius;

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    public Bitmap getBitmap() {
        return _offScreenBitmap;
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        //TODO
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setStyle(Paint.Style.FILL);
        _offScreenCanvas.drawRect(0, 0, this.getWidth(), this.getHeight(), p);
        invalidate();
    }

    public void invertBrush() {
        _invert = !_invert;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){

        //TODO
        //Basically, the way this works is to liste for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location
        int touchXCurr = (int) motionEvent.getX();
        int touchYCurr = (int) motionEvent.getY();
        _imageViewBitmap = _imageView.getDrawingCache();
        int pixel = _imageViewBitmap.getPixel(touchXCurr, touchYCurr);
        int index = motionEvent.getActionIndex();
        int pointerID = motionEvent.getPointerId(index);
        Random r = new Random();
        _brushRadius = _defaultRadius;

        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (_brushType == _brushType.Illuminati) {
                    if (_invert) {
                        int red = Color.green(pixel);
                        int blue = Color.red(pixel);
                        int green = Color.blue(pixel);

                        Log.d("Tag", "Inverting!");

                        _paint.setColor(Color.argb(_paint.getAlpha(), red, green, blue));

                    }
                    _paint.setStrokeWidth(10f);
                    _offScreenCanvas.drawLine(touchXCurr + _brushRadius, touchYCurr + _brushRadius, touchXCurr, touchYCurr - _brushRadius, _paint);
                    _offScreenCanvas.drawLine(touchXCurr, touchYCurr - _brushRadius, touchXCurr - _brushRadius, touchYCurr + _brushRadius, _paint);
                    _offScreenCanvas.drawLine(touchXCurr - _brushRadius, touchYCurr + _brushRadius, touchXCurr + _brushRadius, touchYCurr + _brushRadius, _paint);
                    _offScreenCanvas.drawCircle(touchXCurr, touchYCurr, 10, _paint);
                    _paint.setStrokeWidth(_brushRadius);
                    invalidate();
                }
                if(velocityTracker != null) {
                    velocityTracker.clear();
                } else {
                    velocityTracker = VelocityTracker.obtain();
                }

                velocityTracker.addMovement(motionEvent);
                break;
            case MotionEvent.ACTION_MOVE:
                int historySize = motionEvent.getHistorySize();

                _paint.setStrokeWidth(_brushRadius);
                _paint.setColor(pixel);

                if (_invert) {
                    int red = Color.green(pixel);
                    int blue = Color.red(pixel);
                    int green = Color.blue(pixel);

                    Log.d("Tag", "Inverting!");

                    _paint.setColor(Color.argb(_paint.getAlpha(), red, green, blue));

                }

                for (int i = 0; i < historySize; i++) {
                    float touchX = motionEvent.getHistoricalX(i);
                    float touchY = motionEvent.getHistoricalY(i);

                    if (_brushType == _brushType.Circle) {
                        velocityTracker.addMovement(motionEvent);
                        velocityTracker.computeCurrentVelocity(1000);

                        int xV = (int) velocityTracker.getXVelocity(pointerID);
                        int yV = (int) velocityTracker.getYVelocity(pointerID);
                        int vel = Math.max(xV,yV);

                        if (vel < 100) {
                            _brushRadius = 4;
                        } else if (vel >= 100 && vel < 200) {
                            _brushRadius = 7;
                        } else if (vel >= 250 && vel < 300) {
                            _brushRadius = 13;
                        } else if (vel >= 350 && vel < 400) {
                            _brushRadius = 16;
                        } else if (vel >= 450 && vel < 500) {
                            _brushRadius = 22;
                        } else if (vel > 500) {
                            _brushRadius = 28;
                        }

                        _offScreenCanvas.drawCircle(touchX, touchY, _brushRadius, _paint);
                    } else if (_brushType == _brushType.Square) {
                        velocityTracker.addMovement(motionEvent);
                        velocityTracker.computeCurrentVelocity(1000);

                        int xV = (int) velocityTracker.getXVelocity(pointerID);
                        int yV = (int) velocityTracker.getYVelocity(pointerID);
                        int vel = Math.max(xV,yV);

                        if (vel < 100) {
                            _brushRadius = 4;
                        } else if (vel >= 100 && vel < 200) {
                            _brushRadius = 7;
                        } else if (vel >= 250 && vel < 300) {
                            _brushRadius = 13;
                        } else if (vel >= 350 && vel < 400) {
                            _brushRadius = 16;
                        } else if (vel >= 450 && vel < 500) {
                            _brushRadius = 22;
                        } else if (vel > 500) {
                            _brushRadius = 28;
                        }

                        _offScreenCanvas.drawRect(touchX, touchY, touchX + _brushRadius, touchY + _brushRadius, _paint);
                    } else if (_brushType == _brushType.Illuminati) {
                        _paint.setStrokeWidth(10f);
                        _offScreenCanvas.drawLine(touchX + _brushRadius, touchY + _brushRadius, touchX, touchY - _brushRadius, _paint);
                        _offScreenCanvas.drawLine(touchX, touchY - _brushRadius, touchX - _brushRadius, touchY + _brushRadius, _paint);
                        _offScreenCanvas.drawLine(touchX - _brushRadius, touchY + _brushRadius, touchX + _brushRadius, touchY + _brushRadius, _paint);
                        _offScreenCanvas.drawCircle(touchX, touchY, 10, _paint);
                        _paint.setStrokeWidth(_brushRadius);
                    } else if (_brushType == _brushType.SprayPaint) {
                        for (int j = 0; j < _brushRadius; j++) {
                            _offScreenCanvas.drawCircle(touchX + r.nextInt(_brushRadius - 1) + 1, touchY + r.nextInt(_brushRadius - 1) + 1, 1, _paint);
                        }

                    }
                }

                invalidate();
        }


        return true;
    }


    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }
}

