package cn.edu.djtu.excel;

import cn.edu.djtu.excel.entity.Customer;
import com.diffplug.common.base.TreeNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zzx
 * @date 2020/11/4
 */
public class TreeNodeTest {
    private final TreeNode<String> root = TreeNode.createTestData(
            "root",
            " src",
            "  org",
            "   math",
            "    Array.java",
            "    Matrix.java",
            "    QuatRot.java",
            "    Vector.java",
            "   model",
            "    generic",
            "     Constant.java",
            "     Constant.xml",
            "    geometric",
            "     Constant2.java",
            "     Constant2.xml",
            "    Component.java",
            "    DynamicComponent.java",
            "    Folder afterwards",
            "     PerturbDerivative1.java",
            "      PerturbDerivative2.java",
            "      PerturbDerivative3.java",
            "     PerturbDerivative4.java",
            "      PerturbDerivative5.java",
            "      PerturbDerivative6.java",
            " test",
            "  org2",
            "   avl",
            "    allegro.avl",
            "    allegro.mass",
            "    b737.avl",
            "   simulink",
            "    complex.mdl",
            "    long_simple.mdl",
            "    sf_tetris2.mdl",
            " RunAllTests.java"
    );
  
    @Test
    void contentTest() {
        System.out.println(root.getContent());
    }
    
    @Test
    void startWithTest() {
        String s1 = "a/b/c";
        String s2 = "a/b";
        System.out.println(s1.startsWith(s2));
    }
    
    @Test
    void splitTest() {
        String s1 = "a/b/c/d";
        String[] split = s1.split("/");
        
        for (String s : split) {
            int index = s1.lastIndexOf(s);
            System.out.println(s1.substring(0, index + 1));
        }
    }
    
    @Test
    void binaryTreeTest() {
        BinaryTreeNode root = BinaryTreeNode.newTree(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<String> answer = new ArrayList<>();
        if (root != null) searchBT(root, "", answer);
        System.out.println(answer);
    }
    
    @Test
    void binaryTreeStringBuilderTest() {
        BinaryTreeNode root = BinaryTreeNode.newTree(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        List<String> res = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        helper(res, root, sb);
        System.out.println(res);
    }

    private void searchBT(BinaryTreeNode root, String path, List<String> answer) {
        if (root.left == null && root.right == null) answer.add(path + root.val);
        if (root.left != null) searchBT(root.left, path + root.val + "->", answer);
        if (root.right != null) searchBT(root.right, path + root.val + "->", answer);
    }

    private void helper(List<String> res, BinaryTreeNode root, StringBuilder sb) {
        if(root == null) {
            return;
        }
        int len = sb.length();
        sb.append(root.val);
        if(root.left == null && root.right == null) {
            res.add(sb.toString());
        } else {
            sb.append("->");
            helper(res, root.left, sb);
            helper(res, root.right, sb);
        }
        sb.setLength(len);
    }
    
    @Test
    void emptyListTest() {
        List<Object> l = Collections.emptyList();
        l.add(2);
        System.out.println(l);
    }
}
