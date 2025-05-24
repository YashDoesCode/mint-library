package io.mint.junit5;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables Mint snapshot testing for a JUnit 5 test class.
 *
 * <p>Usage example:
 * <pre>{@code
 * @EnableMintSnapshots
 * class UserServiceTest {
 *     @Test
 *     void testUser(MintSnapshot mint) {
 *         mint.assertMatches(userService.getUser(1));
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(MintExtension.class)
public @interface EnableMintSnapshots {

    /**
     * Specifies the directory name or path relative to test resources for storing snapshots.
     *
     * @return the snapshot storage directory name
     */
    String directory() default "__snapshots__";
}
