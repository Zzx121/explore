# read the maze into list with axis, map is more suitable
from pathlib import Path
import sys
# print(sys.getrecursionlimit())


lineCount = 1
maze_axis = {}
block = "*"
path = " "
start = "^"
end = "$"
with open("maze.txt") as file:
    for line in file:
        # the original rstrip will also strip the normal ' '
        for idx, p in enumerate(line.rstrip("\n")):
            maze_axis[(idx + 1, lineCount)]= p
            if(p == start):
                start_axis = (idx + 1, lineCount)
            elif(p == end):
                end_axis = (idx + 1, lineCount)
        lineCount += 1

def find_by_DFS(maze_axis, start_axis, end_axis):
# use DFS to find the path
    pathes = [start_axis]
    current_axis = start_axis
    moved_directions = {}
    # move from the start
    while(True):
        # if not in the pathes and not tried four directions, go on
        # the way of match int with the specific next move axis is not flexible
        next_possible_axis = [(current_axis[0] - 1, current_axis[1]), (current_axis[0], current_axis[1] - 1),
                            (current_axis[0] + 1, current_axis[1]), (current_axis[0], current_axis[1] + 1) ]
        moved_list = moved_directions.get(current_axis, [])
        for p in next_possible_axis:
            # priorized go through not moved axis, then go back when needed
            # if((p not in pathes or len(moved_list) == 3) and p not in moved_list):
            if(p not in pathes and p not in moved_list):
                next_axis = p
                # if(len(moved_list) == 3):
                #     pathes.remove(current_axis)
                moved_list.append(next_axis)
                break
        
        # need to go back
        if(current_axis == next_axis):
            for p in reversed(pathes):
                if(p in next_possible_axis and p not in moved_list):
                    next_axis = p
                    pathes.remove(current_axis)
                    moved_list.append(next_axis)
                    break
        
        moved_directions[current_axis] = moved_list
        
        next_sign = maze_axis.get(next_axis)
        # when meet the end just return
        if(next_axis == end_axis):
            pathes.append(next_axis)
            break
        # meet the block or out of range
        elif(next_sign == None or next_sign == block):
            next_axis = current_axis
            continue
        elif(next_sign == path):
            current_axis = next_axis
            if(next_axis not in pathes):
                pathes.append(next_axis)
            continue
    
    # paint the track
    trackListStr = ''
    for y in range(1, lineCount):
        for x in range(1, int(len(maze_axis) / (lineCount - 1)) + 1):
            if((x, y) in pathes):
                trackListStr += '#'
            else:
                trackListStr += ' '
        trackListStr += '\n'
    path = Path("trackDFS.txt")
    path.write_text(trackListStr)       
    # print(pathes)

def build_tree(current_node, passed_axis):
    current_axis = current_node["node"]
    next_possible_axis = [(current_axis[0] + 1, current_axis[1]), (current_axis[0] - 1, current_axis[1]),
                          (current_axis[0], current_axis[1] + 1), (current_axis[0], current_axis[1] - 1)]
    for n in next_possible_axis:
        ## mind of the exit(the leaf node), when encounter the block, passed, not existed node
        if((maze_axis.get(n) == path or maze_axis.get(n) == end) and n not in passed_axis):
            child_node = {"node": n, "children": []}
            ## need to record the tranversed nodes gloabally
            passed_axis.append(n)
            current_node["children"].append(build_tree(child_node, passed_axis))
        continue
    return current_node
# use BFS to find the path
def find_by_BFS(current_axis, pathes):
    # the primary goal is just tranverse simutineously
    # tranversedPath = [start_axis]
    if(current_axis == None):
        current_axis = start_axis
    next_possible_axis = [(current_axis[0] + 1, current_axis[1]), (current_axis[0] - 1, current_axis[1]),
                              (current_axis[0], current_axis[1] + 1), (current_axis[0], current_axis[1] - 1)]
    if(pathes == None):
        pathes = [start_axis]
    for n in next_possible_axis:
        if(n not in pathes and (maze_axis.get(n) == path or maze_axis.get(n) == end)):
            pathes.append(n)
            if(maze_axis.get(n) == end):
                return pathes
            else:
                # that way is based on the DFS not BFS, BFS need go through all pathes in current depth, 
                # that's there need a global depth to control the transverse, one way for that is to use the
                # parent node with the children nodes to judge the depth
                find_by_BFS(n, pathes)
    # tranverse the tree with BFS
    # construct the axis into tree structure, the start axis is the root
    # the block axis is not in the tree
    # the passing axis not considered as the children of the parent axis
    # passedAxises = [start_axis]
    # root = {"node": start_axis, "children": []}
    
    # print the final path
# tree = build_tree({"node": start_axis, "children": []}, [])
# print(tree)
# shortestPath = find_by_BFS(start_axis, None)
# print(shortestPath)
# maybe no need to build the tree, but just simulate the semontaneous iteration,
# the concept of the global depth maybe needed
def bfs_with_queue(passed_axis = [], queued_nodes = []):
    # 1.two set of axis, one for the transversed axis, one for the delt axis, when move to next level, 
    # need to process thoroughly for the current level, that's no left not processed transversed axises, maybe the stack is useful
    # 2. for the 'infinite' pathes, maybe the final path is valuable, so just copy the previous pathes
    # when iterate to next level axis, bring the pathes with the axis
    # 3. each axis have those properties: 1) depth, 2) path from root
    if queued_nodes == []:
        current_node = {"axis": start_axis, "pathes": [start_axis]}
    else:
        current_node = queued_nodes.pop(0)    
    current_axis = current_node["axis"]
    next_possible_axis = [(current_axis[0] + 1, current_axis[1]), (current_axis[0] - 1, current_axis[1]),
                          (current_axis[0], current_axis[1] + 1), (current_axis[0], current_axis[1] - 1)]
    for n in next_possible_axis:
        if((maze_axis.get(n) == path or maze_axis.get(n) == end) and n not in passed_axis):
            passed_axis.append(n)
            # otherwise, the pathes are not correct, need to not influence the parent pathes
            current_pathes = current_node["pathes"].copy()
            current_pathes.append(n)
            queued_nodes.append({"axis": n, "pathes": current_pathes})
            if(maze_axis.get(n) == end):
                return current_pathes
                
    return bfs_with_queue(passed_axis, queued_nodes)

def bfs_with_queue():
    queued_nodes = []
    passed_axis = []
    while True:
        if queued_nodes == []:
            current_node = {"axis": start_axis, "pathes": [start_axis]}
        else:
            current_node = queued_nodes.pop(0)  
        current_axis = current_node["axis"]
        next_possible_axis = [(current_axis[0] + 1, current_axis[1]), (current_axis[0] - 1, current_axis[1]),
                            (current_axis[0], current_axis[1] + 1), (current_axis[0], current_axis[1] - 1)]
        for n in next_possible_axis:
            if((maze_axis.get(n) == path or maze_axis.get(n) == end) and n not in passed_axis):
                passed_axis.append(n)
                current_pathes = current_node["pathes"].copy()
                current_pathes.append(n)
                queued_nodes.append({"axis": n, "pathes": current_pathes})
                if(maze_axis.get(n) == end):
                    return current_pathes
                    
# sys.setrecursionlimit(5000)
path = bfs_with_queue()
print(path)