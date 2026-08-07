package io.github.asiagodtroll.justwarp.domain;

public record Warp(String name, String author, String description, String icon, WarpLocation location) {
    public Warp withName(String newName) {
        return new Warp(newName, author, description, icon, location);
    }

    public Warp withIcon(String newIcon) {
        return new Warp(name, author, description, newIcon, location);
    }

    public Warp withAuthor(String newAuthor) {
        return new Warp(name, newAuthor, description, icon, location);
    }

    public Warp withDescription(String newDescription) {
        return new Warp(name, author, newDescription, icon, location);
    }

    public Warp withLocation(WarpLocation newLocation) {
        return new Warp(name, author, description, icon, newLocation);
    }
}
