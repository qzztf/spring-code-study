package cn.sexycode.spring.study.chapter4;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

/**
 * 线程级别的 Bean作用范围
 */
public class ThreadScope implements Scope {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadScope.class);

    public static final String SCOPE_NAME = "thread";

    /**
     * 用于保存线程变量
     */
    private ThreadLocal<Map<String, Object>> objectThreadLocal = new ThreadLocal<>();
    private ThreadLocal<Map<String, Runnable>> callbackThreadLocal = new ThreadLocal<>();
    /**
     * Return the object with the given name from the underlying scope,
     * {@link ObjectFactory#getObject() creating it}
     * if not found in the underlying storage mechanism.
     * <p>This is the central operation of a Scope, and the only operation
     * that is absolutely required.
     *
     * @param name          the name of the object to retrieve
     * @param objectFactory the {@link ObjectFactory} to use to create the scoped
     *                      object if it is not present in the underlying storage mechanism
     * @return the desired object (never {@code null})
     * @throws IllegalStateException if the underlying scope is not currently active
     */
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Map<String, Object> map = Optional.ofNullable(objectThreadLocal.get()).orElse(new HashMap<>());
        Object o = Optional.ofNullable(map.get(name)).orElse(objectFactory.getObject());
        map.put(name,o);
        objectThreadLocal.set(map);
        return o;
    }

    /**
     * Remove the object with the given {@code name} from the underlying scope.
     * <p>Returns {@code null} if no object was found; otherwise
     * returns the removed {@code Object}.
     * <p>Note that an implementation should also remove a registered destruction
     * callback for the specified object, if any. It does, however, <i>not</i>
     * need to <i>execute</i> a registered destruction callback in this case,
     * since the object will be destroyed by the caller (if appropriate).
     * <p><b>Note: This is an optional operation.</b> Implementations may throw
     * {@link UnsupportedOperationException} if they do not support explicitly
     * removing an object.
     *
     * @param name the name of the object to remove
     * @return the removed object, or {@code null} if no object was present
     * @throws IllegalStateException if the underlying scope is not currently active
     * @see #registerDestructionCallback
     */
    @Override
    public Object remove(String name) {
        LOGGER.info("进入remove方法");
        Map<String, Object> map = objectThreadLocal.get();
        Object o = map.remove(name);
        callbackThreadLocal.get().remove(name);
        return o;
    }

    /**
     * Register a callback to be executed on destruction of the specified
     * object in the scope (or at destruction of the entire scope, if the
     * scope does not destroy individual objects but rather only terminates
     * in its entirety).
     * <p><b>Note: This is an optional operation.</b> This method will only
     * be called for scoped beans with actual destruction configuration
     * (DisposableBean, destroy-method, DestructionAwareBeanPostProcessor).
     * Implementations should do their best to execute a given callback
     * at the appropriate time. If such a callback is not supported by the
     * underlying runtime environment at all, the callback <i>must be
     * ignored and a corresponding warning should be logged</i>.
     * <p>Note that 'destruction' refers to automatic destruction of
     * the object as part of the scope's own lifecycle, not to the individual
     * scoped object having been explicitly removed by the application.
     * If a scoped object gets removed via this facade's {@link #remove(String)}
     * method, any registered destruction callback should be removed as well,
     * assuming that the removed object will be reused or manually destroyed.
     *
     * @param name     the name of the object to execute the destruction callback for
     * @param callback the destruction callback to be executed.
     *                 Note that the passed-in Runnable will never throw an exception,
     *                 so it can safely be executed without an enclosing try-catch block.
     *                 Furthermore, the Runnable will usually be serializable, provided
     *                 that its target object is serializable as well.
     * @throws IllegalStateException if the underlying scope is not currently active
     * @see DisposableBean
     * @see AbstractBeanDefinition#getDestroyMethodName()
     * @see DestructionAwareBeanPostProcessor
     */
    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        callbackThreadLocal.get().put(name, callback);
    }

    /**
     * Resolve the contextual object for the given key, if any.
     * E.g. the HttpServletRequest object for key "request".
     *
     * @param key the contextual key
     * @return the corresponding object, or {@code null} if none found
     * @throws IllegalStateException if the underlying scope is not currently active
     */
    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    /**
     * Return the <em>conversation ID</em> for the current underlying scope, if any.
     * <p>The exact meaning of the conversation ID depends on the underlying
     * storage mechanism. In the case of session-scoped objects, the
     * conversation ID would typically be equal to (or derived from) the
     * {@link javax.servlet.http.HttpSession#getId() session ID}; in the
     * case of a custom conversation that sits within the overall session,
     * the specific ID for the current conversation would be appropriate.
     * <p><b>Note: This is an optional operation.</b> It is perfectly valid to
     * return {@code null} in an implementation of this method if the
     * underlying storage mechanism has no obvious candidate for such an ID.
     *
     * @return the conversation ID, or {@code null} if there is no
     * conversation ID for the current scope
     * @throws IllegalStateException if the underlying scope is not currently active
     */
    @Override
    public String getConversationId() {
        return null;
    }
}
