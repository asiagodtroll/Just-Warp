package io.github.asiagodtroll.justwarp.service;

final class WarpMetadataValidator {
    private WarpMetadataValidator() {}

    static void author(String author) throws WarpException {
        if (author == null || author.isBlank() || author.length() > 64) {
            throw new WarpException("error.author");
        }
    }

    static void description(String description) throws WarpException {
        if (description == null || description.length() > 256) {
            throw new WarpException("error.description");
        }
    }

}
