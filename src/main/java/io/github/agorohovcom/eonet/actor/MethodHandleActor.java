package io.github.agorohovcom.eonet.actor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class MethodHandleActor implements Actor {
    private static final Logger log = LoggerFactory.getLogger(MethodHandleActor.class);

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private final Map<Class<?>, MethodHandle> handlerMap = new HashMap<>();

    private boolean initialized = false;

    protected MethodHandleActor() {
        initializeHandlers();
    }

    private void initializeHandlers() {
        if (initialized) return;

        try {
            // Находим все методы с аннотацией @Handle
            for (Method method : getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(Handle.class)) {
                    registerHandler(method);
                }
            }
            initialized = true;
            log.debug("Initialized {} handlers for {}", handlerMap.size(), getClass().getSimpleName());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize method handles for " + getClass().getSimpleName(), e);
        }
    }

    private void registerHandler(Method method) throws IllegalAccessException {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            log.warn("Method {} in {} must have exactly one parameter", method.getName(), getClass().getSimpleName());
            return;
        }

        Class<?> messageType = parameterTypes[0];

        MethodHandle handle;
        try {
            handle = LOOKUP.findVirtual(
                    getClass(),
                    method.getName(),
                    MethodType.methodType(
                            void.class,
                            messageType
                    )
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // Биндим к текущему экземпляру для лучшей производительности
        MethodHandle boundHandle = handle.bindTo(this);

        handlerMap.put(messageType, boundHandle);
        log.debug("Registered handler for {} -> {}", messageType.getSimpleName(), method.getName());
    }

    @Override
    public void onMessage(Object message) {
        if (message == null) {
            log.warn("Received null message");
            return;
        }

        MethodHandle handler = handlerMap.get(message.getClass());
        if (handler != null) {
            try {
                handler.invoke(message);
            } catch (Throwable e) {
                log.error("Handler execution failed for message type: {}", message.getClass().getSimpleName(), e);
                handleHandlerError(message, e);
            }
        } else {
            unhandled(message);
        }
    }

    protected void unhandled(Object message) {
        log.warn("No handler found for message type: {}", message.getClass().getSimpleName());
    }

    protected void handleHandlerError(Object message, Throwable error) {
        log.error("Error processing message of type: {}", message.getClass().getSimpleName(), error);
    }
}
