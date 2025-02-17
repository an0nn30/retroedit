package com.github.an0nn30.retroedit.event;

public record EventRecord<T>(String type, T data, Object source) {
}