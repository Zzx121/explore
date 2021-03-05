package cn.edu.djtu.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zzx
 * @date 2020/11/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatTreeNode {
    private Long id;
    private String content;
    private Long parentId;
    private String path;
    private String namePath;
}
