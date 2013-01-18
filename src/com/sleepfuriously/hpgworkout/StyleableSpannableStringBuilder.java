/**
 * An extension of SpannableStringBuilder, this
 * allows me to add styles as I append strings.
 * Seems kind of an obvious thing to do--weird
 * that the parent doesn't do this.
 *
 * Taken from a comment by a guy named Mike on
 * this webpage:
 * 		http://www.androidengineer.com/2010/08/easy-method-for-formatting-android.html
 */
package com.sleepfuriously.hpgworkout;

import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;

public class StyleableSpannableStringBuilder
					extends SpannableStringBuilder {

	/************************
	 * Appends the given text to whatever is already
	 * here, applying the specified style.
	 *
	 * @param text		The text to append.
	 * @param style		The style to apply to the new
	 * 					text.
	 *
	 * @return	The changed StyleableSpannableStringBuilder.
	 */
	public StyleableSpannableStringBuilder appendWithStyle(
					CharSequence text,
					CharacterStyle style) {
		super.append(text);
		int startPos = length() - text.length();
		setSpan(style, startPos, length(), 0);
		return this;
	} // appendWithStyle(text, style)


	/************************
	 * Convenience method to easily append text with
	 * a color style set to it.
	 *
	 * @param text		The text to append.
	 * @param color		The color to apply to the text.
	 * 					Warning, the COLOR, not the ResID
	 * 					that defines the color.
	 *
	 * @return	The changed StyleableSpannableStringBuilder.
	 */
	public StyleableSpannableStringBuilder appendWithForegroundColor(
				CharSequence text, int color) {
		return appendWithStyle(text, new ForegroundColorSpan(color));
	} // appendWithColor(text, color)

	/************************
	 * Same as appendWithForegroundColor(), except for
	 * the background.
	 *
	 * @see #appendWithForegroundColor(CharSequence, int)
	 *
	 */
	public StyleableSpannableStringBuilder appendWithBackgroundColor(
				CharSequence text, int color) {
		return appendWithStyle(text, new BackgroundColorSpan(color));
	} // appendWithColor(text, color)

}
