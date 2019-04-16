/****************************************************************************************
 * Copyright (c) 2016, 2017, 2019 Vincent Hiribarren                                    *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * Linking Beacon Simulator statically or dynamically with other modules is making      *
 * a combined work based on Beacon Simulator. Thus, the terms and conditions of         *
 * the GNU General Public License cover the whole combination.                          *
 *                                                                                      *
 * As a special exception, the copyright holders of Beacon Simulator give you           *
 * permission to combine Beacon Simulator program with free software programs           *
 * or libraries that are released under the GNU LGPL and with independent               *
 * modules that communicate with Beacon Simulator solely through the                    *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataGenerator and the                    *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataParser interfaces. You may           *
 * copy and distribute such a system following the terms of the GNU GPL for             *
 * Beacon Simulator and the licenses of the other code concerned, provided that         *
 * you include the source code of that other code when and as the GNU GPL               *
 * requires distribution of source code and provided that you do not modify the         *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataGenerator and the                    *
 * net.alea.beaconsimulator.bluetooth.AdvertiseDataParser interfaces.                   *
 *                                                                                      *
 * The intent of this license exception and interface is to allow Bluetooth low energy  *
 * closed or proprietary advertise data packet structures and contents to be sensibly   *
 * kept closed, while ensuring the GPL is applied. This is done by using an interface   *
 * which only purpose is to generate android.bluetooth.le.AdvertiseData objects.        *
 *                                                                                      *
 * This exception is an additional permission under section 7 of the GNU General        *
 * Public License, version 3 (“GPLv3”).                                                 *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

package net.alea.beaconsimulator.component;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.regex.Pattern;

public class TextInputUUID extends TextInputEditText {

    private static final Logger sLogger = LoggerFactory.getLogger(TextInputUUID.class);

    public TextInputUUID(Context context) {
        super(context);
        setup();
    }

    public TextInputUUID(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public TextInputUUID(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    @Override
    public void setFilters(InputFilter[] filters) {
        InputFilter[] newFilters = new InputFilter[filters.length+1];
        newFilters[0] = new UUIDInputFilter();
        System.arraycopy(filters, 0, newFilters, 1, filters.length);
        super.setFilters(newFilters);
    }

    private void setup() {
        this.addTextChangedListener(new UUIDTextWatcher());
        super.setFilters(new InputFilter[]{new UUIDInputFilter()});
    }


    // http://stackoverflow.com/questions/3349121/how-do-i-use-inputfilter-to-limit-characters-in-an-edittext-in-android/4401227#4401227
    private static class UUIDInputFilter implements InputFilter {
        private final static Pattern VALID_CHARS = Pattern.compile("[-0-9a-fA-F]");
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source instanceof SpannableStringBuilder) {
                SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder)source;
                for (int i = end - 1; i >= start; i--) {
                    char currentChar = source.charAt(i);
                    if ( ! VALID_CHARS.matcher(String.valueOf(currentChar)).matches() ) {
                        sourceAsSpannableBuilder.delete(i, i+1);
                    }
                }
                return source;
            } else {
                StringBuilder filteredStringBuilder = new StringBuilder();
                for (int i = start; i < end; i++) {
                    char currentChar = source.charAt(i);
                    if ( VALID_CHARS.matcher(String.valueOf(currentChar)).matches() ) {
                        filteredStringBuilder.append(currentChar);
                    }
                }
                return filteredStringBuilder.toString();
            }
        }
    }


    private class UUIDTextWatcher implements TextWatcher {
        private final int TOTAL_LENGTH = 36;
        private final int[] HYPHEN_POSITIONS = {8, 8+1+4, 8+1+4+1+4, 8+1+4+1+4+1+4};
        private boolean processingText = false;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }
        @Override
        public void afterTextChanged(Editable s) {
            if (processingText) {
                sLogger.trace("afterTextChanged() - ongoing work, leaving");
                return;
            }
            processingText = true;
            sLogger.trace("afterTextChanged() - start to work on text");
            int cursorPosition = TextInputUUID.this.getSelectionStart();
            final StringBuilder buffer = new StringBuilder(TOTAL_LENGTH);
            buffer.append(s);
            for(int pos=0; pos<buffer.length(); pos++) {
                if (Arrays.binarySearch(HYPHEN_POSITIONS, pos) >= 0) {
                    if (buffer.charAt(pos) != '-') {
                        buffer.insert(pos, "-");
                        if (pos == cursorPosition-1) {
                            cursorPosition++;
                        }
                    }
                }
                else {
                    if (buffer.charAt(pos) == '-') {
                        buffer.delete(pos, pos+1);
                        if (pos == cursorPosition-1) {
                            cursorPosition--;
                        }
                    }
                }
            }


            if (buffer.length() > TOTAL_LENGTH) {
                buffer.delete(TOTAL_LENGTH, buffer.length());
            }
            s.replace(0, s.length(), buffer.toString());
            if (cursorPosition <= s.length()) {
                TextInputUUID.this.setSelection(cursorPosition);
            }

            processingText = false;
            sLogger.trace("afterTextChanged() - stop to work on text");
        }
    }

}
