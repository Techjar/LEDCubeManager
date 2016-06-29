
package com.techjar.ledcm.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Techjar
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class Tuple<A, B> {
    private A a;
    private B b;
}
