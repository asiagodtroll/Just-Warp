package io.github.asiagodtroll.justwarp.domain;

public record CustomIcon(String name, String base64) {
    public CustomIcon withBase64(String newBase64) {
        return new CustomIcon(name, newBase64);
    }
}
