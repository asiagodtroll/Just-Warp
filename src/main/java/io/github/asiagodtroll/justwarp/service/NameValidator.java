package io.github.asiagodtroll.justwarp.service;

final class NameValidator {
    private NameValidator() {}

    static void warp(String name) throws WarpException { standard(name); }

    static void group(String name) throws WarpException {
        standard(name);
        if (name.equalsIgnoreCase("none")) {
            throw new WarpException("error.reserved_name");
        }
    }

    static void customIcon(String name) throws WarpException {
        standard(name);
        if (name.indexOf(':') >= 0) {
            throw new WarpException("error.custom_icon_name");
        }
    }

    private static void standard(String name) throws WarpException {
        if (name == null || name.isEmpty() || name.length() > 64
                || name.codePoints().anyMatch(Character::isWhitespace)) {
            throw new WarpException("error.name");
        }
    }
}
