package io.github.asiagodtroll.justwarp.service;

public final class WarpException extends Exception {
    private final String translationKey;
    private final Object[] arguments;

    public WarpException(String translationKey, Object... arguments) {
        super(translationKey);
        this.translationKey = translationKey;
        this.arguments = arguments.clone();
    }

    public String translationKey() {
        return translationKey;
    }

    public Object[] arguments() {
        return arguments.clone();
    }
}
