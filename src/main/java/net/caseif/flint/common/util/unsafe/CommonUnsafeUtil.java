package net.caseif.flint.common.util.unsafe;

import net.caseif.flint.util.unsafe.UnsafeUtil;

import java.util.regex.Pattern;

public abstract class CommonUnsafeUtil extends UnsafeUtil {

    private static final Pattern FLINT_PACKAGE_PATTERN = Pattern.compile("^net\\.caseif\\.flint");
    private static final Pattern UNSAFE_PACKAGE_PATTERN = Pattern.compile("^net\\.caseif\\.flint(.*)\\.util\\.unsafe");

    protected static void testInternalUse() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 1; i < stack.length; i++) {
            if (!UNSAFE_PACKAGE_PATTERN.matcher(stack[i].getClassName()).find()
                    && FLINT_PACKAGE_PATTERN.matcher(stack[i].getClassName()).find()) {
                break;
            } else {
                throw new IllegalStateException("UnsafeUtil may not be used externally");
            }
        }
    }

}
