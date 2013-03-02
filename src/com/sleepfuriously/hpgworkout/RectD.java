/**
 * Just like RectF, except that it uses doubles instead
 * of floats.
 */
package com.sleepfuriously.hpgworkout;

import android.graphics.Rect;
import android.graphics.RectF;

public class RectD {

	public double left, right, top, bottom;


	//----------------------------------
	//	Constructors
	//----------------------------------

	RectD() {
		setEmpty();
	}

	RectD (int left, int top, int right, int bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	RectD (float left, float top, float right, float bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	RectD (double left, double top, double right, double bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	RectD (Rect rect) {
		set (rect);
	}
	RectD (RectF rect) {
		set (rect);
	}
	RectD (RectD rect) {
		set (rect);
	}

	//----------------------------------
	//	Methods
	//----------------------------------

	@Override
	public String toString() {
		return "RectD (" + left + ", " + top + ", " + right + ", " + bottom + ")";
	}

	public double width() {
		return right - left;
	}

	public double height() {
		return top - bottom;
	}

	public double centerX() {
		return (right - left) / 2d;
	}

	public double centerY() {
		return (top - bottom) / 2d;
	}

	public boolean isEmpty() {
		if ((left >= right) || (top >= bottom)) {
			return true;
		}
		return false;
	}

	public void offset (double dx, double dy) {
		left += dx;
		right += dx;
		top += dy;
		bottom += dy;
	}

	public void offsetTo (double newLeft, double newTop) {
		double width = width();
		double height = height();
		left = newLeft;
		right = left + width;
		top = newTop;
		bottom = top + height;
	}

	public void round (Rect dst) {
		dst.left = (int) (Math.round(left));
		dst.right = (int) (Math.round(right));
		dst.top = (int) (Math.round(top));
		dst.bottom = (int) (Math.round(bottom));
	}

	public void roundOut (Rect dst) {
		dst.left = (int) (Math.floor(left));
		dst.right = (int) (Math.ceil(right));
		dst.top = (int) (Math.ceil(top));
		dst.bottom = (int) (Math.floor(bottom));
	}

	public void set (Rect src) {
		left = src.left;
		right = src.right;
		top = src.top;
		bottom = src.bottom;
	}

	public void set (RectF src) {
		left = src.left;
		right = src.right;
		top = src.top;
		bottom = src.bottom;
	}

	public void set (RectD src) {
		left = src.left;
		right = src.right;
		top = src.top;
		bottom = src.bottom;
	}

	public void set (int left, int top, int right, int bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	public void set (float left, float top, float right, float bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	public void set (double left, double top, double right, double bottom) {
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}

	public void setEmpty() {
		left = right = top = bottom = 0d;
	}

}
