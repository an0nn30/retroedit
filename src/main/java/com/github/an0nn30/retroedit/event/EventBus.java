package com.github.an0nn30.retroedit.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A simple static event bus for decoupled communication between components.
 * <p>
 * Listeners can subscribe to a specific event type, and publishers can publish events
 * with associated data and source. Note that type safety is partially lost in the internal
 * storage due to the need to support different event types.
 * </p>
 */
public class EventBus {

    // Map storing event type keys to lists of listeners.
    // The wildcard is used because listeners for different event types are stored in the same map.
    private static final Map<String, List<Consumer<?>>> listeners = new HashMap<>();

    /**
     * Subscribes a listener to the specified event type.
     *
     * @param eventType the name of the event type.
     * @param listener  a Consumer that will be notified with an {@link EventRecord} when the event is published.
     * @param <T>       the type of data associated with the event.
     */
    public static <T> void subscribe(String eventType, Consumer<EventRecord<T>> listener) {
        // Compute the list for the event type if it does not exist, then add the listener.
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(listener);
    }

    /**
     * Publishes an event to all subscribers of the specified event type.
     *
     * @param eventType the name of the event type.
     * @param data      the data associated with the event.
     * @param source    the source object from where the event originates.
     * @param <T>       the type of the event data.
     */
    public static <T> void publish(String eventType, T data, Object source) {
        // Create an event record with the given details.
        EventRecord<T> eventRecord = new EventRecord<>(eventType, data, source);
        List<Consumer<?>> eventListeners = listeners.get(eventType);

        // If there are listeners for the event type, notify each one.
        if (eventListeners != null) {
            for (Consumer<?> consumer : eventListeners) {
                // Cast is necessary because type information was lost in the map.
                @SuppressWarnings("unchecked")
                Consumer<EventRecord<T>> typedConsumer = (Consumer<EventRecord<T>>) consumer;
                typedConsumer.accept(eventRecord);
            }
        }
    }
}