package io.mint.junit5;

import io.mint.core.SnapshotComparator;
import io.mint.core.SnapshotStore;
import io.mint.core.serializer.StringSnapshotSerializer;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JUnit 5 extension that injects and manages snapshot instances for test executions.
 */
public final class MintExtension implements ParameterResolver, BeforeEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(MintExtension.class);
    private static final String STORE_KEY = "mint.snapshot";

    /**
     * Constructs a new {@code MintExtension}.
     */
    public MintExtension() {
    }

    /**
     * Callback executed before each test method to configure the test's snapshot context.
     *
     * @param context the current extension context
     */
    @Override
    public void beforeEach(ExtensionContext context) {
        EnableMintSnapshots annotation = findEnableMintSnapshots(context);
        String dirName = (annotation != null && !annotation.directory().isBlank())
                ? annotation.directory()
                : "__snapshots__";

        Path dirPath = Path.of(dirName);
        Path snapshotDir = dirPath.isAbsolute() || dirPath.getNameCount() > 1
                ? dirPath
                : Path.of("src", "test", "resources", dirName);

        String snapshotName = computeSnapshotName(context);
        SnapshotStore store = new SnapshotStore(snapshotDir);
        MintSnapshot mintSnapshot = new MintSnapshot(
                store,
                new StringSnapshotSerializer(),
                new SnapshotComparator(),
                snapshotName
        );
        context.getStore(NAMESPACE).put(STORE_KEY, mintSnapshot);
    }

    /**
     * Determines if this resolver supports the given parameter.
     *
     * @param parameterContext the context for the parameter for which an argument should be resolved
     * @param extensionContext the extension context for the Executable about to be invoked
     * @return {@code true} if parameter type is {@link MintSnapshot}
     */
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().equals(MintSnapshot.class);
    }

    /**
     * Resolves an argument for the parameter.
     *
     * @param parameterContext the context for the parameter for which an argument should be resolved
     * @param extensionContext the extension context for the Executable about to be invoked
     * @return the resolved {@link MintSnapshot} instance
     * @throws ParameterResolutionException if resolution fails
     */
    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return extensionContext.getStore(NAMESPACE).get(STORE_KEY, MintSnapshot.class);
    }

    private static EnableMintSnapshots findEnableMintSnapshots(ExtensionContext context) {
        if (context.getElement().isPresent()) {
            EnableMintSnapshots annotation = context.getElement().get().getAnnotation(EnableMintSnapshots.class);
            if (annotation != null) {
                return annotation;
            }
        }
        Class<?> current = context.getTestClass().orElse(null);
        while (current != null) {
            EnableMintSnapshots annotation = current.getAnnotation(EnableMintSnapshots.class);
            if (annotation != null) {
                return annotation;
            }
            current = current.getEnclosingClass();
        }
        return null;
    }

    private static String computeSnapshotName(ExtensionContext context) {
        Method testMethod = context.getRequiredTestMethod();
        String methodName = testMethod.getName();
        String displayName = context.getDisplayName();

        StringBuilder nameBuilder = new StringBuilder();
        Class<?> testClass = context.getRequiredTestClass();
        if (testClass.isMemberClass()) {
            List<String> enclosingClasses = new ArrayList<>();
            Class<?> walker = testClass;
            while (walker != null && walker.isMemberClass()) {
                enclosingClasses.add(walker.getSimpleName());
                walker = walker.getEnclosingClass();
            }
            Collections.reverse(enclosingClasses);
            for (String enclosing : enclosingClasses) {
                nameBuilder.append(enclosing).append("_");
            }
        }
        nameBuilder.append(methodName);

        boolean isStandardTest = testMethod.isAnnotationPresent(org.junit.jupiter.api.Test.class);
        if (!isStandardTest) {
            String sanitizedSuffix = sanitizeDisplayName(displayName);
            if (!sanitizedSuffix.isBlank()) {
                nameBuilder.append("_").append(sanitizedSuffix);
            }
        }

        return nameBuilder.toString();
    }

    private static String sanitizeDisplayName(String displayName) {
        return displayName.replaceAll("[^a-zA-Z0-9_-]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }
}
