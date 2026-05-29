package com.example.backend.post;

public enum PostCategory {
    FREE("자유"),
    QUESTION("질문"),
    INFO("정보"),
    CHAT("잡담");

    private final String label;

    PostCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
