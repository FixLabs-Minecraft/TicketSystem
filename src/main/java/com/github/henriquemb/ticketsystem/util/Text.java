package com.github.henriquemb.ticketsystem.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Преобразует строки в формате MineDown (подмножество, которое используют языковые файлы)
 * в компоненты Adventure:
 * <ul>
 *     <li>{@code &a}, {@code &l}, {@code &r}, {@code &#RRGGBB} — цвета и форматирование;</li>
 *     <li>{@code [текст](/команда hover=подсказка)} — выполнить команду;</li>
 *     <li>{@code [текст](suggest_command=/команда hover=подсказка)} — вставить команду в чат;</li>
 *     <li>{@code [текст](https://ссылка hover=подсказка)} — открыть ссылку.</li>
 * </ul>
 */
public final class Text {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    private Text() {
    }

    public static Component parse(String input) {
        return parse(input == null ? "" : input, "");
    }

    private static Component parse(String input, String inheritedCodes) {
        TextComponent.Builder result = Component.text();
        StringBuilder plain = new StringBuilder();
        String codes = inheritedCodes;
        String plainCodes = codes;

        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);

            if (c == '&' && i + 1 < input.length()) {
                String code = readCode(input, i);
                if (code != null) {
                    codes = applyCode(codes, code);
                    plain.append(code);
                    i += code.length();
                    continue;
                }
            }

            if (c == '[') {
                int labelEnd = findClosing(input, i, '[', ']');
                if (labelEnd > 0 && labelEnd + 1 < input.length() && input.charAt(labelEnd + 1) == '(') {
                    int actionEnd = findClosing(input, labelEnd + 1, '(', ')');
                    if (actionEnd > 0) {
                        flush(result, plain, plainCodes);
                        plainCodes = codes;

                        String label = input.substring(i + 1, labelEnd);
                        String action = input.substring(labelEnd + 2, actionEnd);
                        result.append(button(parse(label, codes), action));

                        i = actionEnd + 1;
                        continue;
                    }
                }
            }

            plain.append(c);
            i++;
        }

        flush(result, plain, plainCodes);
        return result.build();
    }

    private static Component button(Component label, String action) {
        String hover = null;
        int hoverIndex = action.indexOf("hover=");
        if (hoverIndex >= 0) {
            hover = action.substring(hoverIndex + "hover=".length());
            action = action.substring(0, hoverIndex);
            // Убираем один пробел-разделитель перед hover=, остальные оставляем (нужны для suggest_command)
            if (action.endsWith(" ")) action = action.substring(0, action.length() - 1);
        }
        action = action.stripLeading();

        ClickEvent click = null;
        if (action.startsWith("suggest_command=")) {
            click = ClickEvent.suggestCommand(action.substring("suggest_command=".length()));
        }
        else if (action.startsWith("run_command=")) {
            click = ClickEvent.runCommand(action.substring("run_command=".length()).strip());
        }
        else if (action.startsWith("/")) {
            click = ClickEvent.runCommand(action.strip());
        }
        else if (action.startsWith("http://") || action.startsWith("https://")) {
            click = ClickEvent.openUrl(action.strip());
        }

        if (click != null) label = label.clickEvent(click);
        if (hover != null && !hover.isEmpty()) label = label.hoverEvent(HoverEvent.showText(LEGACY.deserialize(hover)));

        return label;
    }

    private static void flush(TextComponent.Builder result, StringBuilder plain, String codes) {
        if (plain.isEmpty()) return;
        result.append(LEGACY.deserialize(codes + plain));
        plain.setLength(0);
    }

    private static String readCode(String input, int index) {
        char next = Character.toLowerCase(input.charAt(index + 1));

        if (next == '#' && index + 8 <= input.length()) {
            String hex = input.substring(index + 2, index + 8);
            if (hex.matches("[0-9a-fA-F]{6}")) return input.substring(index, index + 8);
        }

        if ("0123456789abcdefklmnor".indexOf(next) >= 0) return input.substring(index, index + 2);

        return null;
    }

    /**
     * Запоминает активные коды, чтобы стиль переносился на кнопки и последующий текст.
     */
    private static String applyCode(String codes, String code) {
        char type = Character.toLowerCase(code.charAt(1));

        if (type == 'r') return "";
        if (type == '#' || "0123456789abcdef".indexOf(type) >= 0) return code;

        return codes + code;
    }

    private static int findClosing(String input, int start, char open, char close) {
        int depth = 0;
        for (int i = start; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == open) depth++;
            else if (c == close && --depth == 0) return i;
        }
        return -1;
    }
}
