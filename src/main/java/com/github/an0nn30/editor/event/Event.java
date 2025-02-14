package com.github.an0nn30.editor.event;

public record Event<T>(String type, T data, Object source) {
}