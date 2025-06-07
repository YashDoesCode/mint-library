package io.mint.junit5;

import io.mint.core.SnapshotComparator;
import io.mint.core.SnapshotStore;
import io.mint.core.serializer.StringSnapshotSerializer;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.nio.file.Path;

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
        Class<?> testClass = context.getRequiredTestClass();
        EnableMintSnapshots annotation = testClass.getAnnotation(EnableMintSnapshots.class);
        String dirName = (annotation != null && !annotation.directory().isBlank())
                ? annotation.directory()
                : "__snapshots__";

        Path dirPath = Path.of(dirName);
        Path snapshotDir = dirPath.isAbsolute() || dirPath.getNameCount() > 1
                ? dirPath
                : Path.of("src", "test", "resources", dirName);

        String testName = context.getRequiredTestMethod().getName();
        SnapshotStore store = new SnapshotStore(snapshotDir);
        MintSnapshot mintSnapshot = new MintSnapshot(
                store,
                new StringSnapshotSerializer(),
                new SnapshotComparator(),
                testName
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
}
