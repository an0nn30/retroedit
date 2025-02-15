package com.github.an0nn30.editor.event;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;


public class EventBus {
    // Using unbounded wildcard since different event types will be stored here.
    private static final Map<String, List<Consumer<?>>> listeners = new HashMap<>();

    // Subscribe a listener to an event type
    public static <T> void subscribe(String eventType, Consumer<EventRecord<T>> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(listener);
    }

    // Publish an event
    public static <T> void publish(String eventType, T data, Object source) {
        EventRecord<T> eventRecord = new EventRecord<>(eventType, data, source);
        List<Consumer<?>> eventListeners = listeners.get(eventType);

        if (eventListeners != null) {
            for (Consumer<?> consumer : eventListeners) {
                // We need to cast because we lost the type information in the map.
                @SuppressWarnings("unchecked")
                Consumer<EventRecord<T>> typedConsumer = (Consumer<EventRecord<T>>) consumer;
                typedConsumer.accept(eventRecord);
            }
        }
    }
}