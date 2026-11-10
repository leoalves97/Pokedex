package model;

public record Pokemon(
    int id,
    String name,
    double height,
    double weight,
    String type,
    String imageUrl
) {}