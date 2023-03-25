package cn.edu.djtu.db.entity.fle;

import lombok.Data;

import java.util.List;

/**
 * 用于存储边界信息，比如votedElement，nodesCount
 */
@Data
public class Borders {
    private List<VotedElement> votedElements;
    private int nodesCount;
    @Data
    public class VotedElement {
        private Node node;
    }
}
