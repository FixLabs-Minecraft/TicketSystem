package com.github.henriquemb.ticketsystem.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Преобразует строки в формате MineDown (подмножество, которое используют языковые файлы)
 * в компоненты Adventure:
 * <ul>
 *     <li>{@code &a}, {@code &l}, {@code &r}, {@code &#RRGGBB} — цвета и форматирование;</li>
 *     <li>{@code [текст](/команда hover=подсказка)} — выполнить команду;</li>
 *     <li>{@code [текст](suggest_command=/команда hover=подсказка)} — вставить команду в чат;</li>
 *     <li>{@code [текст](https://ссылка hover=подсказка)} — открыть ссылку;</li>
 *     <li>{@code \&}, {@code \[} и т.д. — символ без специального значения (см. {@link #escape(String)}).</li>
 * </ul>
 */
public final class Text {
    private static final String COLOR_CODES = "0123456789abcdef";
    private static final NamedTextColor[] COLORS = {
            NamedTextColor.BLACK, NamedTextColor.DARK_BLUE, NamedTextColor.DARK_GREEN, NamedTextColor.DARK_AQUA,
            NamedTextColor.DARK_RED, NamedTextColor.DARK_PURPLE, NamedTextColor.GOLD, NamedTextColor.GRAY,
            NamedTextColor.DARK_GRAY, NamedTextColor.BLUE, NamedTextColor.GREEN, NamedTextColor.AQUA,
            NamedTextColor.RED, NamedTextColor.LIGHT_PURPLE, NamedTextColor.YELLOW, NamedTextColor.WHITE
    };

    private Text() {
    }

    public static Component parse(String input) {
        return parse(input == null ? "" : input, Style.empty());
    }

    /**
     * Экранирует текст игрока, чтобы в нём не срабатывали цвета и кнопки.
     */
    public static String escape(String input) {
        if (input == null) return "";

        StringBuilder sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()) {
            if (c == '\\' || c == '&' || c == '[' || c == ']' || c == '(' || c == ')') sb.append('\\');
            sb.append(c);
        }
        return sb.toString();
    }

    private static Component parse(String input, Style inherited) {
        TextComponent.Builder result = Component.text();
        StringBuilder plain = new StringBuilder();
        Style style = inherited;

        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);

            if (c == '\\' && i + 1 < input.length()) {
                plain.append(input.charAt(i + 1));
                i += 2;
                continue;
            }

            if (c == '&' && i + 1 < input.length()) {
                int length = codeLength(input, i);
                if (length > 0) {
                    flush(result, plain, style);
                    style = applyCode(style, input.substring(i + 1, i + length));
                    i += length;
                    continue;
                }
            }

            if (c == '[') {
                int labelEnd = findClosing(input, i, '[', ']');
                if (labelEnd > 0 && labelEnd + 1 < input.length() && input.charAt(labelEnd + 1) == '(') {
                    int actionEnd = findClosing(input, labelEnd + 1, '(', ')');
                    if (actionEnd > 0) {
                        flush(result, plain, style);

                        String label = input.substring(i + 1, labelEnd);
                        String action = input.substring(labelEnd + 2, actionEnd);
                        result.append(button(parse(label, style), action));

                        i = actionEnd + 1;
                        continue;
                    }
                }
            }

            plain.append(c);
            i++;
        }

        flush(result, plain, style);
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
        if (hover != null && !hover.isEmpty()) label = label.hoverEvent(HoverEvent.showText(parse(hover)));

        return label;
    }

    private static void flush(TextComponent.Builder result, StringBuilder plain, Style style) {
        if (plain.isEmpty()) return;
        result.append(Component.text(plain.toString(), style));
        plain.setLength(0);
    }

    /**
     * Длина кода форматирования, начинающегося с '&' (0 — если это не код).
     */
    private static int codeLength(String input, int index) {
        char next = Character.toLowerCase(input.charAt(index + 1));

        if (next == '#' && index + 8 <= input.length() && input.substring(index + 2, index + 8).matches("[0-9a-fA-F]{6}"))
            return 8;

        return "0123456789abcdefklmnor".indexOf(next) >= 0 ? 2 : 0;
    }

    /**
     * Цвет сбрасывает форматирование (как в обычных &-кодах), &r сбрасывает всё.
     */
    private static Style applyCode(Style style, String code) {
        char type = Character.toLowerCase(code.charAt(0));

        if (type == '#') return Style.style(TextColor.fromHexString(code));
        if (COLOR_CODES.indexOf(type) >= 0) return Style.style(COLORS[COLOR_CODES.indexOf(type)]);

        return switch (type) {
            case 'k' -> style.decoration(TextDecoration.OBFUSCATED, true);
            case 'l' -> style.decoration(TextDecoration.BOLD, true);
            case 'm' -> style.decoration(TextDecoration.STRIKETHROUGH, true);
            case 'n' -> style.decoration(TextDecoration.UNDERLINED, true);
            case 'o' -> style.decoration(TextDecoration.ITALIC, true);
            default -> Style.empty();
        };
    }

    private static int findClosing(String input, int start, char open, char close) {
        int depth = 0;
        for (int i = start; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\') {
                i++;
                continue;
            }
            if (c == open) depth++;
            else if (c == close && --depth == 0) return i;
        }
        return -1;
    }
}
