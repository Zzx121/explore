package cn.edu.djtu.db.entity.fle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于传输FLE过程中需要的信息
 * 
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payload {
    /**
     * Used to identify the round of the voting between peers, just abort the old turns.
     */
    private int round;
    private State state;
    private Vote vote;
    private long id;

    public enum State {
        ELECTION("election"), LEADING("leading"), FOLLOWING("following");

        State(String state) {
        }
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Vote {
        private String lastZxid;
        private long id;
    }
}
