package cn.edu.djtu.excel;

/**
 * @author zzx
 * @date 2020/11/13
 */
public class BinaryTreeNode {
    public int val;
    public BinaryTreeNode left;
    public BinaryTreeNode right;

    public BinaryTreeNode(int val) {
        this.val = val;
    }

    public BinaryTreeNode(int val, BinaryTreeNode left, BinaryTreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }

    public static BinaryTreeNode newTree(Integer... vars) {
        return newTree(0, vars);
    }

    private static BinaryTreeNode newTree(int index, Integer... vars) {
        if (index >= vars.length || vars[index] == null) return null;

        BinaryTreeNode node = new BinaryTreeNode(vars[index]);
        node.left = newTree(2 * index + 1, vars);
        node.right = newTree(2 * index + 2, vars);

        return node;
    }
}
