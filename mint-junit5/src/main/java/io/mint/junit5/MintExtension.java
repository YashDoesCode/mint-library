package io.mint.junit5;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * JUnit 5 extension that injects and manages snapshot instances for test executions.
 */
public final class MintExtension implements ParameterResolver, BeforeEachCallback {

    /**
     * Constructs a new {@code MintExtension}.
     */
    public MintExtension() {
    }

    /**
     * Callback executed before each test method.
     *
     * @param context the current extension context
     */
    @Override
    public void beforeEach(ExtensionContext context) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Determines if this resolver supports the given parameter.
     *
     * @param parameterContext the context for the parameter for which an argument should be resolved
     * @param extensionContext the extension context for the Executable about to be invoked
     * @return true if parameter is supported
     */
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Resolves an argument for the parameter.
     *
     * @param parameterContext the context for the parameter for which an argument should be resolved
     * @param extensionContext the extension context for the Executable about to be invoked
     * @return the resolved argument
     * @throws ParameterResolutionException if resolution fails
     */
    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
