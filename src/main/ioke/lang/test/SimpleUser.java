/*
 * See LICENSE file in distribution for copyright and licensing information.
 */
package ioke.lang.test;

/**
 *
 * @author <a href="mailto:ola.bini@gmail.com">Ola Bini</a>
 */
public class SimpleUser {
    public static boolean useBooleanObject(SimpleBooleanClass si) {
        return si.doTheThing();
    }

    public static boolean useBooleanInterface(SimpleBooleanInterface si) {
        return si.doSomething();
    }

    public static int useIntObject(SimpleIntClass si) {
        return si.doTheThing();
    }

    public static int useIntInterface(SimpleIntInterface si) {
        return si.doSomething();
    }

    public static short useShortObject(SimpleShortClass si) {
        return si.doTheThing();
    }

    public static short useShortInterface(SimpleShortInterface si) {
        return si.doSomething();
    }

    public static char useCharObject(SimpleCharClass si) {
        return si.doTheThing();
    }

    public static char useCharInterface(SimpleCharInterface si) {
        return si.doSomething();
    }

    public static byte useByteObject(SimpleByteClass si) {
        return si.doTheThing();
    }

    public static byte useByteInterface(SimpleByteInterface si) {
        return si.doSomething();
    }
}// SimpleUser