package com.autotech.barcodemobilereader.controller.zxing.client.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.zxing.DecodeHintType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class DecodeHintManager {
    private static final Pattern COMMA = Pattern.compile(",");

    private DecodeHintManager() {
    }

    private static Map<String, String> splitQuery(String query) {
        Map<String, String> map = new HashMap<>();
        int pos = 0;
        while (pos < query.length()) {
            if (query.charAt(pos) == '&') {
                pos++;
                continue;
            }
            int amp = query.indexOf('&', pos);
            int equ = query.indexOf('=', pos);
            if (amp < 0) {
                String name;
                String text;
                if (equ < 0) {
                    name = query.substring(pos);
                    name = name.replace('+', ' ');
                    name = Uri.decode(name);
                    text = "";
                } else {
                    name = query.substring(pos, equ);
                    name = name.replace('+', ' ');
                    name = Uri.decode(name);
                    text = query.substring(equ + 1);
                    text = text.replace('+', ' ');
                    text = Uri.decode(text);
                }
                if (!map.containsKey(name)) {
                    map.put(name, text);
                }
                break;
            }
            if (equ < 0 || equ > amp) {
                String name = query.substring(pos, amp);
                name = name.replace('+', ' ');
                name = Uri.decode(name);
                if (!map.containsKey(name)) {
                    map.put(name, "");
                }
                pos = amp + 1;
                continue;
            }
            String name = query.substring(pos, equ);
            name = name.replace('+', ' ');
            name = Uri.decode(name);
            String text = query.substring(equ + 1, amp);
            text = text.replace('+', ' ');
            text = Uri.decode(text);
            if (!map.containsKey(name)) {
                map.put(name, text);
            }
            pos = amp + 1;
        }
        return map;
    }

    static Map<DecodeHintType, ?> parseDecodeHints(Uri inputUri) {
        String query = inputUri.getEncodedQuery();
        if (query == null || query.isEmpty()) {
            return null;
        }

        Map<String, String> parameters = splitQuery(query);

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);

        for (DecodeHintType hintType : DecodeHintType.values()) {

            if (hintType == DecodeHintType.CHARACTER_SET ||
                    hintType == DecodeHintType.NEED_RESULT_POINT_CALLBACK ||
                    hintType == DecodeHintType.POSSIBLE_FORMATS) {
                continue;
            }

            String parameterName = hintType.name();
            String parameterText = parameters.get(parameterName);
            if (parameterText == null) {
                continue;
            }
            if (hintType.getValueType().equals(Object.class)) {
                hints.put(hintType, parameterText);
                continue;
            }
            if (hintType.getValueType().equals(Void.class)) {
                hints.put(hintType, Boolean.TRUE);
                continue;
            }
            if (hintType.getValueType().equals(String.class)) {
                hints.put(hintType, parameterText);
                continue;
            }
            if (hintType.getValueType().equals(Boolean.class)) {
                if (parameterText.isEmpty()) {
                    hints.put(hintType, Boolean.TRUE);
                } else if ("0".equals(parameterText) ||
                        "false".equalsIgnoreCase(parameterText) ||
                        "no".equalsIgnoreCase(parameterText)) {
                    hints.put(hintType, Boolean.FALSE);
                } else {
                    hints.put(hintType, Boolean.TRUE);
                }

                continue;
            }
            if (hintType.getValueType().equals(int[].class)) {
                if (!parameterText.isEmpty() && parameterText.charAt(parameterText.length() - 1) == ',') {
                    parameterText = parameterText.substring(0, parameterText.length() - 1);
                }
                String[] values = COMMA.split(parameterText);
                int[] array = new int[values.length];
                for (int i = 0; i < values.length; i++) {
                    try {
                        array[i] = Integer.parseInt(values[i]);
                    } catch (NumberFormatException ignored) {
                        array = null;
                        break;
                    }
                }
                if (array != null) {
                    hints.put(hintType, array);
                }
                continue;
            }
        }
        return hints;
    }

    public static Map<DecodeHintType, Object> parseDecodeHints(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null || extras.isEmpty()) {
            return null;
        }
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);

        for (DecodeHintType hintType : DecodeHintType.values()) {

            if (hintType == DecodeHintType.CHARACTER_SET ||
                    hintType == DecodeHintType.NEED_RESULT_POINT_CALLBACK ||
                    hintType == DecodeHintType.POSSIBLE_FORMATS) {
                continue;
            }

            String hintName = hintType.name();
            if (extras.containsKey(hintName)) {
                if (hintType.getValueType().equals(Void.class)) {
                    hints.put(hintType, Boolean.TRUE);
                } else {
                    Object hintData = extras.get(hintName);
                    if (hintType.getValueType().isInstance(hintData)) {
                        hints.put(hintType, hintData);
                    }
                }
            }
        }
        return hints;
    }

}
