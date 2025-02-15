package com.github.an0nn30.editor.event;

public record EventRecord<T>(String type, T data, Object source) {
}