package cn.edu.djtu.db.entity.fle;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * Peers nodes
 */
@Data
@Builder
public class Node implements Comparable<Node> {
    private long id;

    @Override
    public int compareTo(@NotNull Node o) {
        return 0;
    }
}
